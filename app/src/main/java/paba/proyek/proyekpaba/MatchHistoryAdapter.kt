package paba.proyek.proyekpaba

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date

class MatchHistoryAdapter(
    private val items: List<MatchHistory>
) : RecyclerView.Adapter<MatchHistoryAdapter.MatchViewHolder>() {

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWinner: TextView = view.findViewById(R.id.tvWinner)
        val tvLoser: TextView = view.findViewById(R.id.tvLoser)
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val item = items[position]

        if (item.scorePlayer1 > item.scorePlayer2) {
            holder.tvWinner.text = "${item.player1Name}"
            holder.tvLoser.text = "${item.player2Name}"
            holder.tvScore.text = "${item.scorePlayer1} - ${item.scorePlayer2}"
        }
        else {
            holder.tvWinner.text = "${item.player2Name}"
            holder.tvLoser.text = "${item.player1Name}"
            holder.tvScore.text = "${item.scorePlayer2} - ${item.scorePlayer1}"
        }

        val date = Date(item.dateTimestamp)
        holder.tvDate.text =
            android.text.format.DateFormat.format("dd MMM yyyy", date)
    }

    override fun getItemCount() = items.size
}