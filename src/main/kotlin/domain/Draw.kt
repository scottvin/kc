package domain

data class Draw(val key:Long = 0L, val name:String = "") {
    constructor(): this(0L)
    companion object {
        val empty = Draw()
        val size = 10
        val top = 1L shl (Card.collection.size + size - 1)
        val ROYAL_FLUSH get() = Draw(top shr 0, "RoyalFlushDraw")
        val STRAIGHT_FLUSH get() = Draw(top shr 1, "StraightFlush")
        val QUADRUPLE get() = Draw(top shr 2, "Quadruple")
        val FULL_HOUSE get() = Draw(top shr 3, "FullHouse")
        val FLUSH get() = Draw(top shr 4, "Flush")
        val STRAIGHT get() = Draw(top shr 5, "Straight")
        val TRIPLE get() = Draw(top shr 6, "Triple")
        val TWO_PAIR get() = Draw(top shr 7, "TwoPair")
        val PAIR get() = Draw(top shr 8, "Pair")
        val HIGH_CARD get() = Draw(top shr 9, "HighCard")
    }
}
