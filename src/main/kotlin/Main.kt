import domain.Card
import domain.CardEdge
import domain.Hand
import domain.HandData
import kotlinx.coroutines.*
import java.text.DecimalFormat
import kotlin.time.TimeSource

//var _52_7 = 133784560
//val baseKey = 0B1111111111111111111111111111111111111111111111111111L
//val baseKey = 0B111_1111L

@OptIn(ExperimentalUnsignedTypes::class)
suspend fun main() = runBlocking {
//    Hand.collection[50].edges.forEach { println(it) }
//    Hand.allEdge.forEach { println(it) }
    execute2()
}

private suspend fun execute2() {
    val scope = CoroutineScope(Dispatchers.Default)
    val format = DecimalFormat("#,##0")
    val time = TimeSource.Monotonic.markNow()
    var count = 0L
    val work = scope.launch {
        count = Hand.childrenInitAll
            .map { it.children }
            .map { hands -> hands.flatMap { it.children } }
            .map { hands -> hands.flatMap { it.children } }
            .map { hands -> hands.flatMap { it.children } }
            .map { hands -> hands.flatMap { it.children } }
            .map { hands -> hands.flatMap { it.childrenBase }.take(1) }
            .flatMap { hands -> hands.chunked(133_784_560 / 318_534).map { it.asSequence() }  }
            .map { hands -> hands.flatMap { it.childrenInit } }
            .map { hands -> hands.flatMap { it.childrenPocket } }
            .map { hands -> hands.flatMap { it.childrenInit } }
            .map { hands -> hands.flatMap { it.children } }
            .map { hands -> hands.flatMap { it.childrenFlop } }
            .map { hands -> hands.flatMap { it.childrenTurns } }
            .map { hands -> hands.flatMap { it.childrenRivers } }
            .map {it.count().toLong()}
            .sum()
//            .onEachIndexed { index, data -> launch { printSample(index, data, time, format) } }
//            .flatMap { it }
    }
    work.join()
    println("Count: ${format.format(count)}  Elapsed: ${time.elapsedNow()}")
}

private fun printSample(
    index: Int,
    data: Sequence<Hand>,
    time: TimeSource.Monotonic.ValueTimeMark,
    format: DecimalFormat
) {
    val count = data.count()
    val totalCount = count * (index + 1)
    val elapsed = time.elapsedNow()
    val first = data.first()
    val handKey = first.handKey
    val base = Card.code(first.baseKey)
    val parent = Card.code(first.parentKey)
    val hand = Card.code(first.handKey)
    val pocket = Card.code(first.pocketKey)
    val flop = Card.code(first.flopKey)
    val turn = Card.code(first.turnKey)
    val river = Card.code(first.riverKey)
    println(
        """

        Index:        ${format.format(index)}
        Elapsed:      $elapsed
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

private fun printAll(
    data: HandData,
    time: TimeSource.Monotonic.ValueTimeMark,
    format: DecimalFormat
) {
    data.hands.forEach {
        val handKey = it.handKey
        val base = Card.code(it.baseKey)
        val parent = Card.code(it.parentKey)
        val hand = Card.code(it.handKey)
        val pocket = Card.code(it.pocketKey)
        val flop = Card.code(it.flopKey)
        val turn = Card.code(it.turnKey)
        val river = Card.code(it.riverKey)
        println(
            """
        Key           ${handKey}L
        Base          $base
        Parent        $parent
        Hand          $hand
        Draw          [$pocket] [$flop] [$turn] [$river]
                  
        """.trimIndent()
        )
    }
}

val Hand.childrenBase: Sequence<Hand>
    get() = edges.map { copy(baseKey = handKey.or(it.key), handKey = 0L) }
val Hand.childrenPocket: Sequence<Hand>
    get() = edges.map { copy(pocketKey = handKey.or(it.key), parentKey = parentKey.or(handKey).or(it.key), handKey = 0L) }
val Hand.childrenFlop: Sequence<Hand>
    get() = edges.map { copy(flopKey = handKey.or(it.key), parentKey = parentKey.or(handKey).or(it.key), handKey = 0L) }
private val Hand.childrenTurns: Sequence<Hand>
    get() = cards.map { copy(turnKey = it.key, parentKey = parentKey.or(it.key)) }

private val Hand.childrenRivers: Sequence<Hand>
    get() = cards.map { copy(riverKey = it.key, parentKey = parentKey.or(it.key)) }

private val Hand.Companion.childrenInitAll: Sequence<Hand>
    get() = Card.collection.asSequence().map { Hand(card = it) }
val Hand.children: Sequence<Hand>
    get() = edges.map { copy(handKey = handKey.or(it.key), card = it.cardOut) }

val Hand.childrenInit: Sequence<Hand>
    get() = cards.map { copy(card = it, handKey = it.key) }

val Hand.edges: Sequence<CardEdge>
    get() = card.edges
        .filter { (baseKey and it.key) == it.key }
        .filter { (parentKey and it.key) == 0L }
        .asSequence()

val Hand.cards: Sequence<Card>
    get() = Card.collection
        .filter { (baseKey and it.key) == it.key }
        .filter { (parentKey and it.key) == 0L }
        .asSequence()
