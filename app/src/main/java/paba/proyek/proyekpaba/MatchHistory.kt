package paba.proyek.proyekpaba

data class MatchHistory(
    var id: String = "",
    val player1Name: String = "",
    val player2Name: String = "",
    val scorePlayer1: Int = 0,
    val scorePlayer2: Int = 0,
    val dateTimestamp: Long = System.currentTimeMillis() // Penting untuk Page 2 (CJ)
)
