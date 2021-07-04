package com.kmortyk.game.item.actions

import com.kmortyk.game.Assets
import com.kmortyk.game.GridPosition
import com.kmortyk.game.effect.BulletEffect
import com.kmortyk.game.item.ItemAction
import com.kmortyk.game.person.Player
import com.kmortyk.game.state.GameState

class BulletEffectAction(val player: Player, private val bulletDrawable: String) : ItemAction() {
    override fun executeOn(gameState: GameState, row: Int, col: Int) {
        val tex = gameState.game.assetManager[Assets[bulletDrawable]]
        gameState.game.addEffect(BulletEffect(tex, player.gridPosition(), GridPosition(row, col)))
    }
}
