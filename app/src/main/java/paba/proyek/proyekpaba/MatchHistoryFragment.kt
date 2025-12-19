package paba.proyek.proyekpaba

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

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
    private val matchList = mutableListOf<MatchHistory>()

    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvMatchHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MatchHistoryAdapter(matchList)
        recyclerView.adapter = adapter

        loadMatches()
    }

    private fun loadMatches() {
        db.collection("matches")
            .orderBy("dateTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Log.e("Retrieve Matches", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                matchList.clear()

                for (document in snapshots.documents) {
                    val match = MatchHistory(
                        id = document.id,
                        dateTimestamp = document.getLong("dateTimestamp") ?: 0L,
                        player1Name = document.getString("player1Name") ?: "",
                        player2Name = document.getString("player2Name") ?: "",
                        scorePlayer1 = document.get("scorePlayer1").toString().toInt(),
                        scorePlayer2 = document.get("scorePlayer2").toString().toInt()
                    )
                    matchList.add(match)
                }

                adapter.notifyDataSetChanged()
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