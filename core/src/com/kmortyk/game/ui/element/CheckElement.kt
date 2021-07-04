package com.kmortyk.game.ui.element

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

interface OnCheckElement {
    fun onCheck(toggled: Boolean)
}

class CheckElement(
        left: Float, bottom: Float,
        val uncheckedTex: Texture,
        val checkedTex: Texture,
        val onCheckElement: OnCheckElement,
        var toggled: Boolean = false) : InterfaceElement(left, bottom, uncheckedTex.width.toFloat(), uncheckedTex.height.toFloat()) {

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(toggled) {
            spriteBatch.draw(checkedTex, bounds.x, bounds.y)
        } else {
            spriteBatch.draw(uncheckedTex, bounds.x, bounds.y)
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        toggled = !toggled
        onCheckElement.onCheck(toggled)
        return true
    }

    override fun onTouchUp(x: Float, y: Float): Boolean {
        return true
    }
}