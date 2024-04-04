package domain

data class Hand(
    val parent: Hand? = null,
    val card: Card
) {
    companion object {
        val allHands: Sequence<Hand> get() = Card.collection.asSequence().flatMap { it.children7 }
    }
    val key:Long get() = card.key.or(parent?.key ?: 0)
    val children: Sequence<Hand>
        get() = card.remaining.asSequence()
            .map { copy(card = it, parent = this) }
}
val Card.hand:Hand get() = Hand(card = this)
val Card.children2:Sequence<Hand> get() = hand.children
val Card.children3:Sequence<Hand> get() = children2.flatMap { it.children }
val Card.children4:Sequence<Hand> get() = children3.flatMap { it.children }
val Card.children5:Sequence<Hand> get() = children4.flatMap { it.children }
val Card.children6:Sequence<Hand> get() = children5.flatMap { it.children }
val Card.children7:Sequence<Hand> get() = children6.flatMap { it.children }
