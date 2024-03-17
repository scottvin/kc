package domain

data class Hand(
    val card: Card,
    val handIndex: Int = 1,
    val handKey: Long = 0L,
    val pocketKey: Long = 0L,
    val flopKey: Long = 0L,
    val turnKey: Long = 0L,
    val riverKey: Long = 0L,
    val baseKey: Long = (0L).inv(),
    val parentKey: Long = (0L),
    val flushKey: Long = (0L),
    val straightKey: Long = card.key,
    val straightFlushKey: Long = card.key,
    val kindKey: Long = (0L),
    val twoKindKey: Long = (0L),
    val highCardKey: Long = (0L),
) {


    companion object {
        val collection: List<Hand> = Card.collection.map { Hand(it) }
        val allEdge = collection.flatMap { it.filteredEdges }.sortedByDescending { it.key }
    }

    val flushBits: Int get() = flushKey.countOneBits()
    val royalFlushKey: Long get() =  straightFlushKey.and(Rank._A.seriesKey)
    val royalFlushBits: Int get() = royalFlushKey.countOneBits()
    val straightFlushBits: Int get() = straightFlushKey.countOneBits()
    val straightBits: Int get() = straightKey.countOneBits()
    val kindBits get() = kindKey.countOneBits()
    val twoKindBits get() = twoKindKey.countOneBits()
    val highCardBits get() = kindKey.countOneBits()
    val last get() = handIndex == 7

    val filteredEdges: Sequence<HandEdge>
        get() = card.edges
            .filter { (it.key.and(baseKey)) == it.key }
            .filter { (it.key.and(parentKey)) == 0L }
            .map { HandEdge(it.cardIn, it.cardOut) }.asSequence()

    val edges: Sequence<HandEdge>
        get() = card.edges.map { HandEdge(it.cardIn, it.cardOut) }.asSequence()


}

