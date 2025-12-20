package paba.proyek.proyekpaba

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayersProfileAdapter(
    private val items: List<Player>
) : RecyclerView.Adapter<PlayersProfileAdapter.PlayersViewHolder>() {

    var searchQuery: String = ""
    private val avatarColors = listOf(
        "#F44336", // Red
        "#E91E63", // Pink
        "#9C27B0", // Purple
        "#3F51B5", // Indigo
        "#2196F3", // Blue
        "#03A9F4", // Light Blue
        "#009688", // Teal
        "#4CAF50", // Green
        "#FF9800", // Orange
        "#795548"  // Brown
    )

    class PlayersViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvPlayer: TextView = view.findViewById(R.id.tvPlayer)
        val tvWin: TextView = view.findViewById(R.id.tvWin)
        val tvLose: TextView = view.findViewById(R.id.tvLose)
        val tvTotalMatch: TextView = view.findViewById(R.id.tvTotalMatch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayersViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayersViewHolder, position: Int) {
        val item = items[position]

        holder.tvAvatar.text = item.name.firstOrNull()?.uppercase() ?: "?"
        holder.tvAvatar.background.setTint(getAvatarColor(item.id.ifEmpty { item.name }))
        holder.tvPlayer.text = highlightText(item.name, searchQuery)
        holder.tvWin.text = item.win.toString()
        holder.tvLose.text = item.lose.toString()
        holder.tvTotalMatch.text = item.totalMatch.toString()
    }

    override fun getItemCount() = items.size

    private fun getAvatarColor(key: String): Int {
        val index = kotlin.math.abs(key.hashCode()) % avatarColors.size
        return android.graphics.Color.parseColor(avatarColors[index])
    }

    private fun highlightText(text: String, query: String): SpannableString {
        val spannable = SpannableString(text)

        if (query.isEmpty()) return spannable

        val startIndex = text.lowercase().indexOf(query.lowercase())
        if (startIndex >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#0080FE")),
                startIndex,
                startIndex + query.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                startIndex + query.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}
