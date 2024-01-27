import kotlinx.coroutines.*
import java.time.Instant
import java.util.stream.IntStream
import java.util.stream.LongStream
import kotlin.streams.asStream
import kotlin.streams.toList


const val seed = 1L shl 51
const val SIZE = 7
val list = (0 until 52).toList();
val sequence = list.asSequence();
suspend fun main() {
    val start3 = Instant.now().toEpochMilli()
    val cnt3 = generateStream(0L, seed, SIZE, 0).count()
    val end3 = Instant.now().toEpochMilli() - start3
    println(cnt3)
    println(end3)
}


fun generateStream(accumulator: Long, value: Long, size: Int, index: Int): LongStream {
    if (index < size) {
        return LongStream.iterate(value, { it > 0 }, { it shr 1 })
            .flatMap {
                when (index) {
                    0 -> generateStreamAsync(accumulator or it, it shr 1, size, index + 1)
                    else -> generateStream(accumulator or it, it shr 1, size, index + 1)
                }
            }
    }
    return LongStream.of(accumulator)
}

fun generateStreamAsync(accumulator: Long, value: Long, size: Int, index: Int): LongStream {
    return LongStream.iterate(value, { it > 0 }, { it shr 1 })
        .flatMap { generateStream(accumulator or it, it shr 1, size, index + 1) }
}

suspend fun List<Long>.flatMapAsyncList(mapper: suspend (Long) -> List<Long>): List<Long> =
    coroutineScope { map { async { mapper(it) } }.awaitAll().flatten() }
