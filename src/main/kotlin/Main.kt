import domain.Card
import domain.Hand
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.DecimalFormat
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    val scope = CoroutineScope(Dispatchers.Default)
    val work = scope.async {
        Card.collection
            .map {
                scope.async {
                    val count = it.hands7
                        .flatMap { it.pocket }
                        .flatMap { it.flop }
                        .flatMap { it.turn }
                        .flatMap { it.river }
                        .count()
                        .toLong()
                    println("Card: ${it.code}  Count: ${count.format}  Elapsed: ${time.elapsedNow()}")
                    count
                }
            }
            .awaitAll()
            .sumOf { it }
    }
    work.join()
    println("Count: ${work.await().format}  Elapsed: ${time.elapsedNow()}")
//        .count()
//    val count = Card.collection[6].pocket2
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

