package paba.proyek.proyekpaba

import MatchHistory
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import paba.proyek.proyekpaba.databinding.ActivityMatchScoringBinding
import kotlin.math.abs

class MatchScoring : AppCompatActivity() {

    private lateinit var binding: ActivityMatchScoringBinding
    private lateinit var db: FirebaseFirestore

    // --- VARIABEL POIN & SET ---
    private var scoreP1 = 0
    private var scoreP2 = 0

    private var setsWonP1 = 0 // Jumlah set yang dimenangkan P1
    private var setsWonP2 = 0 // Jumlah set yang dimenangkan P2

    private var currentSet = 1 // Set aktif (1, 2, atau 3)
    private var setHistoryString = StringBuilder() // Menyimpan riwayat skor ("21-15, ...")

    // DATA PLAYER
    private var masterPlayerList = ArrayList<Player>()
    private var listForP1 = ArrayList<Player>()
    private var listForP2 = ArrayList<Player>()
    private lateinit var adapterP1: ArrayAdapter<Player>
    private lateinit var adapterP2: ArrayAdapter<Player>
    private val placeholderPlayer = Player(id = "dummy_id", name = "Pilih Player")
    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchScoringBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Firebase.firestore

        setupSpinners()
        setupButtons()
        loadPlayersFromFirebase()
        updateUI() // Inisialisasi tampilan awal
    }

    // --- BAGIAN SPINNER (TIDAK BERUBAH) ---
    private fun setupSpinners() {
        adapterP1 = ArrayAdapter(this, R.layout.spinner_item, listForP1)
        adapterP2 = ArrayAdapter(this, R.layout.spinner_item, listForP2)
        binding.spinnerPlayer1.adapter = adapterP1
        binding.spinnerPlayer2.adapter = adapterP2

        binding.spinnerPlayer1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isUpdating) return
                updateOtherSpinner(listForP2, adapterP2, binding.spinnerPlayer2, listForP1[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerPlayer2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isUpdating) return
                updateOtherSpinner(listForP1, adapterP1, binding.spinnerPlayer1, listForP2[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateOtherSpinner(
        targetList: ArrayList<Player>,
        targetAdapter: ArrayAdapter<Player>,
        targetSpinner: android.widget.Spinner,
        playerToExclude: Player
    ) {
        val currentSelection = targetSpinner.selectedItem as? Player ?: placeholderPlayer
        isUpdating = true
        targetList.clear()
        targetList.add(placeholderPlayer)

        if (playerToExclude.id == placeholderPlayer.id) {
            targetList.addAll(masterPlayerList)
        } else {
            val filtered = masterPlayerList.filter { it.id != playerToExclude.id }
            targetList.addAll(filtered)
        }
        targetAdapter.notifyDataSetChanged()

        var newPosition = 0
        for (i in targetList.indices) {
            if (targetList[i].id == currentSelection.id) {
                newPosition = i
                break
            }
        }
        targetSpinner.setSelection(newPosition)
        isUpdating = false
    }

    private fun loadPlayersFromFirebase() {
        db.collection("players").get().addOnSuccessListener { result ->
            masterPlayerList.clear()
            for (document in result) {
                val player = document.toObject(Player::class.java)
                player.id = document.id
                masterPlayerList.add(player)
            }
            listForP1.clear(); listForP1.add(placeholderPlayer); listForP1.addAll(masterPlayerList)
            adapterP1.notifyDataSetChanged()
            listForP2.clear(); listForP2.add(placeholderPlayer); listForP2.addAll(masterPlayerList)
            adapterP2.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal ambil data", Toast.LENGTH_SHORT).show()
        }
    }

    // --- BUTTONS & LOGIC UTAMA ---
    private fun setupButtons() {
        binding.btnAddP1.setOnClickListener {
            scoreP1++
            updateUI()
            checkSetWinner() // Cek apakah set selesai setiap poin bertambah
        }
        binding.btnMinP1.setOnClickListener {
            if (scoreP1 > 0) { scoreP1--; updateUI() }
        }

        binding.btnAddP2.setOnClickListener {
            scoreP2++
            updateUI()
            checkSetWinner() // Cek apakah set selesai setiap poin bertambah
        }
        binding.btnMinP2.setOnClickListener {
            if (scoreP2 > 0) { scoreP2--; updateUI() }
        }

        binding.btnFinishMatch.setOnClickListener {
            // Finish paksa
            validateAndSaveMatch()
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun updateUI() {
        binding.tvScoreP1.text = scoreP1.toString()
        binding.tvScoreP2.text = scoreP2.toString()

        // Update Indikator Set
        // Kita perlu try-catch atau pengecekan null jika binding belum siap, tapi di sini aman
        try {
            // Karena ID ini baru ditambahkan di XML, pastikan XML sudah diupdate
            // Jika merah, berarti XML belum diupdate
            val tvSet = findViewById<android.widget.TextView>(R.id.tvCurrentSet)
            val tvHistory = findViewById<android.widget.TextView>(R.id.tvSetHistory)

            if (tvSet != null) tvSet.text = "SET $currentSet"
            if (tvHistory != null) {
                tvHistory.text = if (setHistoryString.isEmpty()) "History: -" else "History: $setHistoryString"
            }
        } catch (e: Exception) {
            // Ignore UI update error if view not found
        }
    }

    // --- LOGIKA PERATURAN BADMINTON (DEUCE & MAX 30) ---
    private fun checkSetWinner() {
        val diff = abs(scoreP1 - scoreP2)
        val leaderScore = if (scoreP1 > scoreP2) scoreP1 else scoreP2

        // MENANG JIKA:
        // 1. Skor >= 21 DAN Selisih >= 2 (Normal / Deuce 2 poin)
        // 2. ATAU Skor == 30 (Sudden Death / Max Point)
        val isSetOver = (leaderScore >= 21 && diff >= 2) || (leaderScore == 30)

        if (isSetOver) {
            val p1 = binding.spinnerPlayer1.selectedItem as? Player
            val p2 = binding.spinnerPlayer2.selectedItem as? Player
            val winnerName = if (scoreP1 > scoreP2) p1?.name else p2?.name

            // 1. Simpan skor set ke history
            if (setHistoryString.isNotEmpty()) setHistoryString.append(", ")
            setHistoryString.append("$scoreP1-$scoreP2")

            // 2. Update Set Won
            if (scoreP1 > scoreP2) setsWonP1++ else setsWonP2++

            // 3. Munculkan Dialog
            AlertDialog.Builder(this)
                .setTitle("SET $currentSet SELESAI!")
                .setMessage("Pemenang Set: $winnerName\nSkor: $scoreP1 - $scoreP2")
                .setCancelable(false)
                .setPositiveButton("LANJUT") { _, _ ->
                    checkMatchWinner() // Cek apakah Match sudah selesai total
                }
                .show()
        }
    }

    // --- LOGIKA BEST OF 3 (RUBBER SET) ---
    private fun checkMatchWinner() {
        // Jika salah satu pemain sudah menang 2 set -> MATCH SELESAI
        if (setsWonP1 == 2 || setsWonP2 == 2) {
            val p1Name = (binding.spinnerPlayer1.selectedItem as? Player)?.name
            val p2Name = (binding.spinnerPlayer2.selectedItem as? Player)?.name
            val winner = if (setsWonP1 == 2) p1Name else p2Name

            AlertDialog.Builder(this)
                .setTitle("🏆 MATCH SELESAI! 🏆")
                .setMessage("Pemenang: $winner\nSkor Akhir (Set): $setsWonP1 - $setsWonP2\nDetail: $setHistoryString")
                .setPositiveButton("SIMPAN DATA") { _, _ ->
                    validateAndSaveMatch()
                }
                .setCancelable(false)
                .show()
        } else {
            // Jika Skor Set masih 1-0 atau 1-1, Lanjut Set Berikutnya
            currentSet++
            resetScoreForNextSet()
        }
    }

    private fun resetScoreForNextSet() {
        scoreP1 = 0
        scoreP2 = 0
        updateUI()
        Toast.makeText(this, "Memulai Set $currentSet", Toast.LENGTH_SHORT).show()
    }

    private fun validateAndSaveMatch() {
        val p1 = binding.spinnerPlayer1.selectedItem as? Player
        val p2 = binding.spinnerPlayer2.selectedItem as? Player

        if (p1 == null || p2 == null || p1.id == placeholderPlayer.id || p2.id == placeholderPlayer.id) {
            Toast.makeText(this, "Mohon pilih pemain!", Toast.LENGTH_SHORT).show()
            return
        }
        if (p1.id == p2.id) {
            Toast.makeText(this, "Pemain tidak boleh sama!", Toast.LENGTH_LONG).show()
            return
        }

        // --- SIMPAN KE FIREBASE ---
        val match = MatchHistory(
            player1Name = p1.name,
            player2Name = p2.name,
            scorePlayer1 = setsWonP1, // Simpan Jumlah SET yang dimenangkan
            scorePlayer2 = setsWonP2,
            matchDetails = setHistoryString.toString() // Simpan detail skor per set
        )

        val batch = db.batch()
        val matchRef = db.collection("matches").document()
        match.id = matchRef.id
        batch.set(matchRef, match)

        // Update Stats Player 1
        val p1Ref = db.collection("players").document(p1.id)
        val p1Win = if (setsWonP1 > setsWonP2) 1 else 0
        val p1Lose = if (setsWonP1 < setsWonP2) 1 else 0
        batch.update(p1Ref, "totalMatch", p1.totalMatch + 1, "win", p1.win + p1Win, "lose", p1.lose + p1Lose)

        // Update Stats Player 2
        val p2Ref = db.collection("players").document(p2.id)
        val p2Win = if (setsWonP2 > setsWonP1) 1 else 0
        val p2Lose = if (setsWonP2 < setsWonP1) 1 else 0
        batch.update(p2Ref, "totalMatch", p2.totalMatch + 1, "win", p2.win + p2Win, "lose", p2.lose + p2Lose)

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Data Berhasil Disimpan!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}