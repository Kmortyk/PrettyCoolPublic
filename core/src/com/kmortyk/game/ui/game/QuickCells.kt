package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.element.TextureButton
import com.kmortyk.game.ui.element.TextureDescBacking
import com.kmortyk.game.ui.group.ElementsGroup

class QuickCells(assetManager: AssetManager, private val offsetX: Float, private val gameState: GameState) : ElementsGroup() {
    companion object {
        const val cellOffsetX = 5.0f
        const val cellOffsetY = 16.0f
    }

    private val selection: TextureDescBacking = TextureDescBacking(Assets["ui_quick_slot_selected"], 0.0f, cellOffsetY)
    private val cellSize: Int = Assets.width(assetManager, "ui_quick_slot")
    private val playerItems = gameState.player.items

    init {
        for(i in 0 until 7) {
            val left = cellOffsetX + offsetX + cellSize * i
            val qSlot = TextureButton(left, cellOffsetY, assetManager[Assets["ui_quick_slot"]], Runnable {
                select(i)
            })
            elements.add(qSlot)
        }
        elements.add(selection)
        select(0)
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        super.onDraw(assetManager, spriteBatch)
        for(i in 0 until 7) {
            val itm = playerItems[i]
            if(itm != null) {
                val tex = assetManager[itm.texture]
                val x = cellOffsetX + offsetX + cellSize * i + (cellSize - tex.width) * 0.5f
                val y = 16.0f + (cellSize - tex.height) * 0.5f

                spriteBatch.draw(tex, x, y)
            }
        }
    }

    private fun select(idx: Int) {
        val left = cellOffsetX + offsetX + cellSize * idx.toFloat()
        selection.x = left

        gameState.selectedItemIdx = idx
    }
}