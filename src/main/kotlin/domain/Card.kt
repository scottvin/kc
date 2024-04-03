package domain

data class Card(
    val rank: Rank,
    val suit: Suit,
) {
    companion object {
        val collection: List<Card> = Rank.collection.flatMap { it.cards }
    }

    val key: Long get() = rank.key.and(suit.key)
    val code: String get() = rank.code + suit.code
    val edges: List<CardEdge>
        get() = remaining.map { CardEdge(this, it) }
    val remaining: List<Card>
        get() = collection.filter { it.key < key }
}

data class CardEdge(
    val cardIn: Card,
    val cardOut: Card,
) {
//    val key = cardIn.key.or(cardOut.key)
    val isNextRank get() = cardIn.rank.next.key == cardOut.rank.key
    val isSameRank get() = cardIn.rank.key == cardOut.rank.key
    val isSameSuit get() = cardIn.suit.key == cardOut.suit.key
    val straightFun: (Long) -> Long
        get() = when {
            isNextRank -> { oldKey -> oldKey.or(cardOut.key) }
            isSameRank -> { oldKey -> oldKey }
            else -> { _ -> cardOut.key }
        }
    val kindFun: (Long, Long) -> Long
        get() = when {
            isSameRank -> { oldKey, key ->
                val newKey = key.and(cardOut.rank.key)
                if (newKey.countOneBits() > oldKey.countOneBits()) newKey else oldKey
            }

            else -> { oldKey, _ -> oldKey }
        }
    val twoKindFun: (Long, Long) -> Long
        get() = when {
            isSameRank -> { oldKey, key ->
                val newKey = key.and(cardOut.rank.key).or(oldKey)
                if (newKey.countOneBits() > oldKey.countOneBits()) newKey else oldKey
            }

            else -> { oldKey, _ -> oldKey }
        }
    val flushFun: (Long, Long) -> Long
        get() = when {
            isSameSuit -> { oldKey, key ->
                val newKey = key.and(cardOut.suit.key)
                if (newKey.countOneBits() > oldKey.countOneBits()) newKey else oldKey
            }

            else -> { oldKey, _ -> oldKey }
        }
}