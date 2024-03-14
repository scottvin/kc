package domain

open class Draw(val key:Long = 0L) {
    constructor(): this(0L)
    companion object {
        val empty = Draw()
        val size = 10
        val top = Card.collection.size + size - 1

        val ROYAL_FLUSH = RoyalFlushDraw()
        val STRAIGHT_FLUSH = StraightFlush()
        val QUADRUPLE = Quadruple()
        val FULL_HOUSE = FullHouse()
        val FLUSH = Flush()
        val STRAIGHT = Straight()
        val TRIPLE = Triple()
        val TWO_PAIR = TwoPair()
        val PAIR = Pair()
        val HIGH_CARD = HighCard()


    }
    open fun rankMatch(card: Card, index: Int): Draw = this
    open fun suitMatch(card: Card, index: Int): Draw = this
    open fun straightMatch(card: Card, index: Int): Draw = this
    open fun mismatch(card: Card, index: Int): Draw = this

}
class RoyalFlushDraw: Draw(1L shl top)
class StraightFlush: Draw(1L shl ( top - 1 ) )
class Quadruple: Draw(1L shl ( top - 2 ))
class FullHouse: Draw(1L shl ( top - 3 ))
class Flush: Draw(1L shl ( top - 4 ))
class Straight: Draw(1L shl ( top - 5 ))
class Triple: Draw(1L shl ( top - 6 ))
class TwoPair: Draw(1L shl ( top - 7 ))
class Pair: Draw(1L shl ( top - 8 ))
class HighCard: Draw(1L shl ( top - 9 ))


