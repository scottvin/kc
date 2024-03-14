package domain

data class HandEdge(
    val cardIn: Card, val cardOut: Card
) {
    companion object {
//        val collection: List<HandEdge> = Card.collection.map { cardIn -> Card.collection.filter { } }
    }

    val key: Long get() = cardIn.key.or(cardOut.key)
    val isStraightMatch get() = cardIn.rank.key == cardOut.rank.next.key
    val isFlushMatch get() = cardIn.suit.key == cardOut.suit.key
    val isKindMatch get() = cardIn.rank.key == cardOut.rank.key

    val draw: (Hand) -> Hand
        get() = when {
            isStraightMatch && isFlushMatch -> { hand -> hand.copy(straightFlushKey = hand.straightFlushKey.or(key), straightKey = hand.straightKey.or(key), flushKey = hand.flushKey.or(key)) }
            isFlushMatch -> { hand -> hand.copy(flushKey = hand.flushKey.or(key)) }
            isKindMatch -> { hand -> hand.copy(oneKindKey = hand.oneKindKey.or(key)) }
            isStraightMatch -> { hand -> hand.copy(straightKey = hand.straightKey.or(key)) }
            else -> { hand -> hand.copy(highCardKey = hand.highCardKey.or(key)) }
        }
}