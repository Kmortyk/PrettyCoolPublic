package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.kmortyk.game.Assets
import com.kmortyk.game.person.Person
import com.kmortyk.game.ui.element.InterfaceElement
import kotlin.math.ceil

class HeartBar(val assetManager: AssetManager, private val left: Float, private val bottom: Float, val player: Person) : InterfaceElement() {
    private val fullTex = assetManager[Assets["ui_heart_full"]]

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val percent = player.healthPoints.toDouble() / player.maxHealthPoints
        val offset = ceil(fullTex.height * (1 - percent)).toInt()

        val region = TextureRegion(fullTex, 0, offset, fullTex.width, fullTex.height - offset)

        spriteBatch.draw(region, left + 2f, bottom)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        return false
    }

}