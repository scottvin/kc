import domain.Card
import domain.Hand
import java.time.Instant


fun main() {
    Card.init()
    val start = Instant.now().toEpochMilli()
    val cnt = Hand.root.children( 7)
//        .onEach { println(it.code()) }
        .count()
    val end = Instant.now().toEpochMilli() - start
    println("Run Count $cnt")
    println("Run Time $end")
}



