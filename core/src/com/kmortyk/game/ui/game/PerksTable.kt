package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.math.Rectangle
import com.kmortyk.game.Assets
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.group.ElementsGroup
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.kmortyk.game.person.perk.*
import com.kmortyk.game.state.PlayerPerks
import com.kmortyk.game.ui.element.TextureBacking
import com.kmortyk.game.ui.screens.GameUI

interface OnPerkClick {
    fun onPerkClick(perk: Perk, row: Int, col: Int)
}

class PerksTable(val playerPerks: PlayerPerks, val assetManager: AssetManager,
                 val left: Float, private val top: Float,

    private val onPerkClick: OnPerkClick
) : ElementsGroup() {

    private val tex: TextureAtlas.AtlasRegion = Assets.atlas("perks").findRegion("perk_blindfury")
    private val selection: Backing = Backing(Rectangle(left, top, 0.0f, 0.0f), borderColor=Color.valueOf("#8ade92"), useBatchMatrix = true)
    private val offset = tex.originalHeight + GameUI.DefaultPadding

    init {
        selection.isTouchable = false

        for (row in 0 until playerPerks.size()/3) {
            for (col in 0 until 3) {
                addPerk(playerPerks[col*3 + row], row, col, col>0)
            }
        }

        addElements(selection)
    }

    private fun addPerk(perk: Perk, row: Int, col: Int, addArrow: Boolean) {
        val arrowTex = assetManager[Assets["ui_perks_tree_arrow"]]

        val x = left + (GameUI.DefaultPadding + PerkRect.PerkRectWidth)*col + arrowTex.width*col + GameUI.DefaultPadding*(col+1)
        val y = top - offset*(row+1)

        val rect = PerkRect(perk, x, y)
        rect.callback = Runnable {
            select(rect)
            onPerkClick.onPerkClick(perk, row, col)
        }
        addElements(rect)

        if(addArrow) {
            val arrowX = left + (2* GameUI.DefaultPadding + PerkRect.PerkRectWidth + arrowTex.width)*col -arrowTex.width
            val arrowY = top - offset*(row+1) + PerkRect.PerkRectHeight *0.5f - GameUI.DefaultPadding

            addElements(TextureBacking(arrowTex, arrowX, arrowY))
        }
    }

    private fun select(rect: PerkRect) {
        selection.bounds.height = PerkRect.PerkRectHeight
        selection.bounds.width = PerkRect.PerkRectWidth
        selection.bounds.x = rect.x
        selection.bounds.y = rect.y
    }
}