import domain.Card
import domain.Draw
import domain.Hand
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    val scope = CoroutineScope(Dispatchers.Default)
    var total = AtomicLong()
    val work = scope.launch {
        Card.collection
//            .take(1)
            .map { card ->
                async {
                    sequenceOf(Hand(card))
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
//                        .filter { it.straightKey.countOneBits() < 5 && it.straightFlushKey.countOneBits() < 5 && it.kindKey.countOneBits() == 0 }
//                        .take(1)
                }
            }
//            .map { hands ->
//                async {
//                    hands.await()
//                        .filter {
////                            it.kindKey.countOneBits() == 4
////                                    it.twoKindKey.countOneBits() != 4 &&
//                                    it.straightKey.countOneBits() == 5
////                                    it.straightFlushKey.countOneBits() < 5
////                                    it.flushKey.countOneBits() < 5
//                        }
//                }
//            }
            .awaitAll()
            .asSequence()
            .flatten()
            .chunked(125_000)
            .map { hands ->
                async {
                    hands.asSequence()
                        .map { if(it.straightKey.countOneBits() == 5) it.copy(draw = Draw.STRAIGHT) else it}
                }
            }
            .map { hands ->
                async {
                    hands.await()
                        .flatMap { it.pockets }
                        .flatMap { it.flops }
                        .flatMap { it.turns }
                        .flatMap { it.rivers }
                }
            }
            .forEach { hands ->
                launch {
                    hands.await()
                        .let { hands ->
                            val count = hands.count().toLong()
                            total.addAndGet(count)
                            val first = hands.firstOrNull()
                            first?.print
                            println(
                                """
                                    |Total:   ${total.toLong().format}
                                    |Count:   ${count.format}
                                    |Elapsed: ${time.elapsedNow()}
                                    |
                                """.trimMargin()
                            )
                        }
                }
            }
    }
    work.join()
    println("Count: ${total.toLong().format}  Elapsed: ${time.elapsedNow()}")

}

val Hand.children: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map {
            copy(
                parent = this,
                baseKey = baseKey.or(it.key),
                card = it,
            )
        }
//val Hand.children: Sequence<Hand>
//    get() = card.edge.asSequence()
//        .map {
//            copy(
//                parent = this,
//                baseKey = baseKey.or(it.cardOut.key),
//                card = it.cardOut,
//            )
//        }
//val Hand.straightFlushKey: Long
//    get() {
//        return parent?.let { baseKey.and(card.rank.seriesKey).and(card.suit.key) } ?: 0L
//    }
val Hand.straightFlushKey: Long
    get() {
        return parent?.let {
            val newStraightFlushKey = baseKey.and(card.rank.seriesKey).and(card.suit.key)
            return if (it.straightFlushKey.countOneBits() >= newStraightFlushKey.countOneBits()) it.straightFlushKey
            else newStraightFlushKey
        } ?: 0L
    }

val Hand.flushKey: Long
    get() {
        return parent?.let {
            val newFlushKey = baseKey.and(card.suit.key)
            if (it.flushKey.countOneBits() >= newFlushKey.countOneBits()) it.flushKey else newFlushKey
        } ?: 0L
    }

val Hand.kindKey: Long
    get() {
        return parent?.let {
            val newKindKey = baseKey.and(card.rank.key)
            if (newKindKey.countOneBits() < 2 || it.kindKey.countOneBits() >= newKindKey.countOneBits()) it.kindKey else newKindKey
        } ?: 0L
    }

val Hand.twoKindKey: Long
    get() {
        return parent?.let {
            val newKindKey = baseKey.and(card.rank.key)
            if(newKindKey.countOneBits() >= 2) {
                val newTwoKindKey = it.twoKindKey.or(baseKey.and(card.rank.key))
                if (it.twoKindKey.countOneBits() >= newTwoKindKey.countOneBits()) it.twoKindKey else newTwoKindKey
            } else it.twoKindKey
        } ?: 0L
    }

val Hand.straightKey: Long
    get() {
        val cardKey = card.key
        return parent?.let {
            val newStraightKey = it.straightKey.or(it.card.rank.next.key.and(cardKey)).and(card.rank.seriesKey)
            if (it.straightKey.countOneBits() >= newStraightKey.countOneBits()) it.straightKey else newStraightKey
        } ?: cardKey
    }

val Hand.initDrawCards: List<Card>
    get() = Card.collection
        .filter { baseKey.and(it.key) == it.key }
        .filter { parentKey.and(it.key) == 0L }

val Hand.drawCards: List<Card>
    get() = initDrawCards.filter { it.key < card.key }

val Hand.drawsInit: Sequence<Hand>
    get() = initDrawCards.asSequence()
        .map { copy(parent = this, drawKey = it.key, parentKey = parentKey.or(it.key), card = it) }

val Hand.draws: Sequence<Hand>
    get() = drawCards.asSequence()
        .map { copy(parent = this, drawKey = drawKey.or(it.key), parentKey = parentKey.or(it.key), card = it) }

val Hand.pockets: Sequence<Hand>
    get() = sequenceOf(this)
        .flatMap { it.drawsInit }
        .flatMap { it.draws }
        .map { it.copy(pocketKey = it.drawKey) }

val Hand.flops: Sequence<Hand>
    get() = sequenceOf(this)
        .flatMap { it.drawsInit }
        .flatMap { it.draws }
        .flatMap { it.draws }
        .map { it.copy(flopKey = it.drawKey) }

val Hand.turns: Sequence<Hand>
    get() = sequenceOf(this)
        .flatMap { it.drawsInit }
        .map { it.copy(turnKey = it.drawKey) }

val Hand.rivers: Sequence<Hand>
    get() = sequenceOf(this)
        .flatMap { it.drawsInit }
        .map { it.copy(riverKey = it.drawKey) }

val Hand.print: Unit
    get() = println(
        """
            |
            |Bass:          ${baseKey.code} 
            |Parent:        ${parentKey.code} 
            |Draw:          ${drawKey.code} 
            |Pocket:        ${pocketKey.code} 
            |Flow:          ${flopKey.code} 
            |Turn:          ${turnKey.code} 
            |River:         ${riverKey.code} 
            |TwoKind        ${twoKindKey.code}
            |kind           ${kindKey.code}
            |flush          ${flushKey.code}
            |straight       ${straightKey.code}
            |straightFlush  ${straightFlushKey.code}
        |""".trimMargin()
    )

val Long.cards: List<Card> get() = Card.collection.filter { (it.key and this) == it.key }
val Long.code: String get() = cards.joinToString(" ") { c -> c.code }
val formatLong = DecimalFormat("#,##0")
val Long.format: String get() = formatLong.format(this)
val Int.format: String get() = formatLong.format(this)

val Hand.creator: (Hand) -> Sequence<Card>
    get() = {
        card.remaining.asSequence()
            .filter { baseKey.and(it.key) == it.key }
            .filter { parentKey.and(it.key) == 0L }
            .filter { it.key < card.key }
    }