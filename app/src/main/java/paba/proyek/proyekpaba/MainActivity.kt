package paba.proyek.proyekpaba

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var btnAddMatch: FloatingActionButton
    private lateinit var btnMatchHistory: LinearLayout
    private lateinit var btnPlayersProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnAddMatch = findViewById<FloatingActionButton>(R.id.btnAddMatch)
        btnAddMatch.setOnClickListener {
            startActivity(Intent(this, MatchScoring::class.java))
        }

        if (savedInstanceState == null) {
            replaceFragment(MatchHistoryFragment())
        }

        btnMatchHistory = findViewById<LinearLayout>(R.id.btnHistory)
        btnMatchHistory.setOnClickListener {
            replaceFragment(MatchHistoryFragment())
        }

//        btnPlayersProfile = findViewById<LinearLayout>(R.id.btnPlayers)
//        btnPlayersProfile.setOnClickListener {
//            loadFragment(MatchHistoryFragment())
//        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}