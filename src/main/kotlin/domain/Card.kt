package domain

data class Card(
    val key: Long = Long.MAX_VALUE,
    val rank: Rank = Rank(),
    val suit: Suit = Suit(),
    val code: String = "",
) {
    companion object {
        val empty: Card = Card()
        val collection: List<Card> = Rank.collection.flatMap { it.cards}
        fun cards(key: Long): List<Card> = collection.filter { (it.key and key) == it.key }
        fun create(rank: Rank, suit: Suit): Card {
            return Card(rank.key and suit.key, rank, suit, rank.code + suit.code)
        }
        fun init(){
            empty.init()
            collection.forEach { it.init() }
        }
    }
    lateinit var remaining: List<Card>
    fun init(): Card {
        this.remaining = collection.filter { it.key < this.key }
        return this
    }
}