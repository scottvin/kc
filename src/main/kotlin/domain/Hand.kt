package domain

data class Hand(
    val card: Card,
    val key:Long = card.key,
    val drawKey:Long = 0L,
    val parent: Hand? = null
) {
//    val key:Long = card.key.or(parent?.key ?: 0)
}


