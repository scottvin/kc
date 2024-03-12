package domain

data class Suit(val index: Int, val code: String) {
    companion object {
        private const val topSuitKey = 0b1000100010001000100010001000100010001000100010001000L
        val collection: List<Suit> = listOf(
            Suit(0, "S"),
            Suit(1, "H"),
            Suit(2, "D"),
            Suit(3, "C"),
        )
    }
    val key: Long get() = topSuitKey shr index
}