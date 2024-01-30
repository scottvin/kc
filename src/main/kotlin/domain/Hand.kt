package domain

data class Hand(val key: Long = 0, val card: Card = Card.empty) {
    companion object {
        val root: Hand = Hand()
    }
    private val children: Sequence<Hand> get() = card.remaining.asSequence().map { child(it) }
    private fun child(card: Card): Hand {
        return Hand(this.key or card.key, card)
    }
    fun code(): String = Card.cards(this.key).joinToString(" ") { it.code }
    fun children(depth:Int, depthIndex:Int = 0): Sequence<Hand> {
        if(depthIndex < depth) {
            return this.children.flatMap { it.children(depth, depthIndex + 1) }
        }
        return sequenceOf(this)
    }
}