package paba.proyek.proyekpaba

data class Player(
    var id: String = "proyek-paba-d7f1d",
    val name: String = "",
    val totalMatch: Int = 0,
    val win: Int = 0,
    val lose: Int = 0
) {
    override fun toString(): String {
        return name
    }
}
