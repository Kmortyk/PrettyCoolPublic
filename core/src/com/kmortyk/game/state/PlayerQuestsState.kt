package com.kmortyk.game.state

import com.kmortyk.game.log
import com.kmortyk.game.quest.Quest
import com.kmortyk.game.quest.QuestFactory

class PlayerQuestsState {
    val currentQuests = mutableListOf<Quest>()
    private var hasUnseenQuests: Boolean = false

    public fun addQuest(quest: Quest) {
        currentQuests.add(0, quest)
        hasUnseenQuests = true
    }

    public fun addQuest(gameState: GameState, questID: String) {
        val quest = QuestFactory.createQuest(gameState, questID)
        currentQuests.add(0, quest)
        hasUnseenQuests = true
    }

    public fun finishQuest(questID: String) {
        for(quest in currentQuests) {
            if(quest.questData.questID == questID) {
                quest.finishQuest()
                return
            }
        }

        log.info("can't finish quest '$questID' player has no such quest")
    }

    public fun isQuestCompleted(questID: String) : Boolean {
        for(quest in currentQuests) {
            if(quest.questData.questID == questID) {
                return quest.isCompleted()
            }
        }

        return false
    }

    public fun hasUnseenQuests(): Boolean {
        return hasUnseenQuests
    }

    public fun sawAllQuests() {
        hasUnseenQuests = false
    }
}