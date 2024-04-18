package domain

import java.util.concurrent.atomic.AtomicLong

data class Draw(val key: Long = 0L, val name: String = "", var sequence5: Int = 0, var sequence7: Long = 0) {
    constructor() : this(0L)

    companion object {
        val empty = Draw()
        val size = 10
        val top = 1L shl (Card.collection.toList().size + size - 1)
        val collection: List<Draw> = listOf(
            Draw(top shr 0, "RoyalFlush", 4, 4_324),
            Draw(top shr 1, "StraightFlush", 36, 37_260),
            Draw(top shr 2, "Quadruple", 624, 224_848),
            Draw(top shr 3, "FullHouse", 3_744, 3_473_184),
            Draw(top shr 4, "Flush", 5_108, 4_047_644),
            Draw(top shr 5, "Straight", 10_200, 6_180_020),
            Draw(top shr 6, "Triple", 54_912, 6_461_620),
            Draw(top shr 7, "TwoPair", 123_552, 31_433_400),
            Draw(top shr 8, "Pair", 1_098_240, 58_627_800),
            Draw(top shr 9, "HighCard", 1_302_540, 23_294_460)
        )
        val ROYAL_FLUSH get() = collection[0]
        val STRAIGHT_FLUSH get() = collection[1]
        val QUADRUPLE get() = collection[2]
        val FULL_HOUSE get() = collection[3]
        val FLUSH get() = collection[4]
        val STRAIGHT get() = collection[5]
        val TRIPLE get() = collection[6]
        val TWO_PAIR get() = collection[7]
        val PAIR get() = collection[8]
        val HIGH_CARD get() = collection[9]
    }

//    val total = AtomicLong()
}
