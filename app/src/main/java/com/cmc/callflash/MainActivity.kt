package com.cmc.callflash

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Switch pour activer/désactiver le flash lors d'un appel

    private lateinit var flashToggleSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation du Switch depuis le layout
        flashToggleSwitch = findViewById(R.id.switchFlash)

        // Définit l'état du switch en fonction de l'état enregistré
        flashToggleSwitch.isChecked = retrieveFlashSetting()

        // Ajoute un écouteur pour gérer les changements d'état du switch
        flashToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableFlashService()  // Démarre le service si activé
            } else {
                disableFlashService() // Arrête le service si désactivé
            }
        }
    }

    // Démarre le service de flash pour les appels et enregistre l'état activé
    private fun enableFlashService() {
        val flashServiceIntent = Intent(this, MainActivity::class.java)
        startService(flashServiceIntent)
        saveFlashSetting(true)
    }

    // Arrête le service de flash pour les appels et enregistre l'état désactivé
    private fun disableFlashService() {
        val flashServiceIntent = Intent(this, MainActivity::class.java)
        stopService(flashServiceIntent)
        saveFlashSetting(false)
    }

    // Enregistre l'état du flash dans les préférences partagées
    private fun saveFlashSetting(isEnabled: Boolean) {
        val preferences = getSharedPreferences("flash_settings", MODE_PRIVATE)
        preferences.edit().putBoolean("flash_enabled", isEnabled).apply()
    }

    // Récupère l'état actuel du flash depuis les préférences partagées
    private fun retrieveFlashSetting(): Boolean {
        val preferences = getSharedPreferences("flash_settings", MODE_PRIVATE)
        return preferences.getBoolean("flash_enabled", false)
    }
}
