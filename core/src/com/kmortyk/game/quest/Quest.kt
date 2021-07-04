package com.kmortyk.game.quest

import com.kmortyk.game.condition.Condition

abstract class Quest(
    // questData - whole data of this quest
    val questData: QuestData,
    // quest condition of that quest to be finished
    val condition: Condition
) {

    // TODO compare with the save data
    var isFinishedCache = false

    public fun isCompleted() : Boolean {
        return condition.isTruly()
    }

    // TODO retrieve from the save file
    public fun isFinished() : Boolean {
        return isFinishedCache
    }

    // finishes quest
    // TODO save to save file
    open fun finishQuest() {
        isFinishedCache = true
        // override for more actions ...
    }
}