package paba.proyek.proyekpaba

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.Locale

class MatchHistoryAdapter(
    private val items: List<MatchHistory>
) : RecyclerView.Adapter<MatchHistoryAdapter.MatchViewHolder>() {

    var searchQuery: String = ""

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
            holder.tvWinner.text = "🏆${item.player1Name}🏆"
            holder.tvLoser.text = "${item.player2Name}"
            holder.tvScore.text = "${item.scorePlayer1} - ${item.scorePlayer2}"
        }
        else {
            holder.tvWinner.text = "🏆${item.player2Name}🏆"
            holder.tvLoser.text = "${item.player1Name}"
            holder.tvScore.text = "${item.scorePlayer2} - ${item.scorePlayer1}"
        }

        val date = millisToDateString(item.dateTimestamp)
        holder.tvDate.text = highlightText(date, searchQuery)
    }

    override fun getItemCount() = items.size

    private fun millisToDateString(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", Locale("en"))
        return sdf.format(Date(millis))
    }

    private fun highlightText(text: String, query: String): SpannableString {
        val spannable = SpannableString(text)

        if (query.isBlank()) return spannable

        val start = text.lowercase().indexOf(query.lowercase())
        if (start >= 0) {
            val end = start + query.length
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#4CAF50")),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }

}