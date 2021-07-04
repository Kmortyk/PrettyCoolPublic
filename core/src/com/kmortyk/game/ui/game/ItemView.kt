package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.item.Item
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.element.InterfaceElement
import com.kmortyk.game.ui.element.TextElement
import com.kmortyk.game.ui.element.TextureBacking
import com.kmortyk.game.ui.screens.GameUI

class ItemView(val gameState: GameState, var itmIdx: Int, var itm: Item, val offsetX: Float, val offsetY: Float) : InterfaceElement() {
    companion object {
        const val textPadding = GameUI.DefaultPadding*1.5f
    }

    private val itemUI : TextureBacking
    private val texDesc : Texture
    private val countTex: TextElement?

    init {
        val row = itmIdx / 7
        val col = itmIdx % 7

        texDesc = Assets.scaleTextureDescriptor(gameState.game.assetManager, itm.texture, 2.0f)
        val x = offsetX + col * InventoryCells.invCellSize + (InventoryCells.invCellSize - texDesc.width) * 0.5f
        val y = offsetY - row * InventoryCells.invCellSize + (InventoryCells.invCellSize - texDesc.height) * 0.5f

        itemUI = TextureBacking(texDesc, x, y)
        itemUI.isTouchable = false
        bounds = itemUI.bounds

        countTex = if(itm.isResource()) {
            TextElement(Assets.FontKurale12, "${itm.count}", 1000.0f,
                   offsetX + col* InventoryCells.invCellSize + textPadding,
                offsetY - row* InventoryCells.invCellSize +TextElement.lineHeight(Assets.FontKurale12)+ textPadding,
                Color.WHITE, null, padding = 0.0f)
        } else null
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        itemUI.draw(assetManager, spriteBatch)
        countTex?.draw(assetManager, spriteBatch)
    }

    override fun onTouch(x: Float, y: Float): Boolean { return false }

    fun updatePos(newItemIdx: Int) {
        val row = newItemIdx / 7
        val col = newItemIdx % 7

        val x = offsetX + col * InventoryCells.invCellSize + (InventoryCells.invCellSize - texDesc.width) * 0.5f
        val y = offsetY - row * InventoryCells.invCellSize + (InventoryCells.invCellSize - texDesc.height) * 0.5f

        itmIdx = newItemIdx
        itemUI.bounds.setPosition(x, y)
        bounds = itemUI.bounds

        countTex?.bounds?.setPosition(
            offsetX + col* InventoryCells.invCellSize + textPadding,
            offsetY - row* InventoryCells.invCellSize +TextElement.lineHeight(Assets.FontKurale12)+ textPadding -
                    countTex.bounds.height
        )
    }
}