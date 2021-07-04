package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.effect.Callback
import com.kmortyk.game.person.perk.Perk
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.element.LineElement
import com.kmortyk.game.ui.element.TextElement
import com.kmortyk.game.ui.element.TextureRegionBacking
import com.kmortyk.game.ui.group.ElementsGroup
import com.kmortyk.game.ui.screens.GameUI

class PerkRect(val perk: Perk, val x: Float, val y: Float, var callback: Callback? = null) : ElementsGroup() {
    companion object {
        const val PerkRectWidth: Float = 145.0f
        const val PerkRectHeight: Float = 50.0f
        const val PerkDrawableSize: Float = 50.0f
    }

    private val levelTextElement: TextElement
    private var prevFrameLevel: Int = -1

    init {
        addElements(Backing(x, y, PerkRectWidth, PerkRectHeight, mainColor=Color.valueOf("#9a7b5f"), lineWidth=2.0f))
        addElements(LineElement(x + perk.texture.regionWidth, y, x + perk.texture.regionWidth, y + perk.texture.regionHeight, 2.0f))
        addElements(TextureRegionBacking(perk.texture, x, y))

        val lineHeightKurale = TextElement.lineHeight(Assets.FontKurale12, PerkRectWidth - PerkDrawableSize)
        val lineHeightTimes = TextElement.lineHeight(Assets.FontTimes, PerkRectWidth - PerkDrawableSize)

        // perk levels so far
        levelTextElement = TextElement(Assets.FontTimes,  "${perk.curLevel}/${perk.maxLevel()}",
            PerkRectWidth - PerkDrawableSize,
            x + PerkRectWidth - TextElement.wordWidth(Assets.FontTimes, "${perk.curLevel}/${perk.maxLevel()}") - GameUI.DefaultPadding,
            y + lineHeightTimes + GameUI.DefaultPadding*1.5f,
            Color.WHITE, null, padding = 0.0f)

        // green if all filled up
        if(perk.curLevel == perk.maxLevel())
            levelTextElement.color = Color.GREEN

        addElements(
            // perk name
            TextElement(Assets.FontKurale12, perk.name,
                    PerkRectWidth - PerkDrawableSize, x + PerkDrawableSize + GameUI.DefaultPadding, y + PerkRectHeight - GameUI.DefaultPadding*1.5f,
                    Color.WHITE, null, padding = 0.0f),
            // perk stat
            TextElement(Assets.FontKurale12, perk.descriptionShort(),
                    PerkRectWidth - PerkDrawableSize, x + PerkDrawableSize + GameUI.DefaultPadding, y + lineHeightKurale + GameUI.DefaultPadding*1.5f,
                    Color.GREEN, null, padding = 0.0f),
            levelTextElement
        )
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(perk.curLevel != prevFrameLevel) {
            // create new string if level of the perk was changed since previous frame
            levelTextElement.text = "${perk.curLevel}/${perk.maxLevel()}"
            // if all filled up - color as green
            if(perk.curLevel == perk.maxLevel())
                levelTextElement.color = Color.GREEN
            // update level cache
            prevFrameLevel = perk.curLevel
        }
        super.onDraw(assetManager, spriteBatch)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (e.contains(x, y) && e.touch(x, y)) {
                callback!!.run()
                return true
            }
        }
        return false
    }
}