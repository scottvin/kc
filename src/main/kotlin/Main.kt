import domain.Card
import domain.Hand
import java.time.Instant


fun main() {
    Card.init()
    val start1 = Instant.now().toEpochMilli()
    val cnt1 = Hand.root.children( 3).onEach { println(it.code()) }.count()
    val end1 = Instant.now().toEpochMilli() - start1
    println(cnt1)
    println(end1)
}



