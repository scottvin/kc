import domain.Card
import domain.Hand
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

suspend fun main() = runBlocking {
    combine()
}

private suspend fun combine() {
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

    fun hands(hand:Hand): Sequence<Hand> =  sequenceOf(hand)
        .flatMap { pocket(it) }
        .flatMap { flop(it) }
        .flatMap { turn(it) }
        .flatMap { river(it) }

    fun hands() = flow {
        println(
            measureTimeMillis {
                hands(Hand()).forEach { emit(it) }
            }
        )
    }
    hands().buffer(1_000).collect { }
}
