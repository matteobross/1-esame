package it.progmob.esame1

import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class NewRecActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private lateinit var outputFile: String

    private lateinit var btnRecord: Button
    private lateinit var btnAscolto: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newrec)

        btnRecord = findViewById(R.id.btnRecord)
        btnAscolto = findViewById(R.id.btnAscolto)

        outputFile = "${externalCacheDir?.absolutePath}/recording_${System.currentTimeMillis()}.m4a"

        // ----- BOTTONE REGISTRA -----
        btnRecord.setOnClickListener {
            if (!isRecording) startRecording()
            else stopRecording()
        }

        // ----- BOTTONE ASCOLTO -----
        btnAscolto.setOnClickListener {

            val recordedFile = File(outputFile)

            if (!recordedFile.exists() || recordedFile.length() == 0L) {
                Toast.makeText(this, "Nessuna registrazione disponibile", Toast.LENGTH_SHORT).show()

            }

            // Apri PlayActivity passando il file registrato
            val intent = Intent(this, PlayActivity::class.java)
            intent.putExtra("outputFile", outputFile)
            startActivity(intent)
        }
    }

    // -------------------------------------------------------
    //              FUNZIONI DI REGISTRAZIONE
    // -------------------------------------------------------

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }

            isRecording = true
            btnRecord.text = "FERMA"
            Toast.makeText(this, "Registrazione avviata", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            btnRecord.text = "REGISTRA"

            Toast.makeText(this, "Registrazione salvata", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Errore nello stop: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        }
    }
}
