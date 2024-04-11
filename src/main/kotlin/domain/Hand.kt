package domain

import java.io.Flushable

data class Hand(
    val card: Card,
    val parent: Hand? = null,
    val parentKey: Long = 0L,
    val baseKey: Long = card.key,

    val drawKey: Long = 0L,
    val pocketKey: Long = 0L,
    val flopKey: Long = 0L,
    val turnKey: Long = 0L,
    val riverKey: Long = 0L,

    ) {

    val straightFlushKey: Long
        get() {
            return parent?.let {
                val newStraightFlushKey = baseKey.and(card.rank.seriesKey).and(card.suit.key)
                return if (it.straightFlushKey.countOneBits() >= newStraightFlushKey.countOneBits()) it.straightFlushKey
                else newStraightFlushKey
            } ?: 0L
        }

    val flushKey: Long
        get() {
            return parent?.let {
                val newFlushKey = baseKey.and(card.suit.key)
                if (it.flushKey.countOneBits() >= newFlushKey.countOneBits()) it.flushKey else newFlushKey
            } ?: 0L
        }

    val kindKey: Long
        get() {
            return parent?.let {
                val newKindKey = baseKey.and(card.rank.key)
                if (it.kindKey.countOneBits() >= newKindKey.countOneBits()) it.kindKey else newKindKey
            } ?: 0L
        }

    val twoKindKey: Long
        get() {
            return parent?.let {
                val newTwoKindKey = it.twoKindKey.or(it.kindKey)
                if (it.twoKindKey.countOneBits() >= newTwoKindKey.countOneBits()) it.twoKindKey else newTwoKindKey
            } ?: 0L
        }

    val straightKey: Long
        get() {
            val cardKey = card.key
            return parent?.let {
                val rank = it.card.rank
                val isSeries = rank.next.key.and(cardKey) == cardKey
                val isSameKind = rank.key.and(cardKey) == cardKey
                val newStraightKey = when {
                    isSeries -> it.straightKey.or(cardKey)
                    isSameKind -> it.straightKey
                    else -> cardKey
                }.and(card.rank.seriesKey)
                if (it.straightKey.countOneBits() >= newStraightKey.countOneBits()) it.straightKey else newStraightKey
            } ?: cardKey
        }
}


