import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant


const val seed = 1L shl 51
const val SIZE = 7
val list = (0 until 52).toList()
val sequence = list.asSequence()
suspend fun main() {
    val start3 = Instant.now().toEpochMilli()
    val cnt3 = generateAsync(0L, seed, SIZE, 0).count()
    val end3 = Instant.now().toEpochMilli() - start3
    println(cnt3)
    println(end3)
}


suspend fun generateAsync(accumulator: Long, value: Long, size: Int, index: Int): Sequence<Long> {
    return sequence.map { value shr it }
        .takeWhile { it > 0 }
        .flatMapAsync { generate(accumulator or it, it shr 1, size, index + 1) }

}

fun generate(accumulator: Long, value: Long, size: Int, index: Int): Sequence<Long> {
    if (index < size) {
        return sequence.map { value shr it }
            .takeWhile { it > 0 }
            .flatMap { generate(accumulator or it, it shr 1, size, index + 1) }
    }
    return sequenceOf(accumulator)
}

fun Sequence<Long>.flatMapAsync(mapper: (Long) -> Sequence<Long>): Sequence<Long> =
    flatMap { mapper(it) }


