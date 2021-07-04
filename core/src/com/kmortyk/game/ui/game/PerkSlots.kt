package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.kmortyk.game.Assets
import com.kmortyk.game.log
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.element.TextureButton
import com.kmortyk.game.ui.group.ElementsGroup

class PerkSlots(assetManager: AssetManager, private val offsetX: Float, private val offsetY: Float, private val gameState: GameState) : ElementsGroup() {
    companion object {
        const val pad = 3
        const val selectionPad = 3
    }

    private val player = gameState.player
    private val qSlotTex = assetManager[Assets["ui_perk_slot"]]
    //private val perksTextures = Array<Texture?>(3) {null}
    private val perksTextures = Array<TextureRegion?>(3) {null}
    private val selection: Backing = Backing(0.0f, 0.0f, 0.0f, 0.0f,
        fill = false, border = true, borderColor = Color.valueOf("#8ade92"))

    init {
        bounds.x = x()
        bounds.y = y(0)

        selection.isVisible = false
        selection.isTouchable = false

        for(idx in 0 until 3) {
            val perk = player.perkSlots[idx]

            val qSlot = TextureButton(x(), y(idx), qSlotTex, Runnable {
                selection.bounds.set(x() + selectionPad, y(idx) + selectionPad,
                    qSlotTex.width.toFloat() - 2* selectionPad, qSlotTex.height.toFloat() - 2* selectionPad
                )
                selection.isVisible = true

                player.perkSlots[idx]?.execute()
            })
            addElements(qSlot)

            if(perk != null) {
//                perksTextures[idx] = Assets.scaleFitTextureRegion(
//                    perk.texture, qSlotTex.width - 2*pad, qSlotTex.height - 2*pad)
                perksTextures[idx] = Assets.centerCropTextureRegion(perk.texture,
                    qSlotTex.width - 2* pad, qSlotTex.height - 2* pad
                )
            }
        }

        //addElements(selection)
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        super.onDraw(assetManager, spriteBatch)
        for(idx in 0 until 3) {
            if(perksTextures[idx] != null) {
                spriteBatch.draw(perksTextures[idx], x() + pad, y(idx) + pad)
            }
        }
        selection.draw(assetManager, spriteBatch)
    }

    fun x() = offsetX

    fun y(idx: Int) = offsetY + offsetX + qSlotTex.height*(2 - idx)

    fun updatePerk(idx: Int) {
        if(idx >= perksTextures.size) {
            log.error("$idx is greater than perk slots count: ${perksTextures.size}")
            return
        }

        val perk = player.perkSlots[idx]

        if(perk != null) {
//            perksTextures[idx] = Assets.scaleFitTextureRegion(
//                perk.texture, qSlotTex.width - 2*pad, qSlotTex.height - 2*pad)
            perksTextures[idx] = Assets.centerCropTextureRegion(perk.texture,
                qSlotTex.width - 2* pad, qSlotTex.height - 2* pad
            )
        }
    }
}