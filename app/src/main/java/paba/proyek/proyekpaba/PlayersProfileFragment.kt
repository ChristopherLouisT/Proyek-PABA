package paba.proyek.proyekpaba

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlayersProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayersProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlayersProfileAdapter

    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView

    private val allPlayers = mutableListOf<Player>()
    private val filteredPlayers = mutableListOf<Player>()

    private val db = Firebase.firestore

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setFabAction(
            iconRes = R.drawable.add
        ) {
            AddPlayerDialogFragment()
                .show(parentFragmentManager, "AddPlayerDialog")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_players_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvPlayersProfile)
        etSearch = view.findViewById(R.id.etSearchPlayer)
        tvEmpty = view.findViewById(R.id.tvEmptyState)

         adapter = PlayersProfileAdapter(filteredPlayers) { selectedPlayer ->
            val intent = android.content.Intent(requireContext(), PlayerDetails::class.java)
            intent.putExtra("EXTRA_PLAYER_ID", selectedPlayer.id)
            intent.putExtra("EXTRA_PLAYER_NAME", selectedPlayer.name)
            intent.putExtra("EXTRA_PLAYER_WIN", selectedPlayer.win)
            intent.putExtra("EXTRA_PLAYER_LOSE", selectedPlayer.lose)
            intent.putExtra("EXTRA_PLAYER_TOTAL", selectedPlayer.totalMatch)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val player = filteredPlayers[position]
                confirmDelete(player, position)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        setupSearch()
        loadPlayers()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener { text ->
            val query = text.toString().lowercase().trim()

            val result = if (query.isEmpty()) {
                allPlayers
            } else {
                allPlayers.filter {
                    it.name.lowercase().contains(query)
                }
            }

            adapter.searchQuery = query
            filteredPlayers.clear()
            filteredPlayers.addAll(result)
            adapter.notifyDataSetChanged()
            checkEmptyState()
        }
    }

    private fun loadPlayers() {
        db.collection("players")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) {
                    Log.e("Retrieve Players", "Listen failed.", error)
                    return@addSnapshotListener
                }

                allPlayers.clear()

                for (doc in snapshots.documents) {
                    val player = Player(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        win = doc.getLong("win")?.toInt() ?: 0,
                        lose = doc.getLong("lose")?.toInt() ?: 0,
                        totalMatch = doc.getLong("totalMatch")?.toInt() ?: 0
                    )
                    allPlayers.add(player)
                }

                filteredPlayers.clear()
                filteredPlayers.addAll(allPlayers)
                adapter.notifyDataSetChanged()
                checkEmptyState()
            }
    }

    private fun checkEmptyState() {
        if (filteredPlayers.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }

    private fun confirmDelete(player: Player, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Player")
            .setMessage("Are you sure you want to delete ${player.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deletePlayer(player.id)
            }
            .setNegativeButton("Cancel") { _, _ ->
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun deletePlayer(playerId: String) {
        db.collection("players")
            .document(playerId)
            .delete()
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
            }
    }

}
