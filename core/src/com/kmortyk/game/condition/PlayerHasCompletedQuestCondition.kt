package com.kmortyk.game.condition

import com.kmortyk.game.person.Player

class PlayerHasCompletedQuestCondition(val player: Player, val questID: String) : Condition {
    override fun isTruly(): Boolean {
        return player.questsState.isQuestCompleted(questID)
    }
}