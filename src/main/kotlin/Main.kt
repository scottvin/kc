import domain.Card
import domain.Hand
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.time.TimeSource

suspend fun main() = runBlocking {
    execute()
}
private suspend fun execute() {
    val time = TimeSource.Monotonic.markNow()
//    val count = hands()
    val count = Hand.hands7
//        .take(1)
//        .onEach { println(it.key.code) }
        .count()
    println("Count: ${count.format}  Elapsed: ${time.elapsedNow()}")
}
fun hands() = flow {
    Hand.hands7/*.chunked(10_000)*/.forEach { emit(it) }
}
val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

