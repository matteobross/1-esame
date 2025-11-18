package it.progmob.esame1
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PlayActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var filePath: String? = null
    private lateinit var listView: ListView
    private lateinit var btnPlayLast: Button
    private lateinit var btnStop: Button
    private lateinit var txtTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        txtTitle = findViewById(R.id.txtTitle)
        btnPlayLast = findViewById(R.id.btnPlayLast)
        btnStop = findViewById(R.id.btnStop)
        listView = findViewById(R.id.listViewFiles)

        // Percorso file passato da NewRecActivity
        filePath = intent.getStringExtra("outputFile")

        // Mostra nome dell’ultimo file registrato
        if (filePath != null && File(filePath!!).exists()) {
            txtTitle.text = "Ultima Rec: ${File(filePath!!).name}"
        } else {
            txtTitle.text = "Nessuna registrazione recente trovata"
        }

        // Carica elenco di tutti i file nella cartella cache
        loadFileList()

        btnPlayLast.setOnClickListener {
            if (filePath == null || !File(filePath!!).exists()) {
                Toast.makeText(this, "Nessun file da riprodurre", Toast.LENGTH_SHORT).show()
            } else {
                playRecording(filePath!!)
            }
        }

        btnStop.setOnClickListener {
            stopPlayback()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = listView.adapter.getItem(position) as String
            val fullPath = "${externalCacheDir?.absolutePath}/$selectedFile"

            val intent = Intent(this, RhythmActivity::class.java)
            intent.putExtra("audioPath", fullPath)
            startActivity(intent)
        }

    }

    private fun loadFileList() {
        val files = externalCacheDir?.listFiles { file ->
            file.name.endsWith(".m4a") || file.name.endsWith(".mp3")
        }?.sortedByDescending { it.lastModified() }

        if (files.isNullOrEmpty()) {
            Toast.makeText(this, "Nessuna registrazione trovata", Toast.LENGTH_SHORT).show()
            return
        }

        val names = files.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        listView.adapter = adapter
    }

    private fun playRecording(path: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setVolume(1.0f, 1.0f)
                start()
                Toast.makeText(this@PlayActivity, "▶️ Riproduzione: ${File(path).name}", Toast.LENGTH_SHORT).show()
                setOnCompletionListener {
                    Toast.makeText(this@PlayActivity, "✅ Riproduzione terminata", Toast.LENGTH_SHORT).show()
                    release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                release()
                Toast.makeText(this@PlayActivity, "⏹️ Riproduzione fermata", Toast.LENGTH_SHORT).show()
            }
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
