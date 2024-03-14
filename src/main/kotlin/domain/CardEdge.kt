package domain

data class CardEdge(
    val cardIn: Card,
    val cardOut: Card
) {
    val key: Long get() = cardIn.key.or(cardOut.key)
}