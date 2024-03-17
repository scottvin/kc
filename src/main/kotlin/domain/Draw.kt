package domain

data class Draw(val key:Long = 0L, val name:String = "",var sequence:Int = 0) {
    constructor(): this(0L)
    companion object {
        val empty = Draw()
        val size = 10
        val top = 1L shl (Card.collection.size + size - 1)
        val ROYAL_FLUSH get() = Draw(top shr 0, "RoyalFlushDraw", 43_24)
        val STRAIGHT_FLUSH get() = Draw(top shr 1, "StraightFlush", 37_260)
        val QUADRUPLE get() = Draw(top shr 2, "Quadruple", 224_848)
        val FULL_HOUSE get() = Draw(top shr 3, "FullHouse", 3_473_184)
        val FLUSH get() = Draw(top shr 4, "Flush", 4_047_644)
        val STRAIGHT get() = Draw(top shr 5, "Straight", 6_180_020)
        val TRIPLE get() = Draw(top shr 6, "Triple", 6_461_620)
        val TWO_PAIR get() = Draw(top shr 7, "TwoPair", 31_433_400)
        val PAIR get() = Draw(top shr 8, "Pair", 58_627_800)
        val HIGH_CARD get() = Draw(top shr 9, "HighCard", 23_294_460)
    }
}
