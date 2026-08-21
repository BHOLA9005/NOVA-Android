package com.nova.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var chat: TextView
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chat = findViewById(R.id.chat)
        input = findViewById(R.id.input)
        val send = findViewById<Button>(R.id.send)
        val voice = findViewById<Button>(R.id.voice)
        val search = findViewById<Button>(R.id.search)

        tts = TextToSpeech(this) { tts.language = Locale("hi", "IN") }

        send.setOnClickListener { respond(input.text.toString()) }
        voice.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10)
            } else startVoice()
        }
        search.setOnClickListener {
            val q = input.text.toString().trim()
            if (q.isNotEmpty()) startActivity(Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", q))
        }
    }

    private fun startVoice() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "NOVA ko boliye...")
        startActivityForResult(i, 20)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 20 && resultCode == RESULT_OK) {
            val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: return
            input.setText(text)
            respond(text)
        }
    }

    private fun respond(text: String) {
        val q = text.trim()
        if (q.isEmpty()) return
        chat.text = "You: $q\n\nNOVA: Main aapki baat samajh gaya. Android V1 mein AI API connect karne ke liye settings mein API configuration add karein."
        tts.speak("Main aapki baat samajh gaya.", TextToSpeech.QUEUE_FLUSH, null, "nova")
        input.text.clear()
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
