package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.person.Player
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.group.ElementsGroup

class PlayerExperienceBar(
    private val player: Player,
    x: Float, y: Float,
    private val barWidth: Float,
    barHeight: Float = 6.0f) : ElementsGroup() {

    private val border = Backing(x, y, barWidth, barHeight, fill=true, borderColor=Color.valueOf("#005516"), mainColor = Color.valueOf("#295e36"), lineWidth=4f)
    private val bar = Backing(x, y+2.0f, barWidth, barHeight-4.0f, mainColor=Color.valueOf("#05952b"), borderColor=Color.valueOf("#05952b"), border = false)

    init {
        addElements(border);
        addElements(bar)
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        updateValue(player.lvl.curExp(), player.lvl.maxExp())

        super.onDraw(assetManager, spriteBatch)
    }

    fun updateValue(value: Int, maxValue: Int) {
        bar.bounds.width = barWidth*(value.toFloat() / maxValue)
    }
}