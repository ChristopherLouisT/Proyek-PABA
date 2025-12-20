package paba.proyek.proyekpaba

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class PlayerDetails : AppCompatActivity() {

    private lateinit var tvAvatar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvWin: TextView
    private lateinit var tvLose: TextView
    private lateinit var tvTotal: TextView
    private lateinit var rvMatches: RecyclerView

    private val db = Firebase.firestore
    private val playerMatches = mutableListOf<MatchHistory>()
    private lateinit var adapter: MatchHistoryAdapter

    private var playerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_details)

        // 1. Inisialisasi View
        tvAvatar = findViewById(R.id.tvDetailAvatar)
        tvName = findViewById(R.id.tvDetailName)
        tvWin = findViewById(R.id.tvDetailWin)
        tvLose = findViewById(R.id.tvDetailLose)
        tvTotal = findViewById(R.id.tvDetailTotal)
        rvMatches = findViewById(R.id.rvPlayerMatches)

        // 2. Ambil Data dari Intent
        val id = intent.getStringExtra("EXTRA_PLAYER_ID") ?: ""
        playerName = intent.getStringExtra("EXTRA_PLAYER_NAME") ?: "Unknown"
        val win = intent.getIntExtra("EXTRA_PLAYER_WIN", 0)
        val lose = intent.getIntExtra("EXTRA_PLAYER_LOSE", 0)
        val total = intent.getIntExtra("EXTRA_PLAYER_TOTAL", 0)

        // 3. Tampilkan Stats
        tvName.text = playerName
        tvWin.text = win.toString()
        tvLose.text = lose.toString()
        tvTotal.text = total.toString()

        // Set Avatar (Huruf depan & Warna acak konsisten)
        tvAvatar.text = playerName.firstOrNull()?.uppercase() ?: "?"
        tvAvatar.background.setTint(getAvatarColor(id.ifEmpty { playerName }))

        // 4. Setup RecyclerView (Pakai MatchHistoryAdapter yg sudah ada)
        adapter = MatchHistoryAdapter(playerMatches)
        rvMatches.layoutManager = LinearLayoutManager(this)
        rvMatches.adapter = adapter

        // 5. Load History Khusus Player Ini
        loadSpecificPlayerMatches(playerName)

        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)

        // LOGIKA TOMBOL BACK
        btnBack.setOnClickListener {
            finish() // Menutup activity ini dan kembali ke fragment profile
        }
    }

    private fun loadSpecificPlayerMatches(name: String) {
        // Ambil semua match, urutkan dari terbaru
        db.collection("matches")
            .orderBy("dateTimestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                playerMatches.clear()
                for (document in result) {
                    val p1 = document.getString("player1Name") ?: ""
                    val p2 = document.getString("player2Name") ?: ""

                    // LOGIKA FILTER:
                    // Masukkan ke list HANYA JIKA player ini adalah Player 1 ATAU Player 2
                    if (p1 == name || p2 == name) {
                        val match = MatchHistory(
                            id = document.id,
                            dateTimestamp = document.getLong("dateTimestamp") ?: 0L,
                            player1Name = p1,
                            player2Name = p2,
                            scorePlayer1 = document.get("scorePlayer1").toString().toInt(),
                            scorePlayer2 = document.get("scorePlayer2").toString().toInt()
                        )
                        playerMatches.add(match)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    // Helper warna avatar (sama seperti di adapter)
    private fun getAvatarColor(key: String): Int {
        val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#3F51B5", "#2196F3", "#03A9F4", "#009688", "#4CAF50", "#FF9800", "#795548")
        val index = kotlin.math.abs(key.hashCode()) % colors.size
        return Color.parseColor(colors[index])
    }
}