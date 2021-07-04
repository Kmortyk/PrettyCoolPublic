package com.kmortyk.game.ui.element

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion


class CheckElementRegion(
        left: Float, bottom: Float,
        val uncheckedTex: TextureRegion,
        val checkedTex: TextureRegion,
        val onCheckElement: OnCheckElement?,
        var toggled: Boolean = false) : InterfaceElement(left, bottom, uncheckedTex.regionWidth.toFloat(), uncheckedTex.regionHeight.toFloat()) {

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(toggled) {
            spriteBatch.draw(checkedTex, bounds.x, bounds.y)
        } else {
            spriteBatch.draw(uncheckedTex, bounds.x, bounds.y)
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if(onCheckElement != null) {
            toggle()
            onCheckElement.onCheck(toggled)
            return true
        }

        return false
    }

    override fun onTouchUp(x: Float, y: Float): Boolean {
        return true
    }

    public fun toggle() {
        toggled = !toggled
    }
}