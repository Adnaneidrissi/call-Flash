package com.cmc.callflash

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.os.IBinder
import android.telephony.TelephonyManager

class CallFlashService : Service() {

    // Gestionnaire de la caméra pour activer/désactiver le flash
    private lateinit var flashCameraManager: CameraManager

    // Récepteur pour surveiller les changements d'état de l'appel
    private lateinit var callStateReceiver: BroadcastReceiver

    // Gestionnaire de tâches pour planifier les opérations de flash
    private val flashHandler = Handler(Looper.getMainLooper())

    // Indicateur pour déterminer si le flash est actif
    private var isFlashActive = false

    // Intervalle de temps pour alterner l'état du flash (en millisecondes)
    private val flashToggleInterval = 500L

    override fun onCreate() {
        super.onCreate()

        // Initialisation du gestionnaire de caméra pour contrôler le flash
        flashCameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // Initialisation du récepteur pour écouter les changements d'état de l'appel
        callStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Vérifie l'état de l'appel téléphonique
                when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                    // Si le téléphone sonne, activer le flash
                    TelephonyManager.EXTRA_STATE_RINGING -> activateFlash()
                    // Si l'appel est décroché ou terminé, désactiver le flash
                    TelephonyManager.EXTRA_STATE_OFFHOOK, TelephonyManager.EXTRA_STATE_IDLE -> deactivateFlash()
                }
            }
        }

        // Filtre pour écouter uniquement les changements d'état de l'appel
        val stateFilter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(callStateReceiver, stateFilter)
    }

    // Démarre l'activation du flash en boucle
    private fun activateFlash() {
        isFlashActive = true
        flashRunnable.run()
    }

    // Arrête le flash et annule les tâches programmées
    private fun deactivateFlash() {
        isFlashActive = false
        flashHandler.removeCallbacks(flashRunnable)
        toggleFlash(false) // S'assure que le flash est éteint
    }

    // Runnable pour alterner l'état du flash périodiquement
    private val flashRunnable = object : Runnable {
        override fun run() {
            // Alterne l'état du flash (ON/OFF) basé sur isFlashActive
            toggleFlash(isFlashActive)
            isFlashActive = !isFlashActive // Inverse l'état du flash

            // Relance le Runnable après l'intervalle défini
            flashHandler.postDelayed(this, flashToggleInterval)
        }
    }

    // Fonction pour activer ou désactiver le flash
    private fun toggleFlash(state: Boolean) {
        try {
            // Sélectionne le premier ID de caméra disponible pour le mode flash
            flashCameraManager.cameraIdList.firstOrNull()?.let {
                // Active ou désactive le flash selon l'état spécifié
                flashCameraManager.setTorchMode(it, state)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace() // Gère l'exception si l'accès à la caméra échoue
        }
    }

    // Méthode obligatoire pour le Service, ici non utilisé
    override fun onBind(intent: Intent?): IBinder? = null

    // Nettoie les ressources lorsque le service est détruit
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callStateReceiver) // Désenregistre le récepteur
    }
}
