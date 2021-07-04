package com.kmortyk.game.state

import kotlin.math.exp
import kotlin.math.sqrt

class PlayerLevelState {
    companion object {
        const val initialExp = 3
        const val initialExpMax = 50
        const val initialLevel = 1
        const val initialOps = 1
    }

    private var exp = initialExp
    private var max = initialExpMax
    private var level = initialLevel
    private var ops = initialOps

    fun curExp() = exp

    fun maxExp() = max

    fun curLevel() = level

    fun curOperationPoints() = ops

    fun spentOperationPoint() { ops -= 1 }

    fun addExp(expAdd: Int) : Boolean {
        exp += expAdd

        return if(exp >= max)
        {
            exp -= max; nextLevel()
            true
        } else false
    }

    fun nextLevel() {
        level += 1
        ops += 1
        max = calcNewMax(max, level)
    }

    private fun calcNewMax(curMax: Int, nextLevel: Int) : Int {
        // e^sqrt(x∙2) + 10

        val e = exp(sqrt(nextLevel * 2.0f))
        val fx = (e + 10).toInt()

        return curMax + fx
    }

}