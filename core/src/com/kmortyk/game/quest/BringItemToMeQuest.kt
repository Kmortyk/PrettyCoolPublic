package com.kmortyk.game.quest

import com.kmortyk.game.log
import com.kmortyk.game.condition.Condition
import com.kmortyk.game.condition.PlayerHasItemCondition
import com.kmortyk.game.state.GameState

const val QUEST_KEY_ITEM_NAME = "itemName"

class BringItemToMeQuest
    (
    val gameState: GameState,
    questData: QuestData
) :
    Quest(
        questData,
        PlayerHasItemCondition(gameState.player, questData.keysMap[QUEST_KEY_ITEM_NAME]!!)
    ) {

    private val itemName = questData.keysMap[QUEST_KEY_ITEM_NAME]!!

    override fun finishQuest() {
        super.finishQuest()

        // remove item from player's inventory
        val items = gameState.player.items

        for (idx in 0 until items.size()) {
            val item = items[idx]
            if (item != null && item.name == itemName) {
                items[idx] = null
            }
        }
    }
}