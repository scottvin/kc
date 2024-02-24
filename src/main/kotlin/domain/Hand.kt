package domain

data class Hand(
    val handKey: ULong = 0UL,
    val card: Card = Card.empty,
    val pocketKey: ULong = 0UL,
    val flopKey: ULong = 0UL,
    val turnKey: ULong = 0UL,
    val riverKey: ULong = 0UL,
    val baseKey: ULong = (0UL).inv(),
    val parentKey: ULong = (0UL)
) {
    companion object {
        val empty = Hand()
    }
    fun print(): String {
        return """ 
            ************************************
            Key     ${this.handKey}UL
            Base    ${Card.code(this.baseKey)}
            Parent  ${Card.code(this.parentKey)}
            Hand    ${Card.code(this.handKey)}
            Draw    ${Card.code(this.pocketKey)} ${Card.code(this.flopKey)} ${Card.code(this.turnKey)} ${Card.code(this.riverKey)}
            pocket  ${Card.code(this.pocketKey)}
            flop    ${Card.code(this.flopKey)}
            Turn    ${Card.code(this.turnKey)}
            River   ${Card.code(this.riverKey)}
        """.trimIndent()
    }

}