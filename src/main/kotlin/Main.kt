import domain.Card
import domain.Draw
import domain.Hand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    var count = 0L
    val work = scope.launch {
//        count = Hand.baseChildrenInit
        Hand.baseChildrenInit
            .map { it.baseChildren }
            .map { hands -> hands.flatMap { it.baseChildren } }
            .map { hands -> hands.flatMap { it.baseChildren } }
            .map { hands -> hands.flatMap { it.baseChildren } }
            .map { hands -> hands.flatMap { it.baseChildren } }
            .map { hands -> hands.flatMap { it.baseChildrenUpdate } }
            .flatMap { it }
//            .filter { it.draw.key ==  Draw.STRAIGHT_FLUSH.key}
            .groupBy { it.draw }
            .forEach { (draw, hands) ->
                launch {
                    println(
                        """
                        ${draw.name}  
                        ${hands.count().format} 
                        ${draw.sequence.format} 
                        
                        ${(hands.count() - draw.sequence).format}
                        
                        """.trimIndent())
                }
            }
//                .filter { it.draw.key == Draw.ROYAL_FLUSH.key }
//                .filter { it.baseKey == 1016L }
//                .filter { it.baseKey == 7183L }
//                .filter { it.flushBits >= 5 }
//                .filter { it.kindBits == 2 }
//                .filter { it.straightBits >= 5 }
//                .filter { (it.straightKey and it.flushKey).countOneBits() > 5 }
//            .take(1)
    }
//            .take(1)
//            .flatMap { hands -> hands.chunked(133_784_560 / 318_534).map { it.asSequence() } }
//            .map { hands -> hands.flatMap { it.childrenInit } }
//            .map { hands -> hands.flatMap { it.childrenPocket } }
//            .map { hands -> hands.flatMap { it.childrenInit } }
//            .map { hands -> hands.flatMap { it.children } }
//            .map { hands -> hands.flatMap { it.childrenFlop } }
//            .map { hands -> hands.flatMap { it.childrenTurns } }
//            .map { hands -> hands.flatMap { it.childrenRivers } }
//            .onEachIndexed { index, data -> launch { printSample(index, data, time) } }
//            .map { it.count().toLong() }
//            .sum()
//    }
    work.join()
    println("Count: ${count.format}  Elapsed: ${time.elapsedNow()}")
}

private fun printSample(
    index: Int, data: Sequence<Hand>, time: TimeSource.Monotonic.ValueTimeMark
) {
    val count = data.count().toLong()
    val totalCount = count * (index + 1)
    val first = data.first()
    val handKey = first.handKey
    val base = first.baseKey.code
    val parent = first.parentKey.code
    val hand = first.handKey.code
    val pocket = first.pocketKey.code
    val flop = first.flopKey.code
    val turn = first.turnKey.code
    val river = first.riverKey.code
    val flushKey = first.flushKey.code
    val straightKey = first.straightKey.code
    val straightFlushKey = (first.straightKey and first.flushKey).code
    val royalFlushKey = (first.royalFlushKey and first.flushKey).code
    val kindKey = first.kindKey.code
    val twoKindKey = first.twoKindKey.code
    val highCardKey = first.highCardKey.code

    println(
        """

        Index:          ${index.format}
        Elapsed:        ${time.elapsedNow()}
        Count:          ${count.format}
        Total Count:    ${totalCount.format}
        Key:            ${handKey}L
        Base:           $base ${first.baseKey}L
        Parent:         $parent
        Hand:           $hand
        Royal Flush:    $royalFlushKey
        Straight Flush: $straightFlushKey
        Flush:          $flushKey
        Straight:       $straightKey
        Kind:           $kindKey
        Two Kind:       $twoKindKey
        HighCard:       $highCardKey
        Draw:           [$pocket] [$flop] [$turn] [$river] ${first.draw}
                  
        """.trimIndent()
    )
}

val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

val Hand.childrenPocket: Sequence<Hand>
    get() = filteredEdges.map {
        copy(
            pocketKey = handKey.or(it.key), parentKey = parentKey.or(handKey).or(it.key), handKey = 0L
        )
    }
val Hand.childrenFlop: Sequence<Hand>
    get() = filteredEdges.map {
        copy(
            flopKey = handKey.or(it.key),
            parentKey = parentKey.or(handKey).or(it.key),
            handKey = 0L
        )
    }
private val Hand.childrenTurns: Sequence<Hand>
    get() = cards.map { copy(turnKey = it.key, parentKey = parentKey.or(it.key)) }

private val Hand.childrenRivers: Sequence<Hand>
    get() = cards.map { copy(riverKey = it.key, parentKey = parentKey.or(it.key)) }

private val Hand.Companion.baseChildrenInit: Sequence<Hand>
    get() = Card.collection.asSequence().map { Hand(card = it, baseKey = 0L) }
val Hand.children: Sequence<Hand>
    get() = filteredEdges.map { copy(handKey = handKey.or(it.key), card = it.cardOut) }

val Hand.baseChildren: Sequence<Hand>
    get() = edges.map { it.drawUpdate(this) }

val Hand.baseChildrenUpdate: Sequence<Hand>
    get() = edges.map { it.drawUpdate(this) }

val Hand.childrenInit: Sequence<Hand>
    get() = cards.map { copy(card = it, handKey = it.key) }

val Hand.cards: Sequence<Card>
    get() = Card.collection.filter { (baseKey and it.key) == it.key }.filter { (parentKey and it.key) == 0L }
        .asSequence()

val Hand.print: String
    get() {
        return """ 
            ************************************
            Key:     ${this.handKey}L
            Base:    ${this.baseKey.code}
            Parent:  ${this.parentKey.code}
            Hand:    ${this.handKey.code}
            Draw:    ${this.pocketKey.code} ${this.flopKey.code} ${this.turnKey.code} ${this.riverKey.code}
            pocket:  ${this.pocketKey.code}
            flop:    ${this.flopKey.code}
            Turn:    ${this.turnKey.code}
            River:   ${this.riverKey.code}
        """.trimIndent()
    }

val Hand.draw: Draw
    get() = when {
        royalFlushBits >= 5 -> Draw.ROYAL_FLUSH
        straightFlushBits >= 5 -> Draw.STRAIGHT_FLUSH
        kindBits == 4 -> Draw.QUADRUPLE
        twoKindBits == 5 -> Draw.FULL_HOUSE
        flushBits >= 5 -> Draw.FLUSH
        straightBits >= 5 -> Draw.STRAIGHT
        kindBits == 3 -> Draw.TRIPLE
        twoKindBits == 4 -> Draw.TWO_PAIR
        kindBits == 2 -> Draw.PAIR
        else -> Draw.HIGH_CARD
    }
