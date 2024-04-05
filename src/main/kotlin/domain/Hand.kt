package domain

data class Hand(
    val card: Card
) {
    companion object {
        val hands7: Sequence<Hand> get() = Card.collection.asSequence().flatMap { it.hands6 }
    }
    val children: Sequence<Hand>
        get() = card.remaining.asSequence()
            .map { copy(card = it) }
}

