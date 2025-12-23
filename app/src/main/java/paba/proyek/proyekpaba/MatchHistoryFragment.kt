package paba.proyek.proyekpaba

import MatchHistory
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MatchHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MatchHistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MatchHistoryAdapter
    private lateinit var tvEmpty: TextView
    private val allMatches = mutableListOf<MatchHistory>()
    private val filteredMatches = mutableListOf<MatchHistory>()

    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setFabAction(
            iconRes = R.drawable.add
        ) {
            startActivity(Intent(requireContext(), MatchScoring::class.java))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvEmpty = view.findViewById(R.id.tvEmptyState)

        recyclerView = view.findViewById(R.id.rvMatchHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapter = MatchHistoryAdapter(filteredMatches)
        recyclerView.adapter = adapter

        val etSearch = view.findViewById<EditText>(R.id.etSearchDate)

        etSearch.addTextChangedListener {
            val query = it.toString().lowercase()
            filteredMatches.clear()

            if (query.isEmpty()) {
                filteredMatches.addAll(allMatches)
            } else {
                for (match in allMatches) {
                    val dateText = millisToDateString(match.dateTimestamp).lowercase()
                    if (dateText.contains(query)) {
                        filteredMatches.add(match)
                    }
                }
            }
            adapter.searchQuery = query
            adapter.notifyDataSetChanged()
            checkEmptyState()
        }

        loadMatches()
    }

    private fun millisToDateString(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(millis))
    }

    private fun checkEmptyState() {
        if (filteredMatches.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }


    private fun loadMatches() {
        db.collection("matches")
            .orderBy("dateTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) return@addSnapshotListener

                allMatches.clear()
                filteredMatches.clear()

                for (document in snapshots.documents) {
                    val match = MatchHistory(
                        id = document.id,
                        dateTimestamp = document.getLong("dateTimestamp") ?: 0L,
                        player1Name = document.getString("player1Name") ?: "",
                        player2Name = document.getString("player2Name") ?: "",

                        // BAGIAN PERBAIKAN
                        // Ambil sebagai Object -> toString -> toInt
                        // Ini aman meskipun datanya String "2" atau Number 2
                        scorePlayer1 = document.get("scorePlayer1").toString().toIntOrNull() ?: 0,
                        scorePlayer2 = document.get("scorePlayer2").toString().toIntOrNull() ?: 0,

                        matchDetails = document.getString("matchDetails") ?: ""
                    )
                    allMatches.add(match)
                }

                filteredMatches.addAll(allMatches)
                adapter.notifyDataSetChanged()
                checkEmptyState()
            }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MatchHistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MatchHistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}