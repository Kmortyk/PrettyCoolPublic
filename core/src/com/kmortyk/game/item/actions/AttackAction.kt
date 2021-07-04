package com.kmortyk.game.item.actions

import com.kmortyk.game.GridPosition
import com.kmortyk.game.item.ItemAction
import com.kmortyk.game.person.Player
import com.kmortyk.game.state.GameState
import kotlin.math.abs

class AttackAction(val player: Player, private val power: Int, val maxDistance: Int, val ammoName: String?) : ItemAction() {
    override fun executeOn(gameState: GameState, row: Int, col: Int) {
        val hex = gameState.gameMap[row, col]

        if(hex.hasPerson()) {
            // decrease ammo count
            if(usesAmmo()) {
                val ammoItem = gameState.player.items.getResource(ammoName!!)
                if(ammoItem == null || ammoItem.count <= 0)
                    return
                ammoItem.count -= 1
            }

            val target = hex.personSlot!!
            val power = (player.attackPower + power)

            // decrease target hp's
            target.healthPoints -= power

            if(target.healthPoints <= 0) {
                target.healthPoints = 0
                gameState.removePerson(player, target)
            }

            // change journal state
            player.journal.attack(power)
        }
    }

    // TODO check offsets
    fun canAttack(target: GridPosition) : Boolean {
        val plrGp = player.gridPosition()

        val x = abs(plrGp.row - target.row)
        val y = abs(plrGp.col - target.col)

        return x <= maxDistance || y <= maxDistance
    }

    fun usesAmmo() = ammoName != null
}