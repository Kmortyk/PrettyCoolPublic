package com.kmortyk.game.person

import com.beust.klaxon.Klaxon
import com.kmortyk.game.Assets
import com.kmortyk.game.condition.Condition
import com.kmortyk.game.condition.ConditionFactory
import com.kmortyk.game.log
import com.kmortyk.game.state.GameState

enum class DialogState {
    SameLine,
    NextLine,
    EndOfDialog
}

data class DialogNode(val id: Int, val text: String, val lines: Array<DialogLine>)
data class DialogLineCondition(val type: String, val keys: Map<String, String>)
data class DialogLine(val text: String, val next_id: Int = -1, val exit: Boolean = false,
                      val add_quest: String = "", val finish_quest: String = "",
                      val conditions: List<DialogLineCondition> = listOf()) {

    val conditionObjects = mutableListOf<Condition>()

    fun finishesQuest() : Boolean {
        return finish_quest.isNotEmpty()
    }

    fun addsQuest() : Boolean {
        return add_quest.isNotEmpty()
    }

    fun hasConditions() : Boolean {
        return conditions.isNotEmpty()
    }

    fun isConditionsTruly() : Boolean {
        if(!hasConditions())
            return true

        for(cond in conditionObjects) {
            if(!cond.isTruly()) {
                return false
            }
        }

        return true
    }
}

class Dialog(dialogAssetName: String) {

    private val nodes: List<DialogNode>

    private var curID: Int = 0

    init {
        val json = Assets.readFile("dialogs/$dialogAssetName")
        nodes = Klaxon().parseArray(json)!!
    }

    fun text() = nodes[curID].text

    // returns list of possible player's lines
    fun lines() : Array<DialogLine> = nodes[curID].lines

    // change dialog root with given line's index
    fun moveTo(gameState: GameState, lineIdx: Int) : DialogState {
        val line = lines()[lineIdx]

        if(line.next_id < 0) {
            log.info("no next_id was passed for lineIdx=$lineIdx; stay at same branch")
            return DialogState.SameLine
        }

        curID = line.next_id

        if(line.exit)
            return DialogState.EndOfDialog

        createConditions(gameState)

        return DialogState.NextLine
    }

    fun createConditions(gameState: GameState) {
        for(line in nodes[curID].lines) {
            if(line.hasConditions()) {
                for(serializedCondition in line.conditions) {
                    line.conditionObjects.add(ConditionFactory.createCondition(gameState, serializedCondition.type, serializedCondition.keys))
                }
            }
        }
    }
}