package com.example.modeswitcher

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var statusText: TextView
    private var awaitingPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        statusText = findViewById(R.id.statusText)

        findViewById<android.view.View>(R.id.rootLayout).setOnClickListener { cycleMode() }

        cycleMode()
    }

    override fun onResume() {
        super.onResume()
        if (awaitingPermission && notificationManager.isNotificationPolicyAccessGranted) {
            awaitingPermission = false
            cycleMode()
        }
    }

    private fun cycleMode() {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            awaitingPermission = true
            statusText.text = "Allow \"Do Not Disturb access\", then come back"
            Toast.makeText(
                this,
                "Please allow Do Not Disturb access so the app can change sound mode",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            return
        }

        val nextMode = when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        audioManager.ringerMode = nextMode

        val modeName = when (nextMode) {
            AudioManager.RINGER_MODE_NORMAL -> "RING"
            AudioManager.RINGER_MODE_VIBRATE -> "VIBRATE"
            AudioManager.RINGER_MODE_SILENT -> "SILENT"
            else -> "UNKNOWN"
        }
        statusText.text = "Mode: $modeName"
        Toast.makeText(this, "Mode: $modeName", Toast.LENGTH_SHORT).show()

        statusText.postDelayed({ finish() }, 500)
    }
}
