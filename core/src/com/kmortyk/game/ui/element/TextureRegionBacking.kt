package com.kmortyk.game.ui.element

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class TextureRegionBacking(val texture: TextureRegion, x: Float, y: Float) :
        InterfaceElement(x, y, texture.regionWidth.toFloat(), texture.regionHeight.toFloat()) {
    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.draw(texture, bounds.x, bounds.y)
    }

    override fun onTouch(x: Float, y: Float): Boolean { return true }
}