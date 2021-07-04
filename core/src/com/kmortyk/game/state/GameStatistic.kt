package com.kmortyk.game.state

import java.lang.RuntimeException

class GameStatistic {
    fun eventPossible(percentageStr: String) : Boolean {
        return Math.random().toFloat() > 1 - percentageStrToRatio(percentageStr)
    }

    fun randomNearPositionIndex(size: Int) : Int {
        return (Math.random()*size).toInt()
    }

    private fun percentageStrToRatio(percentageStr: String) : Float {
        val lastChar = percentageStr[percentageStr.lastIndex]
        val percentageStrNum = percentageStr.dropLast(1)

        if(lastChar != '%') {
            throw RuntimeException("unknown metrics for percentage: $lastChar")
        }

        val num = Integer.valueOf(percentageStrNum)
        return num / 100.0f
    }
}