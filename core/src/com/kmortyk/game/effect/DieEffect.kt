package com.kmortyk.game.effect

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.person.Direction
import com.kmortyk.game.person.Person

class DieEffect(private val prs: Person) : Effect() {
    companion object {
        private const val alphaSpeed = 0.4f
        private const val minAlpha = 0.3f
    }

    private val sprite: Sprite = Sprite(prs.animationManager.frame(Gdx.graphics.deltaTime))
    private var alphaLevel = 1.0f

    init {
        val pos = prs.getDrawPosition()
        sprite.setPosition(pos.x, pos.y)
        // tint color
        sprite.color = Color.WHITE.cpy().lerp(Color.RED, .6f)
    }

    override fun onExtend(delta: Float): Boolean {
        if (alphaLevel > minAlpha)
            alphaLevel -= delta * alphaSpeed
        return alphaLevel > minAlpha
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        sprite.setAlpha(alphaLevel)
        sprite.setFlip(prs.direction == Direction.Left, false)
        sprite.draw(spriteBatch)
    }
}