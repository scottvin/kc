package domain

data class Hand(
    val parent: Hand? = null,
    val pocket: Hand? = null,
    val card: Card
) {
    companion object {
        val hands7: Sequence<Hand> get() = Card.collection.asSequence().flatMap { it.hands6 }
    }
    val key:Long get() = card.key.or(parent?.key ?: 0)
    val parentKey:Long get() = parent?.key ?: 0
    val children: Sequence<Hand>
        get() = card.remaining.asSequence()
            .map { copy(card = it, parent = this) }
    val filterChildren: Sequence<Hand>
        get() = card.remaining.asSequence()
            .filter { it.key.or(parentKey) == 0L }
            .map { copy(card = it, pocket = this) }
}
val Card.hand: Hand get() = Hand(card = this)
val Card.filteredHands0:Sequence<Hand> get() = sequenceOf(hand)
val Card.filteredHands1:Sequence<Hand> get() = filteredHands0.flatMap { it.filterChildren }
val Card.filteredHands2:Sequence<Hand> get() = filteredHands1.flatMap { it.filterChildren }

val Card.hands0:Sequence<Hand> get() = sequenceOf(hand)
val Card.hands1:Sequence<Hand> get() = hands0.flatMap { it.children }
val Card.hands2:Sequence<Hand> get() = hands1.flatMap { it.children }
val Card.hands3:Sequence<Hand> get() = hands2.flatMap { it.children }
val Card.hands4:Sequence<Hand> get() = hands3.flatMap { it.children }
val Card.hands5:Sequence<Hand> get() = hands4.flatMap { it.children }
val Card.hands6:Sequence<Hand> get() = hands5.flatMap { it.children }

