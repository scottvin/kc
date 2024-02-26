package domain

data class Rank(val index: Int = -1, val code: String = "", val key: Long = topRankKey shr (index * 4)) {
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

        val _A_K_Q_J_T: Series = Series(listOf(_A, _K, _Q, _J, _T))
        val _K_Q_J_T_9: Series = Series(listOf(_K, _Q, _J, _T, _9))
        val _Q_J_T_9_8: Series = Series(listOf(_Q, _J, _T, _9, _8))
        val _J_T_9_8_7: Series = Series(listOf(_J, _T, _9, _8, _7))
        val _T_9_8_7_6: Series = Series(listOf(_T, _9, _8, _7, _6))
        val _9_8_7_6_5: Series = Series(listOf(_9, _8, _7, _6, _5))
        val _8_7_6_5_4: Series = Series(listOf(_8, _7, _6, _5, _4))
        val _7_6_5_4_3: Series = Series(listOf(_7, _6, _5, _4, _3))
        val _6_5_4_3_2: Series = Series(listOf(_6, _5, _4, _3, _2))
        val _5_4_3_2_A: Series = Series(listOf(_5, _4, _3, _2, _A))
        val _4_3_2_A: Series = Series(listOf(_4, _3, _2, _A))
        val _3_2_A: Series = Series(listOf(_3, _2, _A))
        val _2_A: Series = Series(listOf(_2, _A))
        init {
            _A.series = _A_K_Q_J_T
            _K.series = _K_Q_J_T_9
            _Q.series = _Q_J_T_9_8
            _J.series = _J_T_9_8_7
            _T.series = _T_9_8_7_6
            _9.series = _9_8_7_6_5
            _8.series = _8_7_6_5_4
            _7.series = _7_6_5_4_3
            _6.series = _6_5_4_3_2
            _5.series = _5_4_3_2_A
            _4.series = _4_3_2_A
            _3.series = _3_2_A
            _2.series = _2_A
        }
    }

    lateinit var series: Series

    val cards: List<Card> get() = Suit.collection.map { Card.create(this, it) }
}