package com.kmortyk.game.map

import kotlin.math.abs

class PathNode(var row: Int, var col: Int) {

    var parent: PathNode? = null
    var cost = 1

    // weights for A* algorithm
    var f = 0
    var g = 0
    var h = 0

    fun clearWeights() {
        h = 0; g = 0; f = 0
        parent = null
    }

    override fun equals(other: Any?): Boolean {
        if (other is PathNode) {
            return row == other.row && col == other.col
        }
        return false
    }

    override fun hashCode(): Int = 31 * row + col

    override fun toString(): String = "($row, $col)"

    companion object {
        /**
         * @return square of absolute distance between n1 and n2
         */
        fun distance(n1: PathNode, n2: PathNode): Int = abs(n2.row - n1.row) + abs(n2.col - n1.col)
    }
}