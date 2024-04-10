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
//            .take(1)
            .map { card ->
                async {
                    sequenceOf(Hand(card))
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
//                        .take(1)
                }
            }
            .awaitAll()
            .asSequence()
            .flatten()
            .chunked(100_000)
            .map { hands ->
                async {
                    hands.asSequence()
                        .flatMap { it.drawsInit }
                        .flatMap { it.draws }
                        .flatMap { it.drawsInit }
                        .flatMap { it.draws }
                        .flatMap { it.draws }
                        .flatMap { it.drawsInit }
                        .flatMap { it.drawsInit }
                }
            }
            .forEach { hands ->
                launch {
                    hands.await().let { hands ->
                        val count = hands.count().toLong()
                        val first = hands.firstOrNull()
                        total.addAndGet(count)
                        println(
                            """
                        |Bass: ${first?.baseKey?.code} 
                        |Parent: ${first?.parentKey?.code} 
                        |Draw: ${first?.drawKey?.code} 
                        |Total: ${total.toLong().format}  
                        |Count: ${count.format}  
                        |Elapsed: ${time.elapsedNow()}
                        |
                        |""".trimMargin()
                        )
                    }
                }
            }
    }
    work.join()
    println("Count: ${total.toLong().format}  Elapsed: ${time.elapsedNow()}")
}

val Card.hand: Hand get() = Hand(card = this)
val Hand.children: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it) }

val Hand.cards: List<Card>
    get() = Card.collection
        .filter { baseKey.and(it.key) == it.key }
        .filter { parentKey.and(it.key) == 0L }

val Hand.drawsInit: Sequence<Hand>
    get() =
        cards.asSequence()
            .map {
                copy(
                    parent = this,
                    drawKey = it.key,
                    parentKey = parentKey.or(it.key),
                    card = it
                )
            }

val Hand.draws: Sequence<Hand>
    get() =
        cards.asSequence()
            .filter { it.key < card.key }
            .map {
                copy(
                    parent = this,
                    drawKey = drawKey.or(it.key),
                    parentKey = parentKey.or(it.key),
                    card = it
                )
            }


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