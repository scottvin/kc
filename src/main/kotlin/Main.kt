import domain.Hand
import domain.HandData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import java.text.DecimalFormat
import kotlin.time.TimeSource
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

//var _52_7 = 133784560
//val baseKey = 0B1111111111111111111111111111111111111111111111111111L
//val baseKey = 0B111_1111L

@OptIn(ExperimentalUnsignedTypes::class)
suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    execute()
    println(time.elapsedNow())
}

private suspend fun execute() {
    val scope = CoroutineScope(Dispatchers.Default)
    val format = DecimalFormat("#,##0")
    val time = TimeSource.Monotonic.markNow()
    val work = scope.launch {
        println(
            work()
                .onEach { data ->
                    launch {
                        val timedValue = data.execute
                        val count = timedValue.value.toDouble()
                        val duration = timedValue.duration.inWholeNanoseconds
                        val rate = ((count / duration) * 1_000_000_000)
                        println(
                            """

                            Rate: ${format.format(rate)}
                            Elapsed: ${time.elapsedNow()}
                            Duration: ${timedValue.duration}
                            Count: ${format.format(timedValue.value)}
                    
                            """.trimIndent()
                        )
                    }
                }
                .count()
        )
    }
    work.join()
}

private suspend fun work() = flow {
    hands()
        .chunked(7_264_320 / 420)
        .forEachIndexed { index, hands ->
            emit(HandData(index, hands))
        }
}

private fun hands() = sequenceOf(Hand())
    .flatMap { it.baseHands }
    .flatMap { it.pockets }
    .flatMap { it.flops }
    .flatMap { it.turns }
    .flatMap { it.rivers }


val HandData.execute: TimedValue<Long>
    get() = measureTimedValue { hands.count().toLong() }
private val Hand.baseHands: Sequence<Hand>
    get() = sequenceOf(Hand())
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .map { it.copy(baseKey = it.handKey, handKey = 0L) }

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
    .filter { (parentKey and it.key) == 0L }
    .map { copy(handKey = it.key or handKey, card = it) }

