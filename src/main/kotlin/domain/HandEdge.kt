package domain

data class HandEdge(
    val cardIn: Card,
    val cardOut: Card
) {
    companion object {
//        val collection: List<HandEdge> = Card.collection.map { cardIn -> Card.collection.filter { } }
    }

    val key: Long get() = cardIn.key.or(cardOut.key)
    private val isStraightMatch get() = cardIn.rank.next.key == cardOut.rank.key
    private val isFlushMatch get() = cardIn.suit.key == cardOut.suit.key
    private val isStraightFlushMatch get() = isStraightMatch && isFlushMatch
    private val isKindMatch get() = cardIn.rank.key == cardOut.rank.key

    val drawUpdate: (Hand) -> Hand
        get() = { hand ->
            when {
                hand.kindInBits == 4 || hand.kindOutBits == 4 -> updateHand(hand, kindKey = kindKey(hand))
                hand.flushOutBits == 5 -> updateHand(hand, flushKey = flushKey(hand))
                hand.straightOutBits == 5 -> updateHand(hand, straightKey = straightKey(hand))
                hand.kindInBits >= 2 && hand.kindOutBits >= 2 -> updateHand(hand, twoKindKey = twoKindKey(hand))
                hand.kindInBits == 3 || hand.kindOutBits == 3 -> updateHand(hand, kindKey = kindKey(hand))
                hand.kindInBits == 2 || hand.kindOutBits == 2 -> updateHand(hand, kindKey = kindKey(hand))
                else -> updateHand(hand)
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

    private fun updateHand(
        // Builder pattern
        hand: Hand,
        straightKey: Long = hand.straightKey,
        flushKey: Long = hand.flushKey,
        straightFlushKey: Long = hand.straightFlushKey,
        kindKey: Long = hand.kindKey,
        twoKindKey: Long = hand.twoKindKey,
    ): Hand {
        return hand.copy(
            handIndex = hand.handIndex + 1,
            baseKey = hand.baseKey.or(key),
            card = cardOut,
            straightKey = straightKey,
            flushKey = flushKey,
            straightFlushKey = straightFlushKey,
            kindKey = kindKey,
            twoKindKey = twoKindKey,
        )
    }
    val Hand.kindInBits: Int get() = cardIn.rank.key.and(this.baseKey).countOneBits()
    val Hand.kindOutBits: Int get() = cardOut.rank.key.and(this.baseKey).countOneBits()
    val Hand.flushOutBits: Int get() = cardOut.suit.key.and(this.baseKey).countOneBits()
    val Hand.straightOutBits: Int get() = cardOut.rank.seriesKey.and(this.baseKey).countOneBits()
}

