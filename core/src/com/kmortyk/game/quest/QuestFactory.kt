package com.kmortyk.game.quest

import com.kmortyk.game.Assets
import com.kmortyk.game.state.GameState

class QuestFactory {
    companion object {
        fun createQuest(gameState: GameState, questName: String) : Quest {
            val slz = Assets.Quests[questName]!!

            return when(slz.type) {
                "bring_item" -> BringItemToMeQuest(gameState, QuestData(slz.questID, slz.personID, QuestType.BringItem, slz.name, slz.description, slz.keys))
                else         -> DummyQuest(QuestData(slz.questID, slz.personID, QuestType.BringItem, slz.name, slz.description, slz.keys))
            }
        }
    }
}