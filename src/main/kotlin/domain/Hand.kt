package domain

data class Hand(
    val card: Card,
    val baseKey: Long = card.key,
    val parentKey: Long = 0L,
    val drawKey: Long = 0L,
) {
}


