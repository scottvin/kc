package domain

data class Hand(
    val card: Card,
    val baseKey: Long = (0L).inv(),
    val parentKey: Long = 0L,
    val pocketKey: Long = 0L,
    val flopKey: Long = 0L,
    val turnKey: Long = 0L,
    val riverKey: Long = 0L,
    val kindKey: Long = card.key,
    val twoKindKey: Long = 0L,
    val flushKey: Long = card.key,
    val straightKey: Long = card.key
) {


    companion object {
        var roots: Sequence<Hand> = Card.collection.asSequence().map { Hand(it, baseKey = it.key) }
        var test: Long =
            Rank._A.key.and(Suit._S.key) or
                    Rank._A.key.and(Suit._H.key) or
                    Rank._Q.key.and(Suit._D.key) or
                    Rank._Q.key.and(Suit._C.key) or
                    Rank._J.key.and(Suit._S.key)
        val baseHands: Sequence<Hand>
            get() = roots
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
                .flatMap { it.children }
//                .flatMap { it.children }
//                .flatMap { it.children }

//                .filter { it.baseKey == 4468415255281664L }
//                .filter { it.baseKey == test }

    }

    val kindBits get() = kindKey.countOneBits()
    val twoKindBits get() = twoKindKey.countOneBits()
    val flushBits get() = flushKey.countOneBits()
    val straightFlushKey: Long get() = straightKey.and(flushKey)
    val straightFlushBits get() = straightFlushKey.countOneBits()
    val straightBits get() = straightKey.countOneBits()
    val wheelKey: Long get() = straightKey.or(baseKey.and(Rank._A.key).takeHighestOneBit()).and(Rank.WHEEL_RANKS_KEY)
    val wheelBits get() = wheelKey.countOneBits()
    val royalFlushKey: Long get() = straightKey.and(flushKey).and(Rank.ROYAL_RANKS_KEY)
    val royalFlushBits: Int get() = royalFlushKey.countOneBits()
    val wheelFlushKey: Long get() = wheelKey.and(flushKey)
    val wheelFlushBits: Int get() = wheelFlushKey.countOneBits()
    val children: Sequence<Hand>
        get() = card.edges.asSequence()
            .map {
                val key = baseKey.or(it.cardOut.key)
                copy(
                    card = it.cardOut,
                    baseKey = key,
                    kindKey = it.kindFun(kindKey, key),
                    twoKindKey = it.twoKindFun(twoKindKey, key),
                    flushKey = it.flushFun(flushKey, key),
                    straightKey = it.straightFun(straightKey)
                )
            }
    val filteredCards: Sequence<Card>
        get() = card.remaining.asSequence()
            .filter { (baseKey.and(it.key) == it.key) }
            .filter { (parentKey.and(it.key) == 0L) }
    val pocketChildren: Sequence<Hand>
        get() = filteredCards.map { copy(pocketKey = pocketKey.or(it.key)) }
    val flopChildren: Sequence<Hand>
        get() = filteredCards.map { copy(flopKey = flopKey.or(it.key)) }
    val turnChildren: Sequence<Hand>
        get() = filteredCards.map { copy(turnKey = turnKey.or(it.key)) }
    val riverChildren: Sequence<Hand>
        get() = filteredCards.map { copy(riverKey = riverKey.or(it.key)) }
}

