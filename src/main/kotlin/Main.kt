import domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    val count = Card.allHands()
//        .take(1)
//        .onEach { println("${it.key.code}") }
        .flatMap { it.pocket }
//        .onEach { println("${it.drawKey.code}") }
        .flatMap { it.flop }
//        .onEach { println("${it.drawKey.code}") }
        .flatMap { it.turn }
//        .onEach { println("${it.drawKey.code}") }
        .flatMap { it.river }
//        .onEach { println(it.key.code) }
        .count()
//    val count = Card.collection[6].pocket2
    println("Count: ${count.format}  Elapsed: ${time.elapsedNow()}")
}

fun hands(): Flow<Hand> = flow {
//    Card.allHands().forEach { emit(it) }
}

fun Card.Companion.allHands(): Sequence<Hand> = collection.asSequence().hands
val Card.hand: Hand get() = Hand(card = this)
val Card.cards: Sequence<Card> get() = remaining.asSequence()
val Card.hands2: Sequence<Hand> get() = hand.children
val Card.hands3: Sequence<Hand> get() = hands2.children
val Card.hands4: Sequence<Hand> get() = hands3.children
val Card.hands5: Sequence<Hand> get() = hands4.children
val Card.hands6: Sequence<Hand> get() = hands5.children
val Card.hands7: Sequence<Hand> get() = hands6.children//.take(1)

val Hand.pocket1: Sequence<Hand> get() = drawChildrenInit
val Hand.pocket: Sequence<Hand> get() = pocket1.drawChildren

val Hand.flop1: Sequence<Hand> get() = drawChildrenInit
val Hand.flop2: Sequence<Hand> get() = flop1.drawChildren
val Hand.flop: Sequence<Hand> get() = flop2.drawChildren

val Hand.turn: Sequence<Hand> get() = drawChildrenInit
val Hand.river: Sequence<Hand> get() = drawChildrenInit

val Sequence<Card>.hands: Sequence<Hand> get() = flatMap { it.hands7 }


val Hand.children: Sequence<Hand> get() = card.cards.filter { filter(it) }.map { create(it) }
val Hand.filter: (Card) -> Boolean get() = { key.and(it.key) == 0L }
val Hand.create: (Card) -> Hand get() = { copy(parent = this, card = it, key = it.key.or(key)) }
val Sequence<Hand>.children: Sequence<Hand> get() = flatMap { it.children }

val Hand.drawChildrenInit: Sequence<Hand>
    get() = Card.collection.asSequence().filter { drawFilter(it) }.map { drawCreate(it) }
val Hand.drawChildren: Sequence<Hand> get() = card.cards.filter { drawFilter(it) }.map { drawCreate(it) }
val Hand.drawFilter: (Card) -> Boolean get() = { (key.and(it.key) == it.key) && (drawKey.and(it.key) == 0L) }
val Hand.drawCreate: (Card) -> Hand get() = { copy(card = it, drawKey = it.key.or(drawKey)) }
val Sequence<Hand>.drawChildren: Sequence<Hand> get() = flatMap { it.drawChildren }
val Sequence<Hand>.drawChildrenInit: Sequence<Hand> get() = flatMap { it.drawChildrenInit }


val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

