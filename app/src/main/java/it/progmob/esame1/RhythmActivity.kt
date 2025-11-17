package it.progmob.esame1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.onsets.OnsetHandler
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import java.io.File
import kotlin.math.roundToInt

class RhythmActivity : AppCompatActivity() {

    private lateinit var txtBpm: TextView
    private lateinit var imgBeat: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var blinkRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhythm)

        txtBpm = findViewById(R.id.txtBpm)
        imgBeat = findViewById(R.id.imgBeat)

        val audioPath = intent.getStringExtra("audioPath")
        if (audioPath == null || !File(audioPath).exists()) {
            Toast.makeText(this, "Errore: nessun file audio selezionato", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, "Analizzo ritmo in corso...", Toast.LENGTH_SHORT).show()

        Thread {
            val bpm = analyzeRhythmWithTarsos(audioPath)
            runOnUiThread {
                if (bpm > 0) {
                    txtBpm.text = "Ritmo stimato: ${bpm} BPM"
                    startBlinkAnimation(bpm)
                } else {
                    txtBpm.text = "Impossibile stimare il ritmo 😕"
                }
            }
        }.start()
    }

    // --- Analisi del ritmo con TarsosDSP ---
    private fun analyzeRhythmWithTarsos(path: String): Int {
        try {
            AndroidFFMPEGLocator(this) // garantisce supporto per formati come m4a
            val file = File(path)
            val dispatcher = AudioDispatcherFactory.fromPipe(file.absolutePath, 44100, 2048, 1024)

            val onsets = mutableListOf<Double>()
            val onsetDetector = ComplexOnsetDetector(44100.0, 2048)
            onsetDetector.setHandler(OnsetHandler { time, _ ->
                onsets.add(time)
            })

            dispatcher.addAudioProcessor(onsetDetector)
            dispatcher.addAudioProcessor(object : AudioProcessor {
                override fun processingFinished() {}
                override fun process(audioEvent: AudioEvent?): Boolean = true
            })
            dispatcher.run()

            if (onsets.size < 2) return 0

            // Calcola la media della distanza tra onsets
            val intervals = onsets.zipWithNext { a, b -> b - a }
            val avgInterval = intervals.average()
            return (60 / avgInterval).roundToInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun startBlinkAnimation(bpm: Int) {
        if (bpm <= 0) return
        val intervalMs = (60000f / bpm).roundToInt()
        blinkRunnable = object : Runnable {
            override fun run() {
                imgBeat.alpha = if (imgBeat.alpha == 1f) 0.2f else 1f
                handler.postDelayed(this, intervalMs.toLong())
            }
        }
        handler.post(blinkRunnable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        blinkRunnable?.let { handler.removeCallbacks(it) }
    }
}
