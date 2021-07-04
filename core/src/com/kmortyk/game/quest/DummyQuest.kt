package com.kmortyk.game.quest

import com.kmortyk.game.condition.Condition

class DummyQuest(questData: QuestData) : Quest(questData, object : Condition {
    override fun isTruly(): Boolean {
        return false
    }
}) {

    override fun finishQuest() {
        super.finishQuest()
        // do nothing
    }

}