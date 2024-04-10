package domain

data class Hand(
    val card: Card,
    val parent: Hand? = null,
    val parentKey: Long = 0L,
    val baseKey: Long = card.key,
    val drawKey: Long = 0L,
    val pocketKey: Long = 0L,
    val flopKey: Long = 0L,
    val turnKey: Long = 0L,
    val riverKey: Long = 0L,
) {
}


