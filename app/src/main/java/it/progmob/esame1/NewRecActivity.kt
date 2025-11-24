package it.progmob.esame1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException

class NewRecActivity : AppCompatActivity() {

    private var isRecording = false
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var outputFile: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newrec)

        val btnRecord = findViewById<Button>(R.id.btnRecord)
        val btnAscolto = findViewById<Button>(R.id.btnAscolto)
        val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)

        requestAudioPermission()

        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
                btnRecord.clearAnimation()
                isRecording = false
            } else {
                if (startRecording()) {
                    btnRecord.startAnimation(blinkAnimation)
                    isRecording = true
                }
            }
        }

        btnAscolto.setOnClickListener {
            if (isRecording) {
                Toast.makeText(this, "Ferma la registrazione prima di ascoltare!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val recordedFile = File(outputFile)
            if (!recordedFile.exists() || recordedFile.length() == 0L) {
                Toast.makeText(this, "Nessuna registrazione disponibile", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, PlayActivity::class.java)
            intent.putExtra("outputFile", outputFile)
            startActivity(intent)
        }
    }

    private fun requestAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permesso di registrazione negato!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording(): Boolean {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission()
            return false
        }

        // ✔ CORREZIONE 1: genera qui un nuovo file per ogni registrazione
        val timestamp = System.currentTimeMillis()
        outputFile = "${externalCacheDir?.absolutePath}/rec_$timestamp.m4a"

        return try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            Toast.makeText(this, "Registrazione avviata!", Toast.LENGTH_SHORT).show()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Avvio registrzione fallito", Toast.LENGTH_SHORT).show()
            mediaRecorder?.release()
            mediaRecorder = null
            false
        }
    }


    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
//ho aggiunto un toast solo per capire PERCHE NON MOSTRA LA REC
        val f = File(outputFile)
        Toast.makeText(this, "Saved → Exists=${f.exists()}  size=${f.length()} bytes", Toast.LENGTH_LONG).show()
    }


    override fun onStop() {
        super.onStop()
        if (isRecording) {
            stopRecording()
            isRecording = false
        }
    }

}
