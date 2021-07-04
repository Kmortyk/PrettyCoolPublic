package com.kmortyk.game.condition

import com.kmortyk.game.state.GameState

class ConditionFactory {
    companion object {
        fun createCondition(gameState: GameState, type: String, keys: Map<String, String>) : Condition {
            return when(type) {
                "quest_completed" -> PlayerHasCompletedQuestCondition(gameState.player, keys["questID"]!!)
                else -> EmptyCondition()
            }
        }
    }
}