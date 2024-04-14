import domain.*
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    val scope = CoroutineScope(Dispatchers.Default)
    val total = AtomicLong()
    val work = scope.launch {
        Card.collection.asSequence()
            .map { card ->
                async {
                    sequenceOf(Hand(index = 1, card = card))
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.childrenLast }

//                        .filter { it.baseKey.and(Rank._A.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._K.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._Q.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._J.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._T.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._9.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._8.key).countOneBits() == 1 }

//                        .filter { it.baseKey.and(Rank._A.key).and(Suit._S.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._K.key).and(Suit._S.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._Q.key).and(Suit._S.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._J.key).and(Suit._S.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._T.key).and(Suit._S.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._9.key).and(Suit._S.key).countOneBits() == 1 }
//                        .filter { it.baseKey.and(Rank._8.key).and(Suit._S.key).countOneBits() == 1 }

//                        .filter { it.baseKey.and(Rank._A.key).and(Suit._S.key) > 0 }
//                        .filter { it.baseKey.and(Rank._K.key).and(Suit._C.key) > 0 }
//                        .filter { it.baseKey.and(Rank._Q.key).and(Suit._D.key) > 0 }
//                        .filter { it.baseKey.and(Rank._J.key).and(Suit._H.key) > 0 }
//                        .filter { it.baseKey.and(Rank._T.key).and(Suit._S.key) > 0 }
//                        .filter { it.baseKey.and(Rank._9.key).and(Suit._C.key) > 0 }
//                        .filter { it.baseKey.and(Rank._8.key).and(Suit._D.key) > 0 }
//                        .filter { it.baseKey.and(Rank._7.key).and(Suit._H.key) > 0 }
                }
            }
            .forEach { data ->
                launch {
                    data.await().let { hands ->
                        hands.map {
                            val draw = when {
                                it.royalFlushKey >= 5 -> Draw.ROYAL_FLUSH
                                it.straightFlushBits >= 5 -> Draw.STRAIGHT_FLUSH
                                it.quadrupleBits == 4 -> Draw.QUADRUPLE
                                it.fullHouseBits >= 5 -> Draw.FULL_HOUSE
                                it.flushBits >= 5 -> Draw.FLUSH
                                it.straightBits >= 5 -> Draw.STRAIGHT
                                it.triplesBits == 3 -> Draw.TRIPLE
                                it.twoPairBits == 4 -> Draw.TWO_PAIR
                                it.pairsBits == 2 -> Draw.PAIR
                                else -> Draw.HIGH_CARD
                            }
                            draw.total.incrementAndGet()
                            it.copy(draw = draw)
                        }.forEach { _ -> }
                        total.addAndGet(hands.count().toLong())
                    }
                }
            }
    }
    work.join()
    Draw.collection
        .forEach { println(
            """
                Draw: ${it.name} 
                Count: ${it.total.toLong().format} 
                Sequence: ${it.sequence7.format} 
                Diff: ${(it.total.toLong() - it.sequence7).format}
                
                """.trimIndent()) }

    println("Count: ${total.toLong().format}  Elapsed: ${time.elapsedNow()}")
}

val Hand.drawHands: Sequence<Hand>
    get() = sequenceOf(this)
        .flatMap { it.pockets }
        .flatMap { it.flops }
        .flatMap { it.turns }
        .flatMap { it.rivers }

val Hand.children: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it, index = index + 1) }

val Hand.childrenLast: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it, index = index + 1, last = true) }

val Hand.rankKey: Long get() = baseKey.and(card.rank.key)
val Hand.rankBits: Int get() = rankKey.countOneBits()
val Hand.kindBits: Int get() = kindKey.countOneBits()
val Hand.kindKey: Long
    get() {
        if (index >= 2) {
            return parent?.let {
                if (it.card.rank.key != card.rank.key) {
                    if (it.rankBits >= 2) {
                        return it.rankKey
                    }
                } else if (last) {
                    if (rankBits >= 2) {
                        return rankKey
                    }
                }
                return 0L
            } ?: 0L
        }
        return 0L
    }
val Hand.royalFlushBits: Int get() = royalFlushKey.countOneBits()
val Hand.royalFlushKey: Long
    get() {
        if (index >= 5) {
            return parent?.let {
                val newRoyalFlushKey = baseKey.and(Rank.ROYAL_RANKS_KEY).and(card.suit.key)
                if (it.royalFlushBits < 5 && newRoyalFlushKey.countOneBits() >= 5) {
                    return newRoyalFlushKey
                }
                it.royalFlushKey
            } ?: 0L
        }
        return 0L
    }
val Hand.straightFlushBits: Int get() = straightFlushKey.countOneBits()
val Hand.straightFlushKey: Long
    get() {
        if (index >= 5) {
            return parent?.let {
                if (it.straightFlushBits < 5) {
                    val newStraightFlushKey = baseKey.and(card.rank.seriesKey).and(card.suit.key)
                    if (newStraightFlushKey.countOneBits() > it.straightFlushBits) {
                        return newStraightFlushKey
                    }
                }
                it.straightFlushKey
            } ?: 0L
        }
        return 0L
    }
val Hand.quadrupleBits: Int get() = quadrupleKey.countOneBits()
val Hand.quadrupleKey: Long
    get() {
        if (index >= 4) {
            return parent?.let {
                if (kindBits == 4 && it.quadrupleBits < 4) {
                    return it.quadrupleKey.or(kindKey)
                }
                return it.quadrupleKey
            } ?: 0L
        }
        return 0L
    }

val Hand.fullHouseBits: Int get() = fullHouseKey.countOneBits()
val Hand.fullHouseKey: Long
    get() {
        if (index >= 5) {
            return parent?.let {
                if (it.fullHouseBits < 5) {
                    return it.fullHouseKey.or(triplesKey).or(pairsKey)
                }
                return it.fullHouseKey
            } ?: 0L
        }
        return 0L
    }

val Hand.flushBits get() = flushKey.countOneBits()
val Hand.flushKey: Long
    get() {
        if (index >= 5) {
            return parent?.let {
                if (it.flushBits < 5) {
                    val newFlushKey = baseKey.and(card.suit.key)
                    if (newFlushKey.countOneBits() > it.flushBits) {
                        return newFlushKey
                    }
                }
                return it.flushKey
            } ?: 0L
        }
        return 0L
    }
val Hand.straightBits: Int get() = straightKey.countOneBits()
val Hand.straightKey: Long
    get() {
        val cardKey = card.key
        return parent?.let {
            val newStraightKey = it.baseKey.and(card.rank.seriesKey)
            if(it.straightBits < 5 && newStraightKey.countOneBits() >= 5) {
                return it.baseKey.and(card.rank.seriesKey)
            }
            it.straightKey
        } ?: cardKey
    }

val Hand.triplesBits: Int get() = triplesKey.countOneBits()
val Hand.triplesKey: Long
    get() {
        if (index >= 3) {
            return parent?.let {
                if (kindBits == 3 && it.triplesBits < 6) {
                    return it.triplesKey.or(kindKey)
                }
                return it.triplesKey
            } ?: 0L
        }
        return 0L
    }

val Hand.twoPairBits: Int get() = twoPairKey.countOneBits()
val Hand.twoPairKey: Long
    get() {
        if (index >= 4) {
            return parent?.let {
                if (it.twoPairBits < 4) {
                    return it.twoPairKey.or(pairsKey)
                }
                return it.twoPairKey
            } ?: 0L
        }
        return 0L
    }

val Hand.pairsBits: Int get() = pairsKey.countOneBits()
val Hand.pairsKey: Long
    get() {
        if (index >= 2) {
            return parent?.let {
                if (kindBits == 2 && it.pairsBits < 4) {
                    return it.pairsKey.or(kindKey)
                }
                return it.pairsKey
            } ?: 0L
        }
        return 0L
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