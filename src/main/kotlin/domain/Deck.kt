package domain

data class Deck(
    var key: ULong = ALL_CARDS_KEY
) {
    companion object {
        val ALL_CARDS_KEY: ULong = generateSequence (1UL shl 51){ it shr 1}.take(52).reduce { acc, it -> acc or it}
        val empty: Deck = Deck(0UL)
    }

    val isEmpty: Boolean get() = key == 0UL
    val takeHighestCard: ULong get() {
        val takeHighestCard: ULong = key.takeHighestOneBit()
        key = key and takeHighestCard.inv()
        return takeHighestCard
    }
}