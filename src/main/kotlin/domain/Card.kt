package domain

data class Card(
    val rank: Rank,
    val suit: Suit,
) {
    companion object {
        val collection = Rank.collection.flatMap { it.cards }
        val allEdge = collection.flatMap { it.edges }.sortedByDescending { it.key }
        val top = collection[0]
    }

    val key: Long get() = rank.key.and(suit.key)
    val code: String get() = rank.code + suit.code
    val edges: List<CardEdge>
        get() = collection.filter { it.key < key }
            .map { CardEdge(cardIn = this, cardOut = it) }
}

