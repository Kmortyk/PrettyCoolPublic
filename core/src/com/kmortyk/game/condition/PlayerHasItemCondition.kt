package com.kmortyk.game.condition

import com.kmortyk.game.person.Player

class PlayerHasItemCondition(val player: Player, private val itemName: String) : Condition {
    override fun isTruly(): Boolean {
        for(it in player.items) {
            if(it != null && it.name == itemName)
                return true
        }
        return false
    }
}