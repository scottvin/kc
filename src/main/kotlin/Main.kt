import domain.Card
import domain.Hand
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    val scope = CoroutineScope(Dispatchers.Default)
    var total = AtomicLong()
    val work = scope.launch {
        Card.collection
            .map { card ->
                async {
                    sequenceOf(Hand(card))
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                }
            }.awaitAll()
            .map { hands ->
                async {
                    hands
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                }
            }.awaitAll()
            .flatMap { it }
            .map { hand ->
                async {
                    sequenceOf(hand)
                        .flatMap { it.drawsInit }
                        .flatMap { it.draws }
                        .flatMap { it.drawsInit }
                        .flatMap { it.draws }
                        .flatMap { it.draws }
                        .flatMap { it.drawsInit }
                        .flatMap { it.drawsInit }
                }
            }.awaitAll()
            .filter { it.count() > 0 }
            .forEach { hands ->
                launch {
                    val count = hands.count().toLong()
                    val first = hands.firstOrNull()
                    total.addAndGet(count)
                    println(
                        """
                        |First: ${first?.baseKey?.code} 
                        |First: ${first?.parentKey?.code} 
                        |Total: ${total.toLong().format}  
                        |Count: ${count.format}  
                        |Elapsed: ${time.elapsedNow()}
                        |
                        |""".trimMargin()
                    )
                }
            }
    }
    work.join()
    println("Count: ${total.toLong().format}  Elapsed: ${time.elapsedNow()}")
}

val Card.hand: Hand get() = Hand(card = this)
val Hand.children: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(baseKey = baseKey.or(it.key), card = it) }

val Hand.drawsInit: Sequence<Hand>
    get() = Card.collection.asSequence()
        .filter { baseKey.and(it.key) == it.key }
        .filter { parentKey.and(it.key) == 0L }
        .map { copy(drawKey = it.key, parentKey = parentKey.or(it.key), card = it) }

val Hand.draws: Sequence<Hand>
    get() = card.remaining.asSequence()
        .filter { baseKey.and(it.key) == it.key }
        .filter { parentKey.and(it.key) == 0L }
        .filter { it.key < card.key }
        .map { copy(drawKey = drawKey.or(it.key), parentKey = parentKey.or(it.key), card = it) }

val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

val Hand.creator: (Hand) -> Sequence<Card>
    get() = {
        card.remaining.asSequence()
            .filter { baseKey.and(it.key) == it.key }
            .filter { parentKey.and(it.key) == 0L }
            .filter { it.key < card.key }
    }