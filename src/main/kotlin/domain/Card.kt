package domain

import common.graph.Edge

data class Card(
    val rank: Rank,
    val suit: Suit,
) {
    companion object {
        val collection: List<Card> = Rank.collection.flatMap { it.cards }
    }

    val key: Long get() = rank.key.and(suit.key)

    val code: String get() = rank.code + suit.code

    val remaining: List<Card>
        get() = collection.filter { it.key < key }
    val edge: List<CardEdge> get() = remaining.map { CardEdge(this, it) }

}

data class CardEdge(
    val cardIn:Card,
    val cardOut:Card,
)