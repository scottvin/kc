import domain.Card
import domain.Draw
import domain.Hand
import domain.Rank
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.TimeSource

suspend fun main() = runBlocking {
    val time = TimeSource.Monotonic.markNow()
    val scope = CoroutineScope(Dispatchers.Default)
    val total = AtomicLong()
    val work = scope.launch {
        Card.collection
            .map { card ->
                async {
                    Hand(index = 1, card = card).children
                }
            }
            .map { data ->
                async {
                    data.await()
                        .flatMap { it.children }
                }
            }
            .map { data ->
                async {
                    data.await()
                        .flatMap { it.children }
                }
            }
            .map { data ->
                async {
                    data.await()
                        .flatMap { it.children }
                }
            }
            .map { data ->
                async {
                    data.await()
                        .flatMap { it.children }
                }
            }
            .map { data ->
                async {
                    data.await()
                        .flatMap { it.childrenLast }
                }
            }
            .forEach {
                launch {
                    total.addAndGet(it.await().count().toLong())
                }
            }
//            .forEach { hand ->
//                launch {
//                    sequenceOf(hand)
//                        .flatMap { it.childrenLast }
//                        .map { it.drawHands }
//                        .flatMap { it.rivers }
//                        .flatMap { it.turns }
//                        .flatMap { it.flops }
//                        .flatMap { it.pockets }
//                        .forEach { _ -> }
//                }
//            }
    }
    work.join()
    val elapsedNow = time.elapsedNow()
    println("Count: ${total.toLong().format}  Elapsed: $elapsedNow")
}

val Hand.drawHands: Hand
    get() = when {
        royalFlushBits >= 5 -> copy(draw = Draw.ROYAL_FLUSH)
        straightFlushBits >= 5 -> copy(draw = Draw.STRAIGHT_FLUSH)
        wheelFlushBits >= 5 -> copy(draw = Draw.STRAIGHT_FLUSH)
        quadrupleBits == 4 -> copy(draw = Draw.QUADRUPLE)
        fullHouseBits >= 5 -> copy(draw = Draw.FULL_HOUSE)
        flushBits >= 5 -> copy(draw = Draw.FLUSH)
        straightBits >= 5 -> copy(draw = Draw.STRAIGHT)
        wheelBits >= 5 -> copy(draw = Draw.STRAIGHT)
        triplesBits == 3 -> copy(draw = Draw.TRIPLE)
        twoPairBits == 4 -> copy(draw = Draw.TWO_PAIR)
        pairsBits == 2 -> copy(draw = Draw.PAIR)
        else -> this
    }


val Hand.children: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it, index = index + 1) }

val Hand.childrenLast: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it, index = index + 1, last = true) }

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
val Hand.wheelFlushBits: Int get() = wheelFlushKey.countOneBits()
val Hand.wheelFlushKey: Long
    get() {
        if (index >= 5) {
            return parent?.let {
                val newWheelFlushKey = baseKey.and(Rank.WHEEL_RANKS_KEY).and(card.suit.key)
                if (it.wheelFlushBits < 5 && newWheelFlushKey.countOneBits() >= 5) {
                    return newWheelFlushKey
                }
                it.wheelFlushKey
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
                val kindKey = baseKey.and(card.rank.key)
                if (kindKey.countOneBits() == 4 && it.quadrupleBits < 4) {
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
        if (index >= 5) {
            val cardKey = card.key
            return parent?.let {
                val newStraightKey = baseKey.and(card.rank.seriesKey)
                if (it.straightBits < 5 && newStraightKey.countOneBits() >= 5) {
                    return card.rank.series[0].key.and(baseKey).takeHighestOneBit()
                        .or(card.rank.series[1].key.and(baseKey).takeHighestOneBit())
                        .or(card.rank.series[2].key.and(baseKey).takeHighestOneBit())
                        .or(card.rank.series[3].key.and(baseKey).takeHighestOneBit())
                        .or(card.rank.series[4].key.and(baseKey).takeHighestOneBit())
                }
                it.straightKey
            } ?: cardKey
        }
        return 0L
    }

val Hand.wheelBits: Int get() = wheelKey.countOneBits()
val Hand.wheelKey: Long
    get() {
        if (index >= 5) {
            val cardKey = card.key
            return parent?.let {
                val newWheelKey = baseKey.and(Rank.WHEEL_RANKS_KEY)
                if (it.wheelBits < 5 && newWheelKey.countOneBits() >= 5) {
                    return Rank.WHEEL_RANKS[0].key.and(baseKey).takeHighestOneBit()
                        .or(Rank.WHEEL_RANKS[1].key.and(baseKey).takeHighestOneBit())
                        .or(Rank.WHEEL_RANKS[2].key.and(baseKey).takeHighestOneBit())
                        .or(Rank.WHEEL_RANKS[3].key.and(baseKey).takeHighestOneBit())
                        .or(Rank.WHEEL_RANKS[4].key.and(baseKey).takeHighestOneBit())
                }
                it.wheelKey
            } ?: cardKey
        }
        return 0L
    }

val Hand.triplesBits: Int get() = triplesKey.countOneBits()
val Hand.triplesKey: Long
    get() {
        if (index >= 3) {
            return parent?.let {
                val kindKey = baseKey.and(card.rank.key)
                if (it.triplesBits < 6 && kindKey.countOneBits() == 3) {
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
                val kindKey = baseKey.and(card.rank.key)
                if (it.pairsBits < 4 && kindKey.countOneBits() == 2) {
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
        """|Draw:          ${draw.name}
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
