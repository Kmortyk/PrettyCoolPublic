package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.item.Item

// TODO callback
class ItemPickUpEffect(assetManager: AssetManager, item: Item) : Effect() {
    companion object {
        private const val alphaSpeed = 0.4f
        private const val liftSpeed = 0.4f
        private const val minAlpha = 0.3f
    }

    private val pos: Vector2 = itemPositionOffset(assetManager[item.texture], item.position.actual)
    private val sprite: Sprite = Sprite(assetManager[item.texture])

    private var alphaLevel = 1.0f

    override fun onExtend(delta: Float): Boolean {
        if (alphaLevel > minAlpha) {
            alphaLevel -= delta * alphaSpeed
            pos.y += liftSpeed
        }
        return alphaLevel > minAlpha
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        sprite.setPosition(pos.x, pos.y)
        sprite.setAlpha(alphaLevel)
        sprite.draw(spriteBatch)
    }

    private fun itemPositionOffset(tex: Texture, pos: Vector2) : Vector2 {
        return Vector2(pos.x + (Assets.HexWidth - tex.width)*0.5f,
                       pos.y + (Assets.HexHeight - tex.height)*0.5f)
    }
}