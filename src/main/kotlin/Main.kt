import domain.Hand
import domain.HandData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.time.Duration
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

suspend fun main() = runBlocking {
    val scope = CoroutineScope(Dispatchers.Default)
    var count = 0L
    var duration: Duration = Duration.ZERO
    val format = DecimalFormat("#,##0")
    val work = scope.launch {
        work().onEach { data ->
            launch {
                val timedValue = data.execute
                duration += timedValue.duration
                count += timedValue.value
                val rate = ((count.toDouble() / duration.inWholeMicroseconds) * 1_000_000)
                println("Rate: ${format.format(rate)} Duration: $duration Count: ${format.format(count)}")
            }
        }.count()
    }
    work.join()
}

private suspend fun work() = flow {
    baseHands.map { hands ->
        hands.flatMap { it.pockets }
            .flatMap { it.flops }
            .flatMap { it.turns }
            .flatMap { it.rivers }
    }.forEachIndexed { index, hands ->
        emit(HandData(index, hands))
    }
}

val HandData.execute: TimedValue<Long>
    get() = measureTimedValue { hands.count().toLong() }
private val baseHands: Sequence<Sequence<Hand>>
    get() = sequenceOf(Hand())
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .map { it.children() }
        .map { it.flatMap { hand -> hand.children() } }
        .map { it.flatMap { hand -> hand.children() } }
        .map { it.flatMap { hand -> hand.children() } }
        .filter { it.count() > 0 }
        .map { it.map { hand -> hand.copy(baseKey = hand.handKey, handKey = 0UL) } }


private val Hand.pockets: Sequence<Hand>
    get() = sequenceOf(Hand(parentKey = handKey, baseKey = baseKey))
        .flatMap { it.children() }
        .flatMap { it.children() }
        .map { copy(handKey = handKey or it.handKey, pocketKey = it.handKey) }

private val Hand.flops: Sequence<Hand>
    get() = sequenceOf(Hand(parentKey = handKey, baseKey = baseKey))
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .map { copy(handKey = handKey or it.handKey, flopKey = it.handKey) }

private val Hand.turns: Sequence<Hand>
    get() = sequenceOf(Hand(parentKey = handKey, baseKey = baseKey))
        .flatMap { it.children() }
        .map { copy(handKey = handKey or it.handKey, turnKey = it.handKey) }

private val Hand.rivers: Sequence<Hand>
    get() = sequenceOf(Hand(parentKey = handKey, baseKey = baseKey))
        .flatMap { it.children() }
        .map { copy(handKey = handKey or it.handKey, riverKey = it.handKey) }

fun Hand.children(): Sequence<Hand> = card.remaining
    .asSequence()
    .filter { (baseKey and it.key) == it.key }
    .filter { (parentKey and it.key) == 0UL }
    .map { copy(handKey = it.key or handKey, card = it) }

