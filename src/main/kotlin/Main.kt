fun main() {
    combination("Hand Generator"){
        level("Level 1"){
            drawFrom("High Card"){}
        }
        level("Level 2"){
            drawFrom("High Card"){
                match("Rank")  {
                    drawTo("Pair"){}
                }
                noMatch("Rank")  {
                    drawTo("Pair Complete"){}
                }
            }
        }
        level("Level 3"){
            drawFrom("High Card"){
                match("Rank")  {
                    drawTo("Pair"){}
                }
            }
            drawFrom("Pair"){
                match("Rank")  {
                    drawTo("Triple"){}
                }
            }
        }
        level("Level 4"){
            drawFrom("High Card"){
                match("Rank")  {
                    drawTo("Pair"){}
                }
            }
            drawFrom("Pair"){
                match("Rank")  {
                    drawTo("Triple"){}
                }
            }
            drawFrom("Triple"){
                match("Rank")  {
                    drawTo("Quadruple"){}
                }
            }
            drawFrom("High Card"){
                match("None") {
                    drawTo("High Card"){}
                }
                match("Rank")  {
                    drawTo("Pair"){}
                }
                match("Suite")  {
                    drawTo("High Card"){}
                }
                match("Series")  {
                    drawTo("High Card"){}
                }
            }
        }
    }
}

fun match(name: String, create: () -> Unit) {
    TODO("Not yet implemented")
}

fun noMatch(name: String, create: () -> Unit) {
    TODO("Not yet implemented")
}

fun drawFrom(name: String, create: () -> Unit) {
    TODO("Not yet implemented")
}

fun drawTo(name: String, create: () -> Unit) {
    TODO("Not yet implemented")
}

fun level(name: String, create: () -> Unit) {
    TODO("Not yet implemented")
}

fun combination(name: String, create: () -> Unit) {
    TODO("Not yet implemented")
}
