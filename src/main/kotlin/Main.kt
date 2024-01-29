import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant


const val SIZE = 7
suspend fun main() {
    cards.forEach { it.init() }
//    val start1 = Instant.now().toEpochMilli()
//    val cnt1 = generateSequence()/*.onEach { println(it.code()) }*/.count()
    val cnt1 = generateSequence()/*.onEach { println(it.code()) }*/.toList()
//    val end1 = Instant.now().toEpochMilli() - start1
//    println(cnt1)
//    println(end1)
}
suspend fun generateSequence(): Sequence<Hand> {
    return initGenerateInternal()
        .flatMap { generateSequenceInternal(it) }
        .flatMap { generateSequenceInternal(it) }
        .flatMap { generateSequenceInternal(it) }
        .flatMap { generateSequenceInternal(it) }
        .flatMap { generateSequenceInternal(it) }
        .flatMap { generateSequenceInternal(it) }
}
private fun generateSequenceInternal(parent: Hand): Sequence<Hand> {
    return parent.card.cards!!.asSequence().map { createHand(parent, it) }
}
private fun initGenerateInternal(): Sequence<Hand> {
    return cards.asSequence().map { Hand(null, it.key, it) }
}


//suspend fun List<Hand>.flatMap(mapper: suspend (Hand) -> List<Hand>): List<Hand> =
//    coroutineScope { map { async(Dispatchers.Default) { mapper(it) } }.awaitAll().flatten() }

data class Hand(val parent: Hand?, val key: Long, val card: Card)
data class Rank(val key: Long, val code: String)
data class Suit(val key: Long, val code: String)
data class Card(val key: Long, val rank: Rank, val suit: Suit, val code: String, var next: Card?, var cards: List<Card>?)

var ranks: List<Rank> = listOf(
    Rank(0b1111L shl (12 * 4), "A"),
    Rank(0b1111L shl (11 * 4), "K"),
    Rank(0b1111L shl (10 * 4), "Q"),
    Rank(0b1111L shl (9 * 4), "J"),
    Rank(0b1111L shl (8 * 4), "T"),
    Rank(0b1111L shl (7 * 4), "9"),
    Rank(0b1111L shl (6 * 4), "8"),
    Rank(0b1111L shl (5 * 4), "7"),
    Rank(0b1111L shl (4 * 4), "6"),
    Rank(0b1111L shl (3 * 4), "5"),
    Rank(0b1111L shl (2 * 4), "4"),
    Rank(0b1111L shl (1 * 4), "3"),
    Rank(0b1111L shl (0 * 4), "2"),
)

val baseSuitKey = 0b1000100010001000100010001000100010001000100010001000L
val suits: List<Suit> = listOf(
    Suit(baseSuitKey shr 0, "S"),
    Suit(baseSuitKey shr 1, "H"),
    Suit(baseSuitKey shr 2, "D"),
    Suit(baseSuitKey shr 3, "C"),
)

val cards: List<Card> =
    ranks.flatMap { rank -> suits.map { suit -> Card(rank.key and suit.key, rank, suit, rank.code + suit.code, null, null) } }
        .runningReduce{acc, card -> acc.next(card)}
val topCard: Card = cards[0]
fun Card.next(card: Card): Card {
    this.next = card
    return card
}

fun createHand(parent: Hand, card: Card): Hand {
    val key = parent.key or card.key
    return Hand(parent, key, card)
}

fun cards(key: Long): List<Card> = cards.asSequence().filter { (it.key and key) == it.key }.toList()
fun card(key: Long): Card? = cards.asSequence().filter { (it.key and key) == it.key }.firstOrNull()
fun Hand.code(): String = cards(this.key).map { it.code }.joinToString(" ")
fun Card.init(){ this.cards = generateSequence ( this.next ){ it.next }.toList()
}