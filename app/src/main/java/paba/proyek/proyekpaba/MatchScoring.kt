package paba.proyek.proyekpaba

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import paba.proyek.proyekpaba.databinding.ActivityMatchScoringBinding

class MatchScoring : AppCompatActivity() {

    private lateinit var binding: ActivityMatchScoringBinding
    private lateinit var db: FirebaseFirestore

    private var scoreP1 = 0
    private var scoreP2 = 0

    // DATA MASTER & LIST ADAPTER
    private var masterPlayerList = ArrayList<Player>() // Sumber data utama
    private var listForP1 = ArrayList<Player>() // Data untuk Spinner 1
    private var listForP2 = ArrayList<Player>() // Data untuk Spinner 2

    private lateinit var adapterP1: ArrayAdapter<Player>
    private lateinit var adapterP2: ArrayAdapter<Player>

    private val placeholderPlayer = Player(id = "dummy_id", name = "Pilih Player")

    // !!! PENTING: Mencegah Infinite Loop saat update silang !!!
    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchScoringBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Firebase.firestore

        setupSpinners() // Siapkan adapter kosong dulu
        setupButtons()
        loadPlayersFromFirebase() // Isi data
    }

    private fun setupSpinners() {
        adapterP1 = ArrayAdapter(this, R.layout.spinner_item, listForP1)
        adapterP2 = ArrayAdapter(this, R.layout.spinner_item, listForP2)

        binding.spinnerPlayer1.adapter = adapterP1
        binding.spinnerPlayer2.adapter = adapterP2

        // --- LISTENER PLAYER 1 ---
        binding.spinnerPlayer1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isUpdating) return // Stop jika sedang diproses program

                val selectedP1 = listForP1[position]

                // Update daftar Player 2 berdasarkan pilihan P1
                updateOtherSpinner(
                    targetList = listForP2,
                    targetAdapter = adapterP2,
                    targetSpinner = binding.spinnerPlayer2,
                    playerToExclude = selectedP1
                )
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // --- LISTENER PLAYER 2 ---
        binding.spinnerPlayer2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isUpdating) return // Stop jika sedang diproses program

                val selectedP2 = listForP2[position]

                // Update daftar Player 1 berdasarkan pilihan P2
                updateOtherSpinner(
                    targetList = listForP1,
                    targetAdapter = adapterP1,
                    targetSpinner = binding.spinnerPlayer1,
                    playerToExclude = selectedP2
                )
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Fungsi Pintar untuk Update Silang
    private fun updateOtherSpinner(
        targetList: ArrayList<Player>,
        targetAdapter: ArrayAdapter<Player>,
        targetSpinner: android.widget.Spinner,
        playerToExclude: Player
    ) {
        // 1. Simpan pemain yang SEDANG dipilih di spinner target sebelum kita update listnya
        val currentSelection = targetSpinner.selectedItem as? Player ?: placeholderPlayer

        // 2. Aktifkan Flag: "Jangan trigger listener dulu, saya mau ubah data"
        isUpdating = true

        // 3. Reset isi list target
        targetList.clear()
        targetList.add(placeholderPlayer) // Selalu add placeholder

        // 4. Masukkan semua master player KECUALI yang harus di-exclude
        if (playerToExclude.id == placeholderPlayer.id) {
            // Jika spinner pemicu memilih "Pilih Player", target boleh tampilkan semua
            targetList.addAll(masterPlayerList)
        } else {
            // Filter player yang sedang dipilih di spinner sebelah
            val filtered = masterPlayerList.filter { it.id != playerToExclude.id }
            targetList.addAll(filtered)
        }

        targetAdapter.notifyDataSetChanged()

        // 5. KEMBALIKAN PILIHAN (Restore Selection)
        // Kita cari, apakah pemain yang tadi dipilih di spinner target masih ada di list baru?
        // (Misal: P1 pilih "Ayam". P2 tadinya "Bebek". List P2 berubah (Ayam hilang). Tapi "Bebek" masih ada.
        // Maka P2 harus tetap pilih "Bebek", jangan reset ke "Pilih Player")

        var newPosition = 0 // Default ke placeholder

        // Cari index pemain lama di list yang baru
        for (i in targetList.indices) {
            if (targetList[i].id == currentSelection.id) {
                newPosition = i
                break
            }
        }

        // Set kembali posisinya
        targetSpinner.setSelection(newPosition)

        // 6. Matikan Flag
        isUpdating = false
    }

    private fun loadPlayersFromFirebase() {
        db.collection("players")
            .get()
            .addOnSuccessListener { result ->
                masterPlayerList.clear()

                for (document in result) {
                    val player = document.toObject(Player::class.java)
                    player.id = document.id
                    masterPlayerList.add(player)
                }

                // Update List P1 (Isi: Placeholder + Semua Player)
                listForP1.clear()
                listForP1.add(placeholderPlayer)
                listForP1.addAll(masterPlayerList)
                adapterP1.notifyDataSetChanged()

                // Update List P2 (Isi awal sama dengan P1)
                listForP2.clear()
                listForP2.add(placeholderPlayer)
                listForP2.addAll(masterPlayerList)
                adapterP2.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal ambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupButtons() {
        // Kontrol Score P1
        binding.btnAddP1.setOnClickListener { scoreP1++; updateScoreDisplay() }
        binding.btnMinP1.setOnClickListener { if (scoreP1 > 0) scoreP1--; updateScoreDisplay() }

        // Kontrol Score P2
        binding.btnAddP2.setOnClickListener { scoreP2++; updateScoreDisplay() }
        binding.btnMinP2.setOnClickListener { if (scoreP2 > 0) scoreP2--; updateScoreDisplay() }

        // Tombol Finish
        binding.btnFinishMatch.setOnClickListener { validateAndSaveMatch() }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun updateScoreDisplay() {
        binding.tvScoreP1.text = scoreP1.toString()
        binding.tvScoreP2.text = scoreP2.toString()

        if (scoreP1 == 21 || scoreP2 == 21) {
            binding.btnFinishMatch.performClick()
        }
    }

    private fun validateAndSaveMatch() {
        val p1 = binding.spinnerPlayer1.selectedItem as? Player
        val p2 = binding.spinnerPlayer2.selectedItem as? Player

        // Validasi: Pastikan data tidak null dan bukan Placeholder
        if (p1 == null || p2 == null || p1.id == placeholderPlayer.id || p2.id == placeholderPlayer.id) {
            Toast.makeText(this, "Mohon pilih kedua pemain!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi Extra (Meskipun spinner sudah memfilter, tetap good practice dijaga)
        if (p1.id == p2.id) {
            Toast.makeText(this, "Pemain tidak boleh sama!", Toast.LENGTH_LONG).show()
            return
        }

        if (scoreP1 == 0 && scoreP2 == 0) {
            Toast.makeText(this, "Skor masih 0-0, mainkan dulu!", Toast.LENGTH_SHORT).show()
            return
        }

        saveMatchToFirebase(p1, p2)
    }

    private fun resetMatch() {
        scoreP1 = 0
        scoreP2 = 0
        updateScoreDisplay()

        // Kembalikan Spinner ke "Pilih Player"
        binding.spinnerPlayer1.setSelection(0)
        binding.spinnerPlayer2.setSelection(0)

        Toast.makeText(this, "Siap untuk match baru!", Toast.LENGTH_SHORT).show()
    }

    private fun saveMatchToFirebase(p1: Player, p2: Player) {
        // Menyiapkan object MatchHistory
        // Pastikan constructor MatchHistory sesuai dengan Data Class kamu
        val match = MatchHistory(
            player1Name = p1.name,
            player2Name = p2.name,
            scorePlayer1 = scoreP1,
            scorePlayer2 = scoreP2
            // tambahkan field date/timestamp jika ada di data class MatchHistory
        )

        val batch = db.batch()

        // 1. Simpan Match
        val matchRef = db.collection("matches").document()
        match.id = matchRef.id
        batch.set(matchRef, match)

        // 2. Update Stats P1
        val p1Ref = db.collection("players").document(p1.id)
        val p1Win = if (scoreP1 > scoreP2) 1 else 0
        val p1Lose = if (scoreP1 < scoreP2) 1 else 0
        batch.update(p1Ref, "totalMatch", p1.totalMatch + 1, "win", p1.win + p1Win, "lose", p1.lose + p1Lose)

        // 3. Update Stats P2
        val p2Ref = db.collection("players").document(p2.id)
        val p2Win = if (scoreP2 > scoreP1) 1 else 0
        val p2Lose = if (scoreP2 < scoreP1) 1 else 0
        batch.update(p2Ref, "totalMatch", p2.totalMatch + 1, "win", p2.win + p2Win, "lose", p2.lose + p2Lose)

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Match Saved!", Toast.LENGTH_LONG).show()
//                resetMatch()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}