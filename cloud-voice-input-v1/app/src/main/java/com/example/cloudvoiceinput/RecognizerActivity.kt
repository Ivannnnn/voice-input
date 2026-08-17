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
        val p = (24 * resources.displayMetrics.density).toInt()
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(p,p,p,p) }
        box.addView(TextView(this).apply { text = "Cloud Voice Input"; textSize = 22f; gravity = Gravity.CENTER })
        box.addView(TextView(this).apply { text = "Generic speech-intent test."; textSize = 16f; gravity = Gravity.CENTER; setPadding(0,p,0,p) })
        box.addView(Button(this).apply {
            text = "Return test transcription"
            setOnClickListener {
                val result = Intent().apply {
                    putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arrayListOf("Hello from Cloud Voice Input. The speech intent works."))
                    putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, floatArrayOf(1.0f))
                }
                setResult(RESULT_OK, result); finish()
            }
        })
        box.addView(Button(this).apply { text = "Cancel"; setOnClickListener { setResult(RESULT_CANCELED); finish() } })
        setContentView(box)
    }
}
