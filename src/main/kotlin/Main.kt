import domain.Card
import domain.Draw
import domain.Hand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import kotlin.time.TimeSource

//var _52_7 = 133784560
//val baseKey = 0B1111111111111111111111111111111111111111111111111111L
//val baseKey = 0B111_1111L

@OptIn(ExperimentalUnsignedTypes::class)
suspend fun main() = runBlocking {
//    Rank.collection.map { it.next }.forEach { println(it) }
//    Hand.collection[0].edges.forEach { println(it) }
//    Card.collection[0].edges.forEach { println(it) }

//    Hand.collection.forEachIndexed() { index, hand -> hand.edges/*.filter { it.cardIn.rank.key == it.cardOut.rank.next.key }*/.forEach { println("$index ${it.cardIn.rank} ${it.cardIn.rank.next}") } }
//    Hand.allEdge.forEach { println(it) }
    execute2()
}

private suspend fun execute2() {
    val scope = CoroutineScope(Dispatchers.Default)
    val time = TimeSource.Monotonic.markNow()
    var count = 0
    val work = scope.launch {
         count =
//        Hand.baseHands
//            .chunked(10_000)
//             .onEach { hands ->  launch { printSample(hands) } }
//             .map { it.count().toLong() }
//             .reduce{acc, l ->  acc + l}

        hands().count()
//            .groupBy { it.draw }
//            .forEach { (draw, hands) -> printSample(draw, hands, time) }
    }
    work.join()
    println("Count: ${count.format}  Elapsed: ${time.elapsedNow()}")
}

fun hands() = flow {
    Hand.baseHands/*.chunked(10_000)*/.forEach { emit(it) }
}
private fun printSample2(
//    index: Int,
    draw: Draw,
    data: List<Hand>,
    time: TimeSource.Monotonic.ValueTimeMark
) {
    val count = data.count().toLong()
    val first = data.first()
    val base = first.baseKey.code
    val flushKey = first.flushKey.code
    val straightKey = first.straightKey.code
    val straightFlushKey = (first.straightKey and first.flushKey).code
//    val royalFlushKey = (first.royalFlushKey).code
    val kindKey = first.kindKey.code
//    val twoKindKey = first.twoKindKey.code
//    val highCardKey = first.highCardKey.code

    println(
        """

        Elapsed:        ${time.elapsedNow()}
        Count:          ${count.format}
        Base:           $base ${first.baseKey}L
        Royal Flush:    $ royalFlushKey
        Straight Flush: $straightFlushKey
        Flush:          $flushKey
        Straight:       $straightKey
        Kind:           $kindKey
        Two Kind:       $ twoKindKey
        HighCard:       $ highCardKey
        Draw:           ${draw.name}
                  
        """.trimIndent()
    )
}

private fun printSample(
//    index: Int,
    draw: Draw,
    data: List<Hand>,
    time: TimeSource.Monotonic.ValueTimeMark
) {
    val count = data.count().toLong()
//    val totalCount = count * (index + 1)
    val first = data.first()
//    val handKey = first.key
    val base = first.baseKey.code
    val parent = first.parentKey.code
//    val hand = first.key.code
//    val pocket = first.pocketKey.code
//    val flop = first.flopKey.code
//    val turn = first.turnKey.code
//    val river = first.riverKey.code
    val flushKey = first.flushKey.code
    val straightKey = first.straightKey.code
    val straightFlushKey = (first.straightFlushKey).code
    val royalFlushKey = (first.royalFlushKey).code
    val kindKey = first.kindKey.code
    val twoKindKey = first.twoKindKey.code
//    val highCardKey = first.highCardKey.code
//    val draw = first.draw;

    println(
        """

        Draw Type:      ${first.draw.name}
        Index:          $ {index.format}
        Elapsed:        ${time.elapsedNow()}
        Count:          ${count.format} Sequence: ${draw.sequence7.format} Diff: ${(count - draw.sequence7).format}
        Total Count:    $ {totalCount.format}
        Key:            $ {handKey}L
        Base:           $base ${first.baseKey}L
        Parent:         $parent
        Hand:           $ hand
        Royal Flush:    $royalFlushKey
        Straight Flush: $straightFlushKey
        Flush:          $flushKey
        Straight:       $straightKey
        Kind:           $kindKey
        Two Kind:       $twoKindKey
        HighCard:       $ highCardKey
        Draw:           [$ pocket] [$ flop] [$ turn] [$ river]
                  
        """.trimIndent()
    )
}

val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

val Hand.draw: Draw
    get() = when {
        royalFlushBits >= 5 -> Draw.ROYAL_FLUSH
        straightFlushBits >= 5 -> Draw.STRAIGHT_FLUSH
        wheelFlushBits >= 5 -> Draw.STRAIGHT_FLUSH
        kindBits == 4 -> Draw.QUADRUPLE
        twoKindBits == 5 -> Draw.FULL_HOUSE
        flushBits >= 5 -> Draw.FLUSH
        straightBits >= 5 -> Draw.STRAIGHT
        wheelBits >= 5 -> Draw.STRAIGHT
        kindBits == 3 -> Draw.TRIPLE
        twoKindBits == 4 -> Draw.TWO_PAIR
        kindBits == 2 -> Draw.PAIR
        else -> Draw.HIGH_CARD
    }
