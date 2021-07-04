package com.kmortyk.game.ui.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.effect.JumpUIEffect
import com.kmortyk.game.item.Item
import com.kmortyk.game.state.ControlState
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.element.TextureBacking
import com.kmortyk.game.ui.group.ElementsGroup

class WeaponView(private val gameState: GameState, private val camera: Camera) : ElementsGroup() {
    var curItem: Item? = null
    var prevItem: Item? = null

    private var textureElement = object : TextureBacking() {
        var effect: JumpUIEffect? = null

        override fun onTouch(x: Float, y: Float): Boolean {
            if(effect == null) {
                createJumpEffect()
            } else if(!effect!!.isNotEnded()) {
                effect = null
            }

            if(gameState.controlState == ControlState.TouchToWalk) {
                gameState.changeControlTo(ControlState.TouchToAttack)
            } else if(gameState.controlState == ControlState.TouchToAttack) {
                gameState.changeControlTo(ControlState.TouchToWalk)
            }

            return true
        }

        fun createJumpEffect() {
            effect = JumpUIEffect(this)
            gameState.game.addEffect(effect!!)
        }
    }

    init {
        addElements(textureElement)
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(gameState.isItemSelected()) {
            // if item selected - get item object
            curItem = gameState.player.items[gameState.selectedItemIdx]
            // if item has separate view
            if(curItem != null && curItem!!.hasView) {
                // update item
                if(prevItem != curItem) {
                    updateItem(curItem!!, assetManager)
                    prevItem = curItem
                }
                // draw view
                textureElement.draw(assetManager, spriteBatch)
            } else {
                // else draw placeholder
                drawHand(assetManager, spriteBatch)
            }
        } else {
            // is slot is empty - draw placeholder
            drawHand(assetManager, spriteBatch)
        }
    }

    private fun updateItem(curItem: Item, assetManager: AssetManager) {
        val texture = assetManager[Assets["${curItem.name}_view"]]
        val verticalOffset = texture.height * 0.1f

        textureElement.texture = texture
        textureElement.bounds.x = camera.viewportWidth - texture.width
        textureElement.bounds.y = bounds.y - verticalOffset

        textureElement.createJumpEffect()
    }

    private fun drawHand(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val texture = assetManager[Assets["ui_weapon_backing_hand"]]
        spriteBatch.draw(texture, camera.viewportWidth - texture.width, bounds.y)
    }
}