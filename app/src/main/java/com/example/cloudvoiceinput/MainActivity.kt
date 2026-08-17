package com.example.cloudvoiceinput

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val padding = (24 * resources.displayMetrics.density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }

        val title = TextView(this).apply {
            text = "Cloud Voice Input"
            textSize = 26f
        }

        val description = TextView(this).apply {
            text = """
                V0 integration test.

                The app currently returns a fixed sentence instead of transcribing audio.

                Test 1: press the button below.
                Test 2: make this app Android's default speech-input handler, then tap HeliBoard's microphone.
            """.trimIndent()
            textSize = 16f
            setPadding(0, padding, 0, padding)
        }

        val testButton = Button(this).apply {
            text = "Test recognition activity"
            setOnClickListener {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    setPackage(packageName)
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                }
                startActivityForResult(intent, 100)
            }
        }

        container.addView(title)
        container.addView(description)
        container.addView(testButton)

        setContentView(container)
    }

    @Deprecated("Kept simple for the V0 test")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = results?.firstOrNull() ?: "(no text returned)"

            android.app.AlertDialog.Builder(this)
                .setTitle("Recognition result")
                .setMessage(text)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
