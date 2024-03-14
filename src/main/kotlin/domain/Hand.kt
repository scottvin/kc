package domain

data class Hand(
    val card: Card,
    val handKey: Long = 0L,
    val pocketKey: Long = 0L,
    val flopKey: Long = 0L,
    val turnKey: Long = 0L,
    val riverKey: Long = 0L,
    val baseKey: Long = (0L).inv(),
    val parentKey: Long = (0L),
    val straightFlushKey: Long = (0L),
    val flushKey: Long = (0L),
    val straightKey: Long = (0L),
    val twoKindKey: Long = (0L),
    val oneKindKey: Long = (0L),
    val highCardKey: Long = (0L),
    var draw: Draw = Draw.HIGH_CARD
) {


    companion object {
        val collection: List<Hand> = Card.collection.map { Hand(it) }
        val allEdge = collection.flatMap { it.edges }.sortedByDescending { it.key }
    }
    val edges: Sequence<HandEdge>
        get() = card.edges.map { HandEdge(it.cardIn, it.cardOut) }.asSequence()

    fun print(): String {
        return """ 
            ************************************
            Key:     ${this.handKey}L
            Base:    ${Card.code(this.baseKey)}
            Parent:  ${Card.code(this.parentKey)}
            Hand:    ${Card.code(this.handKey)}
            Draw:    ${Card.code(this.pocketKey)} ${Card.code(this.flopKey)} ${Card.code(this.turnKey)} ${Card.code(this.riverKey)}
            pocket:  ${Card.code(this.pocketKey)}
            flop:    ${Card.code(this.flopKey)}
            Turn:    ${Card.code(this.turnKey)}
            River:   ${Card.code(this.riverKey)}
        """.trimIndent()
    }

}

