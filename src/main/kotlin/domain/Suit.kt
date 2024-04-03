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
        val _S: Suit = collection[0];
        val _H: Suit = collection[1];
        val _D: Suit = collection[2];
        val _C: Suit = collection[3];
    }
    val key: Long get() = topSuitKey shr index
}