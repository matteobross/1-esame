package it.progmob.esame1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import kotlin.math.roundToInt

class RhythmActivity : AppCompatActivity() {

    private lateinit var txtBpm: TextView
    private lateinit var txtKey: TextView
    private lateinit var imgBeat: ImageView
    private var handler = Handler(Looper.getMainLooper())
    private var blink: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhythm)

        txtBpm = findViewById(R.id.txtBpm)
        txtKey = findViewById(R.id.txtKey)
        imgBeat = findViewById(R.id.imgBeat)

        val audioPath = intent.getStringExtra("audioPath")
        if (audioPath == null || !File(audioPath).exists()) {
            Toast.makeText(this, "Errore: file audio non trovato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, "Analisi del file in corso...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                // Step 1 — Leggi PCM
                val (pcm, sampleRate) = AudioReader.decodeToPCM(audioPath)

                // Step 2 — BPM
                val bpm = AudioAnalyzer.detectBPM(pcm, sampleRate)

                // Step 3 — Pitch
                val pitch = AudioAnalyzer.detectPitch(pcm, sampleRate)

                // Step 4 — Nota
                val note = AudioAnalyzer.pitchToNoteName(pitch)

                runOnUiThread {
                    txtBpm.text = if (bpm > 0) "BPM: $bpm" else "BPM: --"
                    txtKey.text = if (pitch > 0) "Nota: $note" else "Nota: --"

                    if (bpm > 0) {
                        startBlink(bpm)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Errore nell'analisi audio", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun startBlink(bpm: Int) {
        val interval = (60000 / bpm).toLong()
        blink = object : Runnable {
            override fun run() {
                imgBeat.alpha = if (imgBeat.alpha == 1f) 0.2f else 1f
                handler.postDelayed(this, interval)
            }
        }
        handler.post(blink!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        blink?.let { handler.removeCallbacks(it) }
    }
}
