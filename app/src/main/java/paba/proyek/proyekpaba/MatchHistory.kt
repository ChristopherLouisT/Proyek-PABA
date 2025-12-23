data class MatchHistory(
    var id: String = "",
    val player1Name: String = "",
    val player2Name: String = "",
    val scorePlayer1: Int = 0, // Ini nanti isinya TOTAL SET MENANG (Contoh: 2)
    val scorePlayer2: Int = 0, // Ini nanti isinya TOTAL SET MENANG (Contoh: 1)
    val matchDetails: String = "", // Detail skor per set (Baru)
    val dateTimestamp: Long = System.currentTimeMillis()
)