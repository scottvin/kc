package domain

data class Hand(
    val key: Long = 0L,
    val card: Card = Card.empty,
    var subKey: Long = 0L,
    var filterKey: Long = (0L).inv(),
    var pocketKey: Long = 0L,
    var flopKey: Long = 0L,
    var turnKey: Long = 0L,
    var riverKey: Long = 0L
) {
    fun children(): Sequence<Hand> = card.remaining.asSequence()
        .filter { (it.key and this.filterKey) == it.key }
        .filter { (it.key and this.key) == 0L }
        .map { this.copy(key = this.key or it.key, card = it) }

    fun sub(): Long {
        val subKey = key and subKey.inv()
        this.subKey = this.subKey or subKey
        return subKey
    }

    fun print(): String { return """
***************************
Hand   ${Card.code(this.key)} 
Draw   ${Card.code(this.pocketKey)}:${Card.code(this.flopKey)}:${Card.code(this.turnKey)}:${Card.code(this.riverKey)} 
Filter ${Card.code(this.filterKey)}
Pocket ${Card.code(this.pocketKey)}
Flop   ${Card.code(this.flopKey)}
Turn   ${Card.code(this.turnKey)}
River  ${Card.code(this.riverKey)}
*************************** 
"""
    }

}