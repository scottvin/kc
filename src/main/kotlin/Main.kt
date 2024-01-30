import java.time.Instant


fun main() {
    init()
    val start1 = Instant.now().toEpochMilli()
    val cnt1 = generateSequence()/*.onEach { println(it.code()) }*/.count()
    val end1 = Instant.now().toEpochMilli() - start1
    println(cnt1)
    println(end1)
}
fun init(){
    Card.empty.init()
    Card.collection.forEach { it.init() }
}
fun generateSequence(): Sequence<Hand> {
    return sequenceOf(Hand.root)
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
}

data class Hand(val key: Long = 0, val card: Card = Card.empty) {
    companion object {
        val root:Hand = Hand()
    }
    val children: Sequence<Hand> get() = card.remaining.asSequence().map { child(it) }
    private fun child(card: Card): Hand {
        return Hand(this.key or card.key, card)
    }
    fun code(): String = Card.cards(this.key).joinToString(" ") { it.code }
}
data class Rank(val key: Long = 0, val code: String = "") {
    companion object {
        private const val topRankKey = 0b1111L
        var collection: List<Rank> = listOf(
            Rank(topRankKey shl (12 * 4), "A"),
            Rank(topRankKey shl (11 * 4), "K"),
            Rank(topRankKey shl (10 * 4), "Q"),
            Rank(topRankKey shl (9 * 4), "J"),
            Rank(topRankKey shl (8 * 4), "T"),
            Rank(topRankKey shl (7 * 4), "9"),
            Rank(topRankKey shl (6 * 4), "8"),
            Rank(topRankKey shl (5 * 4), "7"),
            Rank(topRankKey shl (4 * 4), "6"),
            Rank(topRankKey shl (3 * 4), "5"),
            Rank(topRankKey shl (2 * 4), "4"),
            Rank(topRankKey shl (1 * 4), "3"),
            Rank(topRankKey shl (0 * 4), "2"),
        )
    }
    val cards: List<Card> get() = Suit.collection.map{ Card.create(this, it) }
}
data class Suit(val key: Long = 0, val code: String = "") {
    companion object {
        private const val topSuitKey = 0b1000100010001000100010001000100010001000100010001000L
        val collection: List<Suit> = listOf(
            Suit(topSuitKey shr 0, "S"),
            Suit(topSuitKey shr 1, "H"),
            Suit(topSuitKey shr 2, "D"),
            Suit(topSuitKey shr 3, "C"),
        )
    }
}
data class Card(
    val key: Long = Long.MAX_VALUE,
    val rank: Rank = Rank(),
    val suit: Suit = Suit(),
    val code: String = "",
) {
    companion object {
        val empty:Card = Card()
        val collection: List<Card> = Rank.collection.flatMap { it.cards}
        fun cards(key: Long): List<Card> = collection.filter { (it.key and key) == it.key }.toList()
        fun card(key: Long): Card? = collection.firstOrNull { (it.key and key) == it.key }
        fun create(rank: Rank, suit: Suit): Card {
            return Card(rank.key and suit.key, rank, suit, rank.code + suit.code)
        }
    }
    lateinit var remaining: List<Card>
    fun init():Card {
        this.remaining = collection.filter { it.key < this.key }
        return this
    }
}



