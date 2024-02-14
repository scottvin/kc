import domain.Card
import domain.Hand
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

suspend fun main() = runBlocking {
    combine()
}

private suspend fun combine() {
    fun hands(hand: Hand): Sequence<Hand> =
        sequenceOf(hand)
            .flatMap { it.children() }
            .flatMap { it.children() }
            .flatMap { it.children() }
            .flatMap { it.children() }
            .flatMap { it.children() }
            .flatMap { it.children() }
            .flatMap { it.children() }
            .map { it.copy(key = 0L, filterKey = it.key, card = Card.empty) }

    fun pocket(hand: Hand): Sequence<Hand> =
        sequenceOf(hand)
            .flatMap { it.children() }
            .flatMap { it.children() }
            .map { it.copy(pocketKey = it.sub(), card = Card.empty) }

    fun flop(hand: Hand): Sequence<Hand> =
        sequenceOf(hand)
            .flatMap { it.children() }
            .flatMap { it.children() }
            .flatMap { it.children() }
            .map { it.copy(flopKey = it.sub(), card = Card.empty) }

    fun turn(hand: Hand): Sequence<Hand> =
        sequenceOf(hand)
            .flatMap { it.children() }
            .map { it.copy(turnKey = it.sub(), card = Card.empty) }

    fun river(hand: Hand): Sequence<Hand> =
        sequenceOf(hand)
            .flatMap { it.children() }
            .map { it.copy(riverKey = it.sub(), card = Card.empty) }

    fun hands() = flow {
        println(
            measureTimeMillis {
                sequenceOf(sequenceOf(Hand()))
                    .map { it.flatMap { hand -> hands(hand) } }
                    .flatten()
                    .map { sequenceOf(it) }
                    .take(100_000)
                    .map { it.flatMap { hand -> pocket(hand) } }
                    .map { it.flatMap { hand -> flop(hand) } }
                    .map { it.flatMap { hand -> turn(hand) } }
                    .map { it.flatMap { hand -> river(hand) } }
                    .forEach { emit(it) }
            }
        )
    }
    println(
        measureTimeMillis {
            println("Count ${hands().buffer(1_000_000).map { it.count().toLong() }.reduce { acc, it -> acc + it }}")
        }
    )
    hands().take(1).collect{it.take(1).forEach { hand ->  println(hand.print()) }}

    hands().buffer(1_000).collect { it.forEach { _ -> } }
}
