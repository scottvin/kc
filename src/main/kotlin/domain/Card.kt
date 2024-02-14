package domain

data class Card(
    val key: Long = 1L shl 52,
    val rank: Rank = Rank(),
    val suit: Suit = Suit(),
    val code: String = "",
) {
    companion object {
        val empty: Card = Card()
        val collection: List<Card> = Rank.collection.flatMap { it.cards }
//        val top: Card = collection[0]
        fun code(key: Long): String = cards(key).joinToString(" ") { c -> c.code }
        private fun cards(key: Long): List<Card> = collection.filter { (it.key and key) == it.key }
        fun create(rank: Rank, suit: Suit): Card {
            return Card(rank.key and suit.key, rank, suit, rank.code + suit.code)
        }
    }

    lateinit var next:Card
    fun next(next: Card): Card {
        this.next = next
        return this
    }

    private var remainingCards: List<Card>? = null
    val remaining: List<Card> get()  {
        if( remainingCards == null) {
            this.remainingCards = collection.filter { it.key < this.key }
        }
        return this.remainingCards!!
    }
}