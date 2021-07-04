package com.kmortyk.game.ui.element.special

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.ui.element.InterfaceElement

interface InterfaceElementCondition {
    fun drawIf() : Boolean
}

class ConditionElement(private val baseElement: InterfaceElement, private val condition: InterfaceElementCondition) : InterfaceElement() {

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(condition.drawIf()) {
            baseElement.draw(assetManager, spriteBatch)
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if(condition.drawIf() && baseElement.contains(x, y)) {
            return baseElement.touch(x, y)
        }
        return false
    }
}