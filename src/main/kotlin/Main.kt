import kotlinx.coroutines.*
import java.time.Instant
import java.util.stream.IntStream
import java.util.stream.LongStream
import kotlin.streams.asStream


const val seed = 1L shl 51
const val SIZE = 7
val list = (0 until 52).toList();
val sequence = list.asSequence();
suspend fun main() {
//    val start1 = Instant.now().toEpochMilli()
//    val cnt1 = generate(0L, seed, SIZE, 0).count()
//    val end1 = Instant.now().toEpochMilli() - start1
//    println(cnt1)
//    println(end1)
//    println()
//
    val start2 = Instant.now().toEpochMilli()
    val cnt2 = generateList(0L, seed, SIZE, 0).count()
    val end2 = Instant.now().toEpochMilli() - start2
    println(cnt2)
    println(end2)
    println()
//
//    val start3 = Instant.now().toEpochMilli()
//    val cnt3 = generateStream(0L, seed, SIZE, 0).count()
//    val end3 = Instant.now().toEpochMilli() - start3
//    println(cnt3)
//    println(end3)
}


fun generate(accumulator: Long, value: Long, size: Int, index: Int): Sequence<Long> {
    if (index < size) {
        return sequence
            .map { value shr it }
            .takeWhile { it > 0 }
            .flatMap { generate(accumulator or it, it shr 1, size, index + 1) }
    }
    return sequenceOf(accumulator)
}
fun generateStream(accumulator: Long, value: Long, size: Int, index: Int): LongStream {
    if (index < size) {
        return LongStream.iterate(value, { it > 0 }, { it shr 1})
            .flatMap { generateStream(accumulator or it, it shr 1, size, index + 1) }
    }
    return LongStream.of(accumulator)
}
suspend fun generateList(accumulator: Long, value: Long, size: Int, index: Int): List<Long> {
    if (index < size) {
        return list.map { value shr it }
            .takeWhile { it > 0 }
            .flatMap { if (index == 0 ) {
                generateListAsync(accumulator or it, it shr 1, size, index + 1)
            } else {
                generateList(accumulator or it, it shr 1, size, index + 1)
            }}
    }
    return listOf(accumulator)
}

suspend fun generateListAsync(accumulator: Long, value: Long, size: Int, index: Int): List<Long> {
    if (index < size) {
        return list.map { value shr it }
            .takeWhile { it > 0 }
            .flatMapAsync { generateList(accumulator or it, it shr 1, size, index + 1) }
    }
    return listOf(accumulator)
}

suspend fun List<Long>.flatMapAsync(mapper: suspend (Long) -> List<Long>): List<Long> =
    coroutineScope { map { async { mapper(it) } }.awaitAll().flatten() }