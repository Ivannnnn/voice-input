package com.example.cloudvoiceinput

import android.Manifest
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class VoiceInputService : InputMethodService() {

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var recording = false

    private lateinit var statusText: TextView
    private lateinit var recordButton: Button

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateInputView(): View {
        val p = (20 * resources.displayMetrics.density).toInt()

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(p, p, p, p)
        }

        box.addView(TextView(this).apply {
            text = "Cloud Voice Input"
            textSize = 20f
            gravity = Gravity.CENTER
        })

        statusText = TextView(this).apply {
            text = "Ready"
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, p / 2, 0, p)
        }

        recordButton = Button(this).apply {
            text = "Record"

            setOnClickListener {
                if (recording) {
                    stopRecordingAndTranscribe()
                } else {
                    startRecording()
                }
            }
        }

        val cancelButton = Button(this).apply {
            text = "Cancel"

            setOnClickListener {
                stopRecorderIfNeeded()
                returnToPreviousKeyboard()
            }
        }

        box.addView(statusText)
        box.addView(recordButton)
        box.addView(cancelButton)

        return box
    }

    private fun startRecording() {
        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            statusText.text =
                "Microphone permission missing. Open Cloud Voice Input first and grant it."
            return
        }

        val prefs = getSharedPreferences("voice_input_settings", MODE_PRIVATE)
        val apiKey = prefs.getString("elevenlabs_api_key", "") ?: ""

        if (apiKey.isBlank()) {
            statusText.text =
                "No ElevenLabs API key. Open Cloud Voice Input and enter one."
            return
        }

        audioFile = File(
            cacheDir,
            "voice_input_${System.currentTimeMillis()}.m4a"
        )

        try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }

            recording = true
            statusText.text = "Recording…"
            recordButton.text = "Stop and transcribe"

        } catch (e: Exception) {
            recorder?.release()
            recorder = null

            recording = false
            statusText.text = "Could not start recording: ${e.message}"
            recordButton.text = "Record"
        }
    }

    private fun stopRecordingAndTranscribe() {
        if (!recording) return

        try {
            recorder?.stop()
        } catch (e: RuntimeException) {
            audioFile?.delete()

            recorder?.release()
            recorder = null
            recording = false

            statusText.text =
                "Recording was too short. Try speaking for a little longer."
            recordButton.text = "Record"
            return
        }

        recorder?.release()
        recorder = null
        recording = false

        val file = audioFile

        if (file == null || !file.exists() || file.length() == 0L) {
            statusText.text = "No audio was recorded."
            recordButton.text = "Record"
            return
        }

        statusText.text = "Transcribing…"
        recordButton.isEnabled = false

        Thread {
            try {
                val transcript = transcribeWithElevenLabs(file)

                mainHandler.post {
                    if (transcript.isBlank()) {
                        statusText.text = "ElevenLabs returned an empty transcript."
                        recordButton.isEnabled = true
                        recordButton.text = "Record"
                        return@post
                    }

                    currentInputConnection?.commitText(transcript, 1)

                    statusText.text = "Done"
                    recordButton.isEnabled = true
                    recordButton.text = "Record"

                    file.delete()

                    returnToPreviousKeyboard()
                }

            } catch (e: Exception) {
                mainHandler.post {
                    statusText.text =
                        "Transcription failed: ${e.message ?: "Unknown error"}"

                    recordButton.isEnabled = true
                    recordButton.text = "Record"
                }
            }
        }.start()
    }

    private fun transcribeWithElevenLabs(file: File): String {
        val prefs = getSharedPreferences("voice_input_settings", MODE_PRIVATE)

        val apiKey =
            prefs.getString("elevenlabs_api_key", "")?.trim()
                ?: throw IllegalStateException("No ElevenLabs API key")

        if (apiKey.isBlank()) {
            throw IllegalStateException("No ElevenLabs API key")
        }

        val boundary = "----CloudVoiceInput${System.currentTimeMillis()}"

        val connection =
            URL("https://api.elevenlabs.io/v1/speech-to-text")
                .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 20_000
            connection.readTimeout = 120_000
            connection.setChunkedStreamingMode(0)

            connection.setRequestProperty(
                "xi-api-key",
                apiKey
            )

            connection.setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary=$boundary"
            )

            DataOutputStream(connection.outputStream).use { out ->

                writeFormField(
                    out,
                    boundary,
                    "model_id",
                    "scribe_v2"
                )

                writeFormField(
                    out,
                    boundary,
                    "diarize",
                    "false"
                )

                writeFormField(
                    out,
                    boundary,
                    "tag_audio_events",
                    "false"
                )

                out.writeBytes("--$boundary\r\n")

                out.writeBytes(
                    "Content-Disposition: form-data; " +
                    "name=\"file\"; filename=\"voice.m4a\"\r\n"
                )

                out.writeBytes("Content-Type: audio/mp4\r\n")
                out.writeBytes("\r\n")

                file.inputStream().use { input ->
                    input.copyTo(out)
                }

                out.writeBytes("\r\n")
                out.writeBytes("--$boundary--\r\n")
                out.flush()
            }

            val responseCode = connection.responseCode

            val stream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response =
                BufferedReader(InputStreamReader(stream)).use {
                    it.readText()
                }

            if (responseCode !in 200..299) {
                throw IllegalStateException(
                    "ElevenLabs HTTP $responseCode: $response"
                )
            }

            val json = JSONObject(response)

            return json.optString("text", "").trim()

        } finally {
            connection.disconnect()
        }
    }

    private fun writeFormField(
        out: DataOutputStream,
        boundary: String,
        name: String,
        value: String
    ) {
        out.writeBytes("--$boundary\r\n")

        out.writeBytes(
            "Content-Disposition: form-data; name=\"$name\"\r\n"
        )

        out.writeBytes("\r\n")
        out.writeBytes(value)
        out.writeBytes("\r\n")
    }

    private fun stopRecorderIfNeeded() {
        if (!recording) return

        try {
            recorder?.stop()
        } catch (_: Exception) {
        }

        recorder?.release()
        recorder = null
        recording = false

        audioFile?.delete()
    }

    private fun returnToPreviousKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val switched = switchToPreviousInputMethod()

            if (!switched) {
                requestHideSelf(0)
            }
        } else {
            requestHideSelf(0)
        }
    }

    override fun onDestroy() {
        stopRecorderIfNeeded()
        super.onDestroy()
    }
}
