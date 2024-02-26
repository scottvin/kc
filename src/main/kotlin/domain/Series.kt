package domain
data class Series(val ranks:List<Rank>) {
    companion object {
    }
    val key: Long get() = ranks.map { it.key }.reduce { acc, value ->  acc or value}
    val code: String get() = ranks.map { it.code }.reduce { acc, string -> "$acc $string" }
    val cards: List<Card> get() = Card.collection.filter { (it.key and this.key) == it.key }
}