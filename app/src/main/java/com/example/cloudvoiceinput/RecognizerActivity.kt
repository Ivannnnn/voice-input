package com.example.cloudvoiceinput

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class RecognizerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val padding = (24 * resources.displayMetrics.density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(padding, padding, padding, padding)
        }

        val title = TextView(this).apply {
            text = "Cloud Voice Input"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val info = TextView(this).apply {
            text = "V0: no microphone yet.\nPress the button and I will return test text to HeliBoard."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, padding)
        }

        val returnButton = Button(this).apply {
            text = "Return test transcription"
            setOnClickListener {
                returnRecognitionResult(
                    "Hello from Cloud Voice Input. The HeliBoard integration works."
                )
            }
        }

        val cancelButton = Button(this).apply {
            text = "Cancel"
            setOnClickListener {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        container.addView(title)
        container.addView(info)
        container.addView(returnButton)
        container.addView(cancelButton)

        setContentView(container)
    }

    private fun returnRecognitionResult(text: String) {
        val resultIntent = Intent().apply {
            putStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS,
                arrayListOf(text)
            )
            putExtra(
                RecognizerIntent.EXTRA_CONFIDENCE_SCORES,
                floatArrayOf(1.0f)
            )
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
