package com.example.cloudvoiceinput

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val microphonePermissionRequest = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val p = (24 * resources.displayMetrics.density).toInt()
        val prefs = getSharedPreferences("voice_input_settings", MODE_PRIVATE)

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(p, p, p, p)
        }

        box.addView(TextView(this).apply {
            text = "Cloud Voice Input"
            textSize = 26f
        })

        box.addView(TextView(this).apply {
            text =
                "Uses ElevenLabs Scribe v2 batch transcription.\n\n" +
                "Enter your ElevenLabs API key below. It is stored only on this phone."
            textSize = 16f
            setPadding(0, p, 0, p / 2)
        })

        val apiKeyInput = EditText(this).apply {
            hint = "ElevenLabs API key"
            setText(prefs.getString("elevenlabs_api_key", ""))
            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        box.addView(apiKeyInput)

        box.addView(Button(this).apply {
            text = "Save API key"

            setOnClickListener {
                val key = apiKeyInput.text.toString().trim()

                prefs.edit()
                    .putString("elevenlabs_api_key", key)
                    .apply()

                Toast.makeText(
                    this@MainActivity,
                    "API key saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        box.addView(Button(this).apply {
            text = "Grant microphone permission"

            setOnClickListener {
                requestMicrophonePermission()
            }
        })

        box.addView(Button(this).apply {
            text = "Open input method settings"

            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        })

        setContentView(box)

        requestMicrophonePermission()
    }

    private fun requestMicrophonePermission() {
        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                microphonePermissionRequest
            )
        }
    }
}
