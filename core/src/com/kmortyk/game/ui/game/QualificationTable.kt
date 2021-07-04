package com.kmortyk.game.ui.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.kmortyk.game.Assets
import com.kmortyk.game.person.Player
import com.kmortyk.game.ui.element.TextElement
import com.kmortyk.game.ui.group.ElementsGroup
import com.kmortyk.game.ui.right
import com.kmortyk.game.ui.screens.GameUI
import kotlin.math.max

class QualificationTable(left: Float, top: Float, val player: Player) : ElementsGroup() {
    data class TableElement(val row: Int, val col: Int, val nameElement: TextElement, val quantityElement: TextElement)

    private val table: ArrayList<TableElement> = ArrayList()
    private val font: BitmapFont = Assets.FontTimes14
    private val actualTop: Float = top - TextElement.lineHeight(font) - GameUI.DefaultPadding
    private val actualLeft: Float = left + GameUI.DefaultPadding

    var maxNameWidth = 10.0f
    var maxQuanWidth = 10.0f
    var maxBlockHeight = 10.0f

    init {
        update()
    }

    private fun addText(row: Int, col: Int, name: String, quantity: String) {
        val nameElement = TextElement(font, name, 1000.0f, 0.0f, 0.0f, Color.WHITE, null, padding = 0.0f)
        val quantityElement = TextElement(font, quantity, 1000.0f, nameElement.bounds.right(), 0.0f, Color.WHITE, null, padding = 0.0f)

        if(nameElement.bounds.width > maxNameWidth) {
            maxNameWidth = nameElement.bounds.width
        }
        if(quantityElement.bounds.width > maxQuanWidth) {
            maxQuanWidth = quantityElement.bounds.width
        }
        if(max(nameElement.bounds.height, quantityElement.bounds.height) > maxBlockHeight) {
            maxBlockHeight = max(nameElement.bounds.height, quantityElement.bounds.height)
        }

        val tableElement = TableElement(row, col, nameElement, quantityElement)
        table.add(tableElement)
        addElement(name, nameElement)
        addElement(name + quantity, quantityElement)

        alignElements()
    }

    private fun alignElements() {
        val width = maxNameWidth + maxQuanWidth + GameUI.DefaultPadding*3

        for(el in table) {
            el.nameElement.bounds.setPosition(
                actualLeft + (width)*el.col,
                actualTop - (maxBlockHeight + GameUI.DefaultPadding)*el.row)

            el.quantityElement.bounds.setPosition(
                actualLeft + (width)*el.col + maxNameWidth + GameUI.DefaultPadding,
                actualTop - (maxBlockHeight + GameUI.DefaultPadding)*el.row)
        }
    }

    public fun update() {
        table.clear()
        elements.clear()

        // first
        addText(0, 0, "HP", "${player.healthPoints}/${player.maxHealthPoints}")
        addText(1, 0, "EXP", "${player.lvl.curExp()}/${player.lvl.maxExp()}")

        // second
        addText(0, 1, "STRENGTH", "${player.stats.strength}")
        addText(1, 1, "ACCURACY", "${player.stats.accuracy}")
        addText(2, 1, "SPEED", "${player.stats.speed}")

        // third
        addText(0, 2, "DEFENSE", "${player.stats.defense}")
        addText(1, 2, "STEALTH", "${player.stats.stealth}")
        addText(2, 2, "INTELLECT", "${player.stats.intellect}")
    }
}