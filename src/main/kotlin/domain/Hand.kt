package domain

data class Hand(
    val parent: Hand? = null,
    val card: Card
) {
    companion object {
        val roots: Sequence<Hand> get() = Card.collection.asSequence().map { Hand(card = it) }
        val allChildren: Sequence<Hand> get() = roots.flatMap { it.children7 }
        val baseHands: Sequence<Hand>
            get() = roots
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
    }
    val key:Long get() = card.key.or(parent?.key ?: 0)
    val children2:Sequence<Hand> get() = children
    val children3:Sequence<Hand> get() = children2.flatMap { it.children }
    val children4:Sequence<Hand> get() = children3.flatMap { it.children }
    val children5:Sequence<Hand> get() = children4.flatMap { it.children }
    val children6:Sequence<Hand> get() = children5.flatMap { it.children }
    val children7:Sequence<Hand> get() = children6.flatMap { it.children }
    val children: Sequence<Hand>
        get() = card.remaining.asSequence()
            .map { copy(card = it, parent = this) }
}

