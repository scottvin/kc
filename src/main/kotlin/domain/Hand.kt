package domain

import java.io.Flushable

data class Hand(
    val index:Int,
    val card: Card,
    val parent: Hand? = null,
    val parentKey: Long = 0L,
    val baseKey: Long = card.key,
    val last: Boolean = false,
    val drawHands: List<Hand> = listOf(),

    val drawKey: Long = 0L,
    val pocketKey: Long = 0L,
    val flopKey: Long = 0L,
    val turnKey: Long = 0L,
    val riverKey: Long = 0L,

    val draw: Draw = Draw.HIGH_CARD
    ) {

}


