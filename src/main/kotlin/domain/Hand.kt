package domain

import java.util.stream.Stream

data class Hand(val key: Long = 0, val card: Card = Card.empty) {
    companion object {
        val root: Hand = Hand()
    }
    private val children: List<Hand> get() = card.remaining.asSequence().map { child(it) }.toList()
    private fun child(card: Card): Hand {
        return Hand(this.key or card.key, card)
    }
    fun code(): String = Card.cards(this.key).joinToString(" ") { it.code }
    fun children(depth:Int, depthIndex:Int = 0): Stream<Hand> {
        if(depthIndex < depth) {
            return this.children.stream().flatMap { it.children(depth, depthIndex + 1) }
        }
        return Stream.of(this)
    }
}