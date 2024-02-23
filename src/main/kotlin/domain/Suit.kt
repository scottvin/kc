package domain

data class Suit(val key: ULong = 0UL, val code: String = "") {
    companion object {
        private const val topSuitKey = 0b1000100010001000100010001000100010001000100010001000UL
        val collection: List<Suit> = listOf(
            Suit(topSuitKey shr 0, "S"),
            Suit(topSuitKey shr 1, "H"),
            Suit(topSuitKey shr 2, "D"),
            Suit(topSuitKey shr 3, "C"),
        )
    }
}