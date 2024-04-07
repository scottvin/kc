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
        allHands
            .chunked(10_000_000)
            .toList()
            .map {
                scope.async {
                    val result = it.count()
                    println("Count: ${result.format}  Elapsed: ${time.elapsedNow()}")
                    result
                }
            }
            .awaitAll()
            .sumOf { it }
    }
    work.join()
    println("Count: ${work.await().format}  Elapsed: ${time.elapsedNow()}")
}


val allHands: Sequence<Hand>
    get() = Card.collection.asSequence()
        .map { Hand(it) }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.drawChildrenInit }
        .flatMap { it.drawChildren }
        .flatMap { it.drawChildrenInit }
        .flatMap { it.drawChildren }
        .flatMap { it.drawChildren }
        .flatMap { it.drawChildrenInit }
        .flatMap { it.drawChildrenInit }

val Hand.children: Sequence<Hand>
    get() = card.remaining.asSequence()
        .filter { key.and(it.key) == 0L }
        .map { copy(parent = this, card = it, key = it.key.or(key)) }

val Hand.drawChildrenInit: Sequence<Hand>
    get() = Card.collection.asSequence()
        .filter { (key.and(it.key) == it.key) && (drawKey.and(it.key) == 0L) }
        .map { copy(card = it, drawKey = it.key.or(drawKey)) }

val Hand.drawChildren: Sequence<Hand>
    get() = card.remaining.asSequence()
        .filter { (key.and(it.key) == it.key) && (drawKey.and(it.key) == 0L) }
        .map { copy(card = it, drawKey = it.key.or(drawKey)) }

val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

