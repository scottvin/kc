package domain

data class Card(
    val key: Long = 1L shl 52,
    val rank: Rank = Rank(),
    val suit: Suit = Suit(),
    val code: String = "",
    var next: Card? = null,
    var edges: List<CardEdge>? = listOf(),
) {
    companion object {
        val empty: Card = Card()
        val collection: List<Card> = Rank.collection.flatMap { it.cards }
        init {
            collection.zipWithNext().forEach { (card1, card2) -> card1.next(card2) }
        }
        fun code(key: Long): String = cards(key).joinToString(" ") { c -> c.code }
        fun cards(key: Long): List<Card> = collection.filter { (it.key and key) == it.key }
        fun create(rank: Rank, suit: Suit): Card {
            return Card(rank.key and suit.key, rank, suit, rank.code + suit.code)
        }
    }

    fun next(next: Card): Card {
        this.next = next
        return next
    }

    private var remainingCards: List<Card>? = null
    val remaining: List<Card>
        get() {
            if (remainingCards == null) {
                this.remainingCards = collection.filter { it.key < this.key }
            }
            return this.remainingCards!!
        }

    fun edge(card: Card): CardEdge {
        return CardEdge(
            kindKey = this.key or (this.rank.key and card.key),
            flushKey = this.key or (this.suit.key and card.key),
            straightKey = this.key or (this.rank.series.key and card.key),
            straightFlushKey = this.key or (this.suit.key and this.rank.series.key and card.key)
        )
    }
}

class CardEdge(
    val kindKey: Long,
    val flushKey: Long,
    val straightKey: Long,
    val straightFlushKey: Long
)

