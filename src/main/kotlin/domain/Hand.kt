package domain

data class Hand(
    val card: Card,
) {
    companion object {
        var roots: Sequence<Hand> = Card.collection.asSequence().map { Hand(it) }
        val baseHands: Sequence<Hand>
            get() = roots
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
    }
    val children: Sequence<Hand>
        get() = card.remaining.asSequence()
            .map { copy(card = it,) }
}

