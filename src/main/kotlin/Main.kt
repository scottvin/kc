import domain.Card
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
            .take(1)
            .map { card ->
                async {
                    sequenceOf(Hand(card))
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .flatMap { it.children }
                        .take(1)
                }
            }
            .awaitAll()
            .asSequence()
            .flatten()
            .chunked(12_500)
            .map { hands ->
                async {
                    hands.asSequence()
                        .flatMap { it.pockets }
                        .flatMap { it.flops }
                        .flatMap { it.turns }
                        .flatMap { it.rivers }
                }
            }
            .forEach { hands ->
                launch {
                    hands.await().let { hands ->
                        val count = hands.count().toLong()
                        val first = hands.firstOrNull()
                        total.addAndGet(count)
                        println(
                            """
                        |Bass:    ${first?.baseKey?.code} 
                        |Parent:  ${first?.parentKey?.code} 
                        |Draw:    ${first?.drawKey?.code} 
                        |Pocket:  ${first?.pocketKey?.code} 
                        |Flow:    ${first?.flopKey?.code} 
                        |Turn:    ${first?.turnKey?.code} 
                        |River:   ${first?.riverKey?.code} 
                        |Total:   ${total.toLong().format}  
                        |Count:   ${count.format}  
                        |Elapsed: ${time.elapsedNow()}
                        |
                        |""".trimMargin()
                        )
                    }
                }
            }
    }
    work.join()
    println("Count: ${total.toLong().format}  Elapsed: ${time.elapsedNow()}")

}

val Card.hand: Hand get() = Hand(card = this)
val Hand.children: Sequence<Hand>
    get() = card.remaining.asSequence()
        .map { copy(parent = this, baseKey = baseKey.or(it.key), card = it) }

val Hand.initCards: List<Card>
    get() = Card.collection
        .filter { baseKey.and(it.key) == it.key }
        .filter { parentKey.and(it.key) == 0L }

val Hand.cards: List<Card>
    get() = initCards.filter { it.key < card.key }

val Hand.drawsInit: Sequence<Hand>
    get() = initCards.asSequence()
        .map { copy(parent = this, drawKey = it.key, parentKey = parentKey.or(it.key), card = it) }

val Hand.draws: Sequence<Hand>
    get() = cards.asSequence()
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
        |Bass:    ${baseKey.code} 
        |Parent:  ${parentKey.code} 
        |Draw:    ${drawKey.code} 
        |Pocket:  ${pocketKey.code} 
        |Flow:    ${flopKey.code} 
        |Turn:    ${turnKey.code} 
        |River:   ${riverKey.code} 
        |
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