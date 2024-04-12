import domain.Card
import domain.Draw
import domain.Hand
import domain.Rank
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
                        .flatMap { it.childrenLast }
//                        .filter { it.straightKey.countOneBits() < 5 && it.straightFlushKey.countOneBits() < 5 && it.kindKey.countOneBits() == 0 }
//                        .filter { hand -> hand.baseKey.and(Rank._A.key).countOneBits() == 1 }
//                        .filter { hand -> hand.baseKey.and(Rank._K.key).countOneBits() == 1 }
//                        .filter { hand -> hand.baseKey.and(Rank._Q.key).countOneBits() == 1 }
//                        .filter { hand -> hand.baseKey.and(Rank._J.key).countOneBits() == 1 }
//                        .filter { hand -> hand.baseKey.and(Rank._T.key).countOneBits() == 1 }
//                        .filter { hand -> hand.baseKey.and(Rank._9.key).countOneBits() == 2 }
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
                        .map {
                            val key = when {
                                it.straightFlushKey.countOneBits() >= 5 -> it.copy(draw = Draw.STRAIGHT_FLUSH)
                                it.quadrupleKey.countOneBits() == 4 -> it.copy(draw = Draw.QUADRUPLE)
                                it.fullHouseKey.countOneBits() >= 5 -> it.copy(draw = Draw.FULL_HOUSE)
                                it.flushKey.countOneBits() >= 5 -> it.copy(draw = Draw.FLUSH)
                                it.triplesKey.countOneBits() == 3 -> it.copy(draw = Draw.TRIPLE)
                                it.twoPairKey.countOneBits() == 4 -> it.copy(draw = Draw.TWO_PAIR)
                                it.pairsKey.countOneBits() == 2 -> it.copy(draw = Draw.PAIR)
                                else -> it
                            }
                            key
                        }
                }
            }
//            .map { hands ->
//                async {
//                    hands.await()
//                        .flatMap { it.pockets }
//                        .flatMap { it.flops }
//                        .flatMap { it.turns }
//                        .flatMap { it.rivers }
//                }
//            }
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
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it) }

val Hand.childrenLast: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it, last = true) }

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
            if(it.straightFlushKey.countOneBits() < 5) {
                val newStraightFlushKey = baseKey.and(card.rank.seriesKey).and(card.suit.key)
                if (newStraightFlushKey.countOneBits() > it.straightFlushKey.countOneBits()) {
                    return newStraightFlushKey
                }
            }
            it.straightFlushKey
        } ?: 0L
    }

val Hand.flushKey: Long
    get() {
        return parent?.let {
            if (it.flushKey.countOneBits() < 5) {
                val newFlushKey = baseKey.and(card.suit.key)
                if (newFlushKey.countOneBits() > it.flushKey.countOneBits()) {
                    return newFlushKey
                }
            }
            return it.flushKey
        } ?: 0L
    }

val Hand.kindKey: Long
    get() {
        return parent?.let {
            if (it.card.rank.key != card.rank.key) {
                val kindKey = it.baseKey.and(it.card.rank.key)
                if (kindKey.countOneBits() >= 2) {
                    return kindKey
                }
            } else if (last) {
                val kindKey = baseKey.and(card.rank.key)
                if (kindKey.countOneBits() >= 2) {
                    return kindKey
                }

            }
            return it.kindKey
        } ?: 0L
    }
val Hand.pairsKey: Long
    get() {
        return parent?.let {
            if (kindKey.countOneBits() == 2 && it.pairsKey.countOneBits() < 4) {
                return it.pairsKey.or(kindKey)
            }
            return it.pairsKey
        } ?: 0L
    }

val Hand.triplesKey: Long
    get() {
        return parent?.let {
            if (kindKey.countOneBits() == 3 && it.triplesKey.countOneBits() < 6) {
                return it.triplesKey.or(kindKey)
            }
            return it.triplesKey
        } ?: 0L
    }

val Hand.quadrupleKey: Long
    get() {
        return parent?.let {
            if (kindKey.countOneBits() == 4 && it.quadrupleKey.countOneBits() < 4) {
                return it.quadrupleKey.or(kindKey)
            }
            return it.quadrupleKey
        } ?: 0L
    }

val Hand.fullHouseKey: Long
    get() {
        return parent?.let {
            if (it.fullHouseKey.countOneBits() < 5) {
                return it.fullHouseKey.or(triplesKey).or(pairsKey)
            }
            return it.fullHouseKey
        } ?: 0L
    }

val Hand.twoPairKey: Long
    get() {
        return parent?.let {
            if (it.twoPairKey.countOneBits() < 4) {
                return it.twoPairKey.or(pairsKey)
            }
            return it.twoPairKey
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
            |Draw:          ${draw.name} 
            |Bass:          ${baseKey.code} 
            |Parent:        ${parentKey.code} 
            |Draw:          ${drawKey.code} 
            |Pocket:        ${pocketKey.code} 
            |Flow:          ${flopKey.code} 
            |Turn:          ${turnKey.code} 
            |River:         ${riverKey.code} 
            |Pairs          ${pairsKey.code}
            |Triples        ${triplesKey.code}
            |Quadruple      ${quadrupleKey.code}
            |Full House     ${fullHouseKey.code}
            |Two Pair       ${twoPairKey.code}
            |Flush          ${flushKey.code}
            |Straight       ${straightKey.code}
            |Straight Flush ${straightFlushKey.code}
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