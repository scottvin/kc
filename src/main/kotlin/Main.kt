import domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.time.TimeSource

suspend fun main() = runBlocking {
    execute()
}
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun execute() {
    val time = TimeSource.Monotonic.markNow()
    val count = Hand.hands7.count()
    println("Count: ${count.format}  Elapsed: ${time.elapsedNow()}")
}
fun hands():Flow<Hand> = flow {
    Hand.hands7.forEach { emit(it) }
}
val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

