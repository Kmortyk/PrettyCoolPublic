package com.kmortyk.game.ui.game

import com.badlogic.gdx.graphics.Color
import com.kmortyk.game.Assets
import com.kmortyk.game.quest.Quest
import com.kmortyk.game.quest.QuestType
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.element.CheckElementRegion
import com.kmortyk.game.ui.element.TextElement
import com.kmortyk.game.ui.element.TextureRegionBacking
import com.kmortyk.game.ui.group.ElementsGroup
import com.kmortyk.game.ui.right
import com.kmortyk.game.ui.screens.GameUI
import com.kmortyk.game.ui.top

val questTypeToIconName = mapOf(
    QuestType.Dummy to "quest_dummy",
    QuestType.BringItem to "quest_items"
)

class PlayerQuestListElement(width: Float, quest: Quest) : ElementsGroup(0.0f, 0.0f, width, Height) {
    companion object {
        const val Height = 30.0f + GameUI.DefaultPadding * 2
    }

    init {
        val data = quest.questData

        val iconTex = Assets.atlas("quest_icons").findRegion(questTypeToIconName[data.questType])

        addElement("questBacking", Backing(bounds, useBatchMatrix = true, fill = true, borderColor = Color.valueOf("#ab8353"), mainColor = Color.valueOf("#c0a077")))
        addElement("questIcon", TextureRegionBacking(iconTex, GameUI.DefaultPadding, bounds.top() - iconTex.regionHeight - GameUI.DefaultPadding))
        addElement("questName", TextElement(Assets.FontTimes18, data.name, 1000.0f, iconTex.regionWidth + GameUI.DefaultPadding*2,
            bounds.top() - GameUI.DefaultPadding*2, Color.WHITE, null, padding = 0.0f))

        val unchecked = Assets.atlas("ui_quest_check_mark").findRegion("unchecked")
        val checked = Assets.atlas("ui_quest_check_mark").findRegion("checked")
        addElement("questCheckMark", CheckElementRegion(bounds.right() - unchecked.regionWidth - GameUI.DefaultPadding,
        bounds.height*0.5f - unchecked.regionHeight*0.5f, unchecked, checked, null, quest.isFinished()))
    }
}