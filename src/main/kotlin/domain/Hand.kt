package domain

data class Hand(
    val handKey: Long = 0L,
    val card: Card = Card.empty,
    val pocketKey: Long = 0L,
    val flopKey: Long = 0L,
    val turnKey: Long = 0L,
    val riverKey: Long = 0L,
    val baseKey: Long = (0L).inv(),
    val parentKey: Long = (0L)
) {
    companion object {
        val empty = Hand()
    }
    fun print(): String {
        return """ 
            ************************************
            Key     ${this.handKey}L
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