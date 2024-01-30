package domain

data class Rank(val key: Long = 0, val code: String = "") {
    companion object {
        private const val topRankKey = 0b1111L
        var collection: List<Rank> = listOf(
            Rank(topRankKey shl (12 * 4), "A"),
            Rank(topRankKey shl (11 * 4), "K"),
            Rank(topRankKey shl (10 * 4), "Q"),
            Rank(topRankKey shl (9 * 4), "J"),
            Rank(topRankKey shl (8 * 4), "T"),
            Rank(topRankKey shl (7 * 4), "9"),
            Rank(topRankKey shl (6 * 4), "8"),
            Rank(topRankKey shl (5 * 4), "7"),
            Rank(topRankKey shl (4 * 4), "6"),
            Rank(topRankKey shl (3 * 4), "5"),
            Rank(topRankKey shl (2 * 4), "4"),
            Rank(topRankKey shl (1 * 4), "3"),
            Rank(topRankKey shl (0 * 4), "2"),
        )
    }
    val cards: List<Card> get() = Suit.collection.map{ Card.create(this, it) }
}