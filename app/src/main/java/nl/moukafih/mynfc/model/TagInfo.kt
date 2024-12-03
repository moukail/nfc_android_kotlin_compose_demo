package nl.moukafih.mynfc.model

data class TagInfo(
    val manufacturer: String,
    val product: String,
    val atqa: String,
    val sak: String,
    val ats: String?
)