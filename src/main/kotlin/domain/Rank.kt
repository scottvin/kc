package domain

data class Rank(val index: Int, val code: String) {
    companion object {
        private var topRankKey: Long = 0b1111L shl (12 * 4)
        val collection: List<Rank> = listOf(
            Rank(0, "A"),
            Rank(1, "K"),
            Rank(2, "Q"),
            Rank(3, "J"),
            Rank(4, "T"),
            Rank(5, "9"),
            Rank(6, "8"),
            Rank(7, "7"),
            Rank(8, "6"),
            Rank(9, "5"),
            Rank(10, "4"),
            Rank(11, "3"),
            Rank(12, "2"),
        )
        val _A: Rank = collection[0]
        val _K: Rank = collection[1]
        val _Q: Rank = collection[2]
        val _J: Rank = collection[3]
        val _T: Rank = collection[4]
        val _9: Rank = collection[5]
        val _8: Rank = collection[6]
        val _7: Rank = collection[7]
        val _6: Rank = collection[8]
        val _5: Rank = collection[9]
        val _4: Rank = collection[10]
        val _3: Rank = collection[11]
        val _2: Rank = collection[12]

        val seriesData = listOf(
            listOf(_A),
            listOf(_A, _K),
            listOf(_A, _K, _Q),
            listOf(_A, _K, _Q, _J),
            listOf(_A, _K, _Q, _J, _T),
            listOf(_K, _Q, _J, _T, _9),
            listOf(_Q, _J, _T, _9, _8),
            listOf(_J, _T, _9, _8, _7),
            listOf(_T, _9, _8, _7, _6),
            listOf(_9, _8, _7, _6, _5),
            listOf(_8, _7, _6, _5, _4),
            listOf(_7, _6, _5, _4, _3),
            listOf(_6, _5, _4, _3, _2),
        )
    }
    val key: Long get() = topRankKey shr (index * 4)
    val series: List<Rank> get() = seriesData[index]
    val seriesKey: Long get() = series.map { it.key }.reduce { acc, key ->  acc.or(key)}
    val cards: List<Card> get() = Suit.collection.map { Card(this, it) }
    val next: Rank get() = collection[(index + 1) % 13]
}