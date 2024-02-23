import domain.Hand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTimedValue

suspend fun main() = runBlocking {
    work()
}

private suspend fun work2() {
    println(
        measureTimedValue {
            Hand().baseHands.count()
        }
    )
}

private suspend fun work() {
    val scope = CoroutineScope(Dispatchers.Default)
    println(
        measureTimedValue {
            val work = scope.launch {
                Hand().baseHands
                    .forEach {
                        launch {
                            generatorHands(it).count()
                        }
                    }
            }
            work.join()
        }
    )
}

fun generatorHands(baseHand: Hand): Sequence<Hand> =
    sequenceOf(baseHand)
        .flatMap { it.pockets }
        .flatMap { it.flops }
        .flatMap { it.turns }
        .flatMap { it.rivers }


private val Hand.baseHands: Sequence<Hand>
    get() = sequenceOf(Hand())
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }//.take(1)
        .flatMap { it.children() }
        .flatMap { it.children() }
        .flatMap { it.children() }
        .map { it.copy(baseKey = it.handKey, handKey = 0UL) }

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

