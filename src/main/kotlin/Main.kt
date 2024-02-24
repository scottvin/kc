import domain.Hand
import domain.HandData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.time.TimeSource
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

//var _52_7 = 133784560
suspend fun main() = runBlocking {
    val scope = CoroutineScope(Dispatchers.Default)
    val format = DecimalFormat("#,##0")
    val time = TimeSource.Monotonic.markNow()
    val work = scope.launch {
        println(
            work()
                .onEach { data ->
                    launch {
                        val timedValue = data.execute
                        val rate =
                            ((timedValue.value.toDouble() / timedValue.duration.inWholeNanoseconds) * 1_000_000_000)
                        println(
                            """

                    Rate: ${format.format(rate)}
                    Elapsed: ${time.elapsedNow()}
                    Duration: ${timedValue.duration}
                    Count: ${format.format(timedValue.value)}
                    
                    """.trimIndent()
                        )
                        timedValue.value
                    }
                }
                .count()
        )
    }
    work.join()
}

private suspend fun work() = flow {
    baseHands
        .map { hands ->
            hands.flatMap { it.pockets }
                .flatMap { it.flops }
                .flatMap { it.turns }
                .flatMap { it.rivers }
        }
        .forEachIndexed { index, hands ->
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
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .map { it.copy(baseKey = it.handKey, handKey = 0UL) }
        .chunked(22_100)
        .map { it.asSequence() }

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

