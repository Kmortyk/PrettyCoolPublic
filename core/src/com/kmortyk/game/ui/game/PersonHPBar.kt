package com.kmortyk.game.ui.game

import com.badlogic.gdx.graphics.Color
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.group.ElementsGroup

class PersonHPBar : ElementsGroup() {
    companion object {
        const val barWidth = 32f
        const val barHeight = 4f
    }

    private val border = Backing(0.0f, 0.0f, barWidth, barHeight,
            fill=false, borderColor=Color.valueOf("#460101"), lineWidth=2f)
    private val fullBar = Backing(0.0f, 0.0f, barWidth, barHeight,
            mainColor=Color.valueOf("#9d0122"), borderColor=Color.valueOf("#9d0122"))

    init {
        addElements(fullBar)
        addElements(border)
    }

    fun updatePosition(actualX: Float, actualY: Float) {
        border.bounds.x = actualX
        border.bounds.y = actualY

        fullBar.bounds.x = actualX
        fullBar.bounds.y = actualY
    }

    fun updateValue(value: Int, maxValue: Int) {
        val ratio = value.toFloat() / maxValue
        val offset = barWidth - (barWidth *ratio)

        val actualX = fullBar.bounds.x

        fullBar.bounds.x = actualX + offset
        fullBar.bounds.width = barWidth - offset
    }
}