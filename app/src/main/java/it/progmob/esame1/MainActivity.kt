package it.progmob.esame1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAvanti = findViewById<Button>(R.id.btnAvanti)
        val btnLogout = findViewById<Button>(R.id.btnLogout)


        btnAvanti.setOnClickListener {
            val intent = Intent(this, NewRecActivity::class.java)
            startActivity(intent)
        }


        btnLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()
            finish()
        }
    }
}
