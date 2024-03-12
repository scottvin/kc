import domain.Card
import domain.CardEdge
import domain.Hand
import domain.HandData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import java.text.DecimalFormat
import kotlin.time.TimeSource

//var _52_7 = 133784560
//val baseKey = 0B1111111111111111111111111111111111111111111111111111L
//val baseKey = 0B111_1111L

@OptIn(ExperimentalUnsignedTypes::class)
suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    execute()
//    println(Hand.baseHands.count())
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
                        val index = data.index
                        val count = data.hands.count()
                        val totalCount = count * (index + 1)
                        val elapsed = time.elapsedNow()
                        val duration = elapsed.div(totalCount)
                        val first = data.hands.first()
                        var handKey = first.handKey
                        var base = Card.code(first.baseKey)
                        var parent = Card.code(first.parentKey)
                        var hand = Card.code(first.handKey)
                        var pocket = Card.code(first.pocketKey)
                        var flop = Card.code(first.flopKey)
                        var turn = Card.code(first.turnKey)
                        var river = Card.code(first.riverKey)
                        println(
                            """

                            Index:        $index
                            Elapsed:      $elapsed
                            Duration:     $duration
                            Count:        ${format.format(count)}
                            Total Count:  ${format.format(totalCount)}
                            Key           ${handKey}L
                            Base          $base
                            Parent        $parent
                            Hand          $hand
                            Draw          [$pocket] [$flop] [$turn] [$river]
                  
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
//        .chunked(133_784_560 )
        .chunked(7_264_320 )
//        .chunked(7_264_320/420 )
        .forEachIndexed { index, hands -> emit(HandData(index, hands)) }
}

private fun hands() = Hand.baseHands
    .flatMap { it.pockets }
    .flatMap { it.flops }
    .flatMap { it.turns }
    .flatMap { it.rivers }


private val Hand.Companion.childrenInitAll: Sequence<Hand>
    get() = Card.collection.asSequence().map { Hand(card = it) }
val Hand.children: Sequence<Hand>
    get() = edges.map { copy(handKey =  handKey.or(it.key), card = it.cardOut) }
val Hand.baseFinal: Sequence<Hand>
    get() = edges.map { copy(baseKey =  handKey.or(it.key), handKey = 0L, card = it.cardOut) }

private val Hand.Companion.baseHands: Sequence<Hand>
    get() = childrenInitAll
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.children }
        .flatMap { it.baseFinal }

val Hand.childrenInit: Sequence<Hand>
    get() = cards.map { copy(card = it, handKey = it.key) }

val Hand.pocketFinal: Sequence<Hand>
    get() = edges.map { copy(pocketKey =  handKey.or(it.key), parentKey =  parentKey.or(handKey).or(it.key), handKey = 0L, card = it.cardOut) }

private val Hand.pockets: Sequence<Hand>
    get() = childrenInit
        .flatMap { it.pocketFinal }

val Hand.flopFinal: Sequence<Hand>
    get() = edges.map { copy(flopKey = handKey.or(it.key), parentKey =  parentKey.or(handKey).or(it.key), handKey = 0L, card = it.cardOut) }

private val Hand.flops: Sequence<Hand>
    get() = childrenInit
        .flatMap { it.children }
        .flatMap { it.flopFinal }

private val Hand.turns: Sequence<Hand>
    get() = cards.map { copy(turnKey = it.key, parentKey =  parentKey.or(it.key)) }

private val Hand.rivers: Sequence<Hand>
    get() = cards.map { copy(riverKey = it.key, parentKey =  parentKey.or(it.key)) }


val Hand.edges: Sequence<CardEdge> get() = card.edges
    .filter { (baseKey and it.key) == it.key }
    .filter { (parentKey and it.key) == 0L }
    .asSequence()
val Hand.cards: Sequence<Card>
    get() = Card.collection
        .filter { (baseKey and it.key) == it.key }
        .filter { (parentKey and it.key) == 0L }
        .asSequence()
