package domain

data class HandEdge(
    val cardIn: Card,
    val cardOut: Card
) {
    companion object {
//        val collection: List<HandEdge> = Card.collection.map { cardIn -> Card.collection.filter { } }
    }

    val key: Long get() = cardIn.key.or(cardOut.key)
    private val isStraightFlushMatch get() = cardIn.rank.next.key.and(cardIn.suit.key) == cardOut.key
    private val isStraightMatch get() = cardIn.rank.next.key == cardOut.rank.key
    private val isFlushMatch get() = cardIn.suit.key == cardOut.suit.key
    private val isKindMatch get() = cardIn.rank.key == cardOut.rank.key

    val drawUpdate: (Hand) -> Hand
        get() = when {

            isStraightMatch -> { hand ->
                updateHand(
                    hand,
                    straightKey = straightKey(hand),
                )
            }

            isKindMatch -> { hand ->
                updateHand(
                    hand,
                    kindKey = kindKey(hand),
                    twoKindKey = twoKindKey(hand),
                )
            }

            else -> { hand ->
                updateHand(hand)
            }
        }
    private fun flushKey(hand: Hand): Long {
        val flushKey = hand.baseKey.or(key).and(hand.card.suit.key)
        val flushBits = flushKey.countOneBits()
        return if (flushBits > hand.flushBits || flushKey > hand.flushKey) flushKey else hand.flushKey
    }

    private fun kindKey(hand: Hand): Long {
        val kindKey = hand.baseKey.or(key).and(hand.card.rank.key)
        val kindBits = kindKey.countOneBits()
        return if (kindBits > hand.kindBits) kindKey else hand.kindKey
    }

    private fun twoKindKey(hand: Hand): Long {
        val twoKindKey = hand.baseKey.or(key).and(hand.card.rank.key).or(hand.kindKey)
        val twoKindBits = twoKindKey.countOneBits()
        return if (twoKindBits > hand.twoKindBits) twoKindKey else hand.twoKindKey
    }

    private fun straightKey(hand: Hand): Long {
        return hand.straightKey.or(cardOut.key).and(cardOut.rank.seriesKey)
    }
    private fun straightFlushKey(hand: Hand): Long {
        val straightFlushKey = hand.straightFlushKey.or(cardOut.key).and(cardOut.suit.key)
        val straightFlushBits = straightFlushKey.countOneBits()
        return if (straightFlushBits > hand.straightFlushBits) straightFlushKey else hand.straightFlushKey
    }

    private fun updateHand(
        // Builder pattern
        hand: Hand,
        straightKey: Long = hand.straightKey,
        kindKey: Long = hand.kindKey,
        twoKindKey: Long = hand.twoKindKey,
    ): Hand {
        return hand.copy(
            handIndex = hand.handIndex + 1,
            baseKey = hand.baseKey.or(key),
            card = cardOut,
            straightKey = straightKey,
            flushKey = flushKey(hand),
            straightFlushKey = straightFlushKey(hand),
            kindKey = kindKey,
            twoKindKey = twoKindKey,
        )
    }
}