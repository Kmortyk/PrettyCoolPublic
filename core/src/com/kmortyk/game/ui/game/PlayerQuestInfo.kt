package com.kmortyk.game.ui.game

import com.badlogic.gdx.graphics.Color
import com.kmortyk.game.Assets
import com.kmortyk.game.quest.Quest
import com.kmortyk.game.ui.bottom
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.element.TextElement
import com.kmortyk.game.ui.group.ElementsGroup
import com.kmortyk.game.ui.left
import com.kmortyk.game.ui.screens.GameUI
import com.kmortyk.game.ui.top

class PlayerQuestInfo(left: Float, bottom: Float, width: Float, height: Float) : ElementsGroup() {

    private val backing: Backing
    private val questName: TextElement
    private val questDescription: TextElement

    init {
        bounds.set(left, bottom, width, height)

        backing = Backing(bounds, useBatchMatrix=true, fill=true, borderColor=Color.valueOf("#ab8353"), mainColor=Color.valueOf("#c0a077"))
        questName = TextElement(
            Assets.FontTimes18, "<quest_name>", bounds.width, bounds.left() + GameUI.DefaultPadding*2,
            bounds.top() - GameUI.DefaultPadding, Color.WHITE, null, padding = 0.0f)
        questDescription = TextElement(
            Assets.FontTimes14, "<quest_description>", bounds.width, bounds.left() + GameUI.DefaultPadding*2,
            questName.bounds.bottom() - GameUI.DefaultPadding*2, Color.WHITE, null, padding = 0.0f)

        addElement("backing", backing)
        addElement("questName", questName)
        addElement("questDescription", questDescription)
    }

    public fun setQuest(quest: Quest) {
        val h = questDescription.bounds.height

        questName.updateTextSavePosition(quest.questData.name)
        questDescription.updateTextSavePosition(quest.questData.description)

        questDescription.bounds.y = questDescription.bounds.y + h - questDescription.bounds.height
    }
}