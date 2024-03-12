package domain

data class Card(
    val rank: Rank,
    val suit: Suit,
) {
    companion object {
        val collection = Rank.collection.flatMap { it.cards }
        val top = collection[0]
        fun code(key: Long): String = cards(key).joinToString(" ") { c -> c.code }
        fun cards(key: Long): List<Card> = collection.filter { (it.key and key) == it.key }
    }
    val key: Long get() = rank.key.and(suit.key)
    val code: String get() = rank.code + suit.code

    val edges: List<CardEdge> get() = collection.filter { it.key < key }
        .map { CardEdge( cardIn = this, cardOut = it) }
}

data class CardEdge(
    val cardIn: Card,
    val cardOut: Card
) {
    val key: Long get() = cardIn.key.or(cardOut.key)
    val kindKey get() = this.key or (cardIn.rank.key and cardOut.key)
    val flushKey get()  = cardIn.key or (cardIn.suit.key and cardOut.key)
    val straightKey get()  = cardIn.key or (cardIn.rank.seriesKey and cardOut.key)
    val straightFlushKey get()  = cardIn.key or (cardIn.suit.key and cardIn.rank.seriesKey and cardOut.key)

}

