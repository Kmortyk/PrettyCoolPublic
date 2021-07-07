package com.kmortyk.game.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.*
import com.kmortyk.game.effect.Callback
import com.kmortyk.game.person.Dialog
import com.kmortyk.game.person.perk.Perk
import com.kmortyk.game.person.perk.PerkType
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.*
import com.kmortyk.game.ui.element.*
import com.kmortyk.game.ui.element.special.ConditionElement
import com.kmortyk.game.ui.element.special.InterfaceElementCondition
import com.kmortyk.game.ui.element.special.TextWatcherElement
import com.kmortyk.game.ui.element.special.ValueReceiver
import com.kmortyk.game.ui.game.HeartBar
import com.kmortyk.game.ui.game.InventoryCells
import com.kmortyk.game.ui.game.OnPerkClick
import com.kmortyk.game.ui.game.PerkRect
import com.kmortyk.game.ui.game.PerkSlots
import com.kmortyk.game.ui.game.PerksTable
import com.kmortyk.game.ui.game.PlayerExperienceBar
import com.kmortyk.game.ui.game.PlayerQuestInfo
import com.kmortyk.game.ui.game.QuickCells
import com.kmortyk.game.ui.game.WeaponView
import com.kmortyk.game.ui.group.ElementsGroup
import com.kmortyk.game.ui.group.OnElementSelected
import com.kmortyk.game.ui.group.VerticalListElement
import java.util.*

data class GameUIState(val state: GameUIStateType = GameUIStateType.Main, val elements: MutableList<InterfaceElement>)

enum class GameUIStateType {
    Main,
    Dialog,
    Perks,
    Inventory,
    PerkCardDialog,
    SelectPerkSlotDialog,
    Quests,
}

class GameUI(private val game: PrettyCoolGame,
             private val gameUiSpriteBatch: SpriteBatch,
             private val gameSkin: GameSkin,
             private val screenWidth: Float,
             private val screenHeight: Float,
             private val gCam: GameCamera,
             private val gameState: GameState,
             private val assetManager: AssetManager) : ElementsGroup() {
    companion object {
        val MiniMapSize = Gdx.graphics.width*0.16f
        const val DefaultPadding = 6.4f
    }

    // all view's history for returning back
    private val viewsStack: Stack<GameUIState> = Stack()
    // current ui state type
    private var state: GameUIStateType = GameUIStateType.Main

    init {
        elements = createGameUI(screenWidth, screenHeight, assetManager)
    }

    fun backward() {
        if(viewsStack.empty())
            return // nothing to back

        val prevUIState = viewsStack.pop()

        state = prevUIState.state
        elements = prevUIState.elements
    }

    private fun forward(newState: GameUIStateType, savePrevElements: Boolean = false) {
        if(state == newState)
            return // already in this state

        // save current state
        val gameUIState = GameUIState(state, elements)
        viewsStack.push(gameUIState)

        state = newState
        elements = mutableListOf()
        if(savePrevElements)
            elements.addAll(gameUIState.elements)
    }

    fun draw(assetManager: AssetManager) {
        gameUiSpriteBatch.begin()
        super.onDraw(assetManager, gameUiSpriteBatch)
        gameUiSpriteBatch.end()
    }

    /* -- Different ui's -------------------------------------------------------------------------------------------- */
    private fun createGameUI(screenWidth: Float, screenHeight: Float, assetManager: AssetManager) : MutableList<InterfaceElement> {
        val elements: MutableList<InterfaceElement> = mutableListOf()
        // heart
        val tex = assetManager[Assets["ui_heart"]]
        val hpHeart = TextureButton(5.0f, screenHeight - tex.height - DefaultPadding*0.5f, assetManager[Assets["ui_heart"]], Runnable {
            log.info("This is hero ${LogColors.RED_BOLD}hurt${LogColors.RESET}.")
        })

        elements.add(hpHeart)
        elements.add(HeartBar(assetManager, 5.0f, screenHeight - tex.height + 2f - DefaultPadding*0.5f, gameState.player))

        // map
        val map = TextureRegionButton(0.0f, 0.0f,Assets.atlas("ui_game").findRegion("ui_map_back"), Runnable { /* none */ })
        addElementTo(elements, "mapBounds", map)

        // item quick slots
        elements.add(QuickCells(assetManager, map.bounds.width, gameState))

        // perks slots
        val perksSlots = PerkSlots(assetManager, 5.0f, map.bounds.top(), gameState)
        addElementTo(elements, "perksSlots", perksSlots)

        // chat button
        // val chatButtonTex = assetManager[Assets["ui_chat"]]
        // val chatButton = TextureButton(screenWidth - chatButtonTex.width - 10.0f, 16.0f, chatButtonTex, Runnable {  })
        // elements.add(chatButton)

        val weaponBackingTex = assetManager[Assets["ui_weapon_backing"]]
        val weaponBacking = TextureBacking(weaponBackingTex, screenWidth - weaponBackingTex.width, 0.0f)
        addElementTo(elements, "weaponBacking", weaponBacking)
        addElementTo(elements, "weaponView", WeaponView(gameState, game.camera))

        // ammo count
        val ammoTextElement = TextWatcherElement(Assets.FontKurale12,
            weaponBacking.bounds.left() + DefaultPadding*4f,
            weaponBacking.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale12) + DefaultPadding,

            //Color.valueOf("#f7cb25")
            Color.valueOf("#dcdcdc")

            , object : ValueReceiver<Int?> {
                override fun receiveValue(): Int? {
                    if(!gameState.isItemSelected())
                        return null

                    val itm = gameState.player.ammoItemForSelectedWeapon(gameState) ?: return 0

                    return itm.count
                }
        })
        addElementTo(elements, "ammoTextElement", ammoTextElement)

        // perks button
        val perksButtonTex = assetManager[Assets["ui_perks"]]
        val perksButton = TextureButton(screenWidth - perksButtonTex.width - DefaultPadding*0.5f,
            screenHeight - perksButtonTex.height - DefaultPadding*0.5f, perksButtonTex,
        Runnable {
            openPerks(gameState)
        })
        elements.add(perksButton)

        val lvlUpTex = assetManager[Assets["ui_lvlup"]]
        elements.add(ConditionElement(TextureBacking(
            lvlUpTex,
            perksButton.bounds.right() - lvlUpTex.width - DefaultPadding*0.5f,
            perksButton.bounds.top() - lvlUpTex.height - DefaultPadding
        ), object : InterfaceElementCondition {
            override fun drawIf(): Boolean {
                return gameState.player.lvl.curOperationPoints() > 0
            }
        }))

        // inventory button
        val invButtonTex = assetManager[Assets["ui_inventory"]]
        val invButton = TextureButton(screenWidth - invButtonTex.width - DefaultPadding*0.5f,
            screenHeight - perksButtonTex.height - invButtonTex.height - DefaultPadding*1.0f, invButtonTex,
        Runnable {
            openInventory(gameState)
        })
        elements.add(invButton)

        // quests button
        val qstButtonTex = assetManager[Assets["ui_quests"]]
        val qstButton = TextureButton(
            screenWidth - qstButtonTex.width - DefaultPadding*0.5f,
            screenHeight - perksButtonTex.height - invButtonTex.height - qstButtonTex.height - DefaultPadding*1.5f, qstButtonTex,
            Runnable {
                openQuests(gameState)
            })
        elements.add(qstButton)

        val nwQstTex = assetManager[Assets["ui_nwqst"]]
        elements.add(ConditionElement(TextureBacking(
            nwQstTex,
            qstButton.bounds.right() - nwQstTex.width - DefaultPadding*0.5f,
            qstButton.bounds.top() - nwQstTex.height - DefaultPadding
        ), object : InterfaceElementCondition {
            override fun drawIf(): Boolean {
                return gameState.player.questsState.hasUnseenQuests()
            }
        }))

        return elements
    }

    public fun openDialog(dialog: Dialog) {
        forward(GameUIStateType.Dialog)

        val textBack = Backing(gameSkin, 0.0f, 0.0f, screenWidth, screenHeight*0.3f)
        addElement("textBack", textBack)

        // ffd45f
        val textElement = TextElement(Assets.FontKurale, dialog.text(), screenWidth, 0.0f, screenHeight*0.3f, Color.WHITE, null)
        addElement("textElement", textElement)

        dialog.createConditions(gameState)

        val lineHeight = TextElement.lineHeight(Assets.FontKurale, screenWidth)
        val lines = dialog.lines()

        val verticalListElement = VerticalListElement(gCam, DefaultPadding*2,
            textElement.bounds.y - lineHeight * 0.3f - DefaultPadding*2,
            screenWidth-DefaultPadding*2,
            textElement.bounds.bottom() - DefaultPadding*2, onElementSelected = object : OnElementSelected {
                override fun onElementSelected(idx: Int, element: InterfaceElement) {
                    val selectedLine = lines[idx]

                    if(!selectedLine.isConditionsTruly())
                        return

                    // [1] if line adds quest - create new
                    if(selectedLine.addsQuest()) {
                        log.info("add quest ${LogColors.GREEN}\"${selectedLine.add_quest}\"${LogColors.RESET}")
                        gameState.player.questsState.addQuest(gameState, selectedLine.add_quest)
                    }

                    // [2] if line finishes quest - make so
                    if(selectedLine.finishesQuest()) {
                        log.info("finish quest ${LogColors.GREEN}\"${selectedLine.finish_quest}\"${LogColors.RESET}")
                        gameState.player.questsState.finishQuest(selectedLine.finish_quest)
                    }

                    dialog.moveTo(gameState, idx)

                    // [3] if line is exit - perform exit
                    if(selectedLine.exit) {
                        backward()
                        return
                    // [33] else move to another line
                    } else {
                        openDialog(dialog)
                    }
                }
            }
        )
        verticalListElement.centering = false
        for(line in lines) {
            if(line.isConditionsTruly()) {
                verticalListElement.addListElement(TextElement(Assets.FontKurale, "- ${line.text}", screenWidth,0.0f, 0.0f,
                    Color.valueOf("#ffd45f"), null))
            } else {
                val el = TextElement(Assets.FontKurale, "- ${line.text}", screenWidth,0.0f, 0.0f, Color.valueOf("#787878"), null)
                el.isVisible = false
                verticalListElement.addListElement(el)
            }
        }

        addElement("dialogLines", verticalListElement)
    }

    private fun openPerks(gameState: GameState) {
        forward(GameUIStateType.Perks)

        val backing = Backing(gameSkin, screenWidth*0.1f, screenHeight*0.1f, screenWidth*0.8f, screenHeight*0.8f)
        addElements(backing)

        //  (${gameState.player.exp.curExp()}/${gameState.player.exp.maxExp()})
        val expText = "Опыт:"
        val expEl = TextElement(Assets.FontKurale, expText, screenWidth,
            backing.bounds.left() + DefaultPadding*2,
            backing.bounds.top() - DefaultPadding, Color.WHITE,
            Runnable { backward() }, padding = 0.0f)
        addElements(expEl)

        val expBar = PlayerExperienceBar(gameState.player,
            screenWidth*0.1f + DefaultPadding, expEl.bounds.bottom() - DefaultPadding*3, screenWidth*0.5f)
        addElement("playerExpBar", expBar)

        val portraitTex = assetManager[Assets["ui_hero_portrait"]]
        val playerPortrait = TextureBacking(portraitTex, backing.bounds.right() - portraitTex.width - DefaultPadding*1.5f,
            backing.bounds.top() - portraitTex.height - DefaultPadding*0.5f)
        addElement("playerPortrait", playerPortrait)

        val levelText = "Уровень: ${gameState.player.lvl.curLevel()}"
        val levelTextEl = TextElement(
            Assets.FontTimes14, levelText, screenWidth,
            playerPortrait.bounds.left() - TextElement.wordWidth(Assets.FontTimes14, levelText) - DefaultPadding,
            playerPortrait.bounds.top() - DefaultPadding,
            Color.WHITE, null, padding = 0.0f)
        addElements(levelTextEl)

        val ops = gameState.player.lvl.curOperationPoints()
        val opText = "ОП: $ops"
        val opTextEl = TextElement(
            Assets.FontTimes14, opText, screenWidth,
            levelTextEl.bounds.left(), levelTextEl.bounds.bottom() - TextElement.lineHeight(Assets.FontTimes14),
            if(ops > 0) Color.valueOf("#ffe15e") else Color.WHITE,
            Runnable { backward() }, padding = 0.0f)
        addElement("opCounter", opTextEl)

        val propText = "Характеристики:"
        val propEl = TextElement(Assets.FontKurale, propText, screenWidth,
            backing.bounds.left() + DefaultPadding*2,
            playerPortrait.bounds.bottom() + DefaultPadding*2.5f, Color.WHITE,
            Runnable { backward() }, padding = 0.0f)
        addElements(propEl)

        val propFiledHeight = screenHeight*0.2f
        val propField = Backing(screenWidth*0.1f + DefaultPadding, propEl.bounds.bottom() - propFiledHeight - DefaultPadding,
            screenWidth*0.8f - DefaultPadding*2, propFiledHeight, borderColor = Color.valueOf("#c0a077"))
        addElements(propField)
        addElement("statsTable",
            com.kmortyk.game.ui.game.QualificationTable(
                propField.bounds.left(),
                propField.bounds.top(),
                gameState.player
            )
        )

        val skllText = "Способности:"
        val skllEl = TextElement(Assets.FontKurale, skllText, screenWidth,
            backing.bounds.left() + DefaultPadding*2,
            propField.bounds.bottom() - DefaultPadding*0.5f,
            Color.WHITE,
            Runnable { backward() }, padding = 0.0f)
        addElements(skllEl)

        val pt = PerksTable(gameState.playerPerks, assetManager, screenWidth*0.1f, skllEl.bounds.bottom(), object : OnPerkClick {
            override fun onPerkClick(perk: Perk, row: Int, col: Int) {
                if(gameState.player.lvl.curOperationPoints() > 0 &&
                        perk.curLevel < perk.maxLevel()) {
                    openPerkCard(perk)
                }
            }
        })
        addElement("perksTable", pt)

        val menuTex = assetManager[Assets["ui_settings_button"]]
        val menu = TextureButton(
                screenWidth - menuTex.width - DefaultPadding*2,
                screenHeight - menuTex.height - DefaultPadding*2,
                menuTex, Runnable {
            game.openMenu()
        })
        addElements(menu)

        addCloseButton(backing)
    }

    private fun openInventory(gameState: GameState) {
        forward(GameUIStateType.Inventory)

        val backing = Backing(screenWidth*0.1f, screenHeight*0.1f, screenWidth*0.8f, screenHeight*0.8f)
        addElements(backing)

        val invCellSize = 54.0f
        val offsetX = screenWidth * 0.1f + 3.0f
        val offsetY = screenHeight * 0.9f - invCellSize - 3.0f

        addElements(InventoryCells(gameState, 7, 7, offsetX, offsetY))

        addCloseButton(backing)
    }

    private fun openQuests(gameState: GameState) {
        forward(GameUIStateType.Quests)
        gameState.player.questsState.sawAllQuests()

        val quests = gameState.player.questsState.currentQuests

        val backing = Backing(screenWidth*0.1f, screenHeight*0.1f, screenWidth*0.8f, screenHeight*0.8f)
        addElements(backing)

        val infoHeight = backing.bounds.height*0.5f
        val info = PlayerQuestInfo(0.0f, 0.0f,
            backing.bounds.width*0.425f - DefaultPadding*3, infoHeight)

        val questsList = VerticalListElement(gCam,
            backing.bounds.left() + DefaultPadding, backing.bounds.top() - DefaultPadding,
        backing.bounds.width*0.575f, backing.bounds.height - DefaultPadding*2,
        object : OnElementSelected {
            override fun onElementSelected(idx: Int, element: InterfaceElement) {
                info.setQuest(quests[idx])
            }
        }, verticalPadding = 0.0f, extendTex = 0)

        info.offsetTo(questsList.bounds.right() + DefaultPadding, backing.bounds.top() - infoHeight - DefaultPadding)

        addElements(Backing(questsList.bounds, useBatchMatrix=true, borderColor = Color.valueOf("#4d3427")))
        addElement("questsList", questsList)

        for(quest in quests) {
            questsList.addListElement(com.kmortyk.game.ui.game.PlayerQuestListElement(questsList.width, quest))
        }

        if(questsList.size() > 0) {
            questsList.setSelectionElement(Backing(questsList[0].bounds, true, fill = false, borderColor = Color.valueOf("#8ade92")))
            questsList.select(0)
        }

        addElement("questsInfo", info)
        addCloseButton(backing)
    }

    private fun openPerkCard(perk: Perk) {
        forward(GameUIStateType.PerkCardDialog, true)

        val backing = Backing(screenWidth*0.35f, screenHeight*0.2f, screenWidth*0.3f, screenHeight*0.6f)
        backing.isTouchable = true
        addElements(backing)

        val textureElement = TextureRegionBacking(perk.texture,
            backing.bounds.left() + DefaultPadding, backing.bounds.top() - perk.texture.regionHeight - DefaultPadding)
        addElements(textureElement)

        val x = backing.bounds.left() + DefaultPadding
        val y = textureElement.bounds.bottom()
        val lineHeightKurale = TextElement.lineHeight(Assets.FontKurale12, PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize)
        val lineHeightTimes = TextElement.lineHeight(Assets.FontTimes, PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize)

        addElements(
            // perk levels so far
            TextElement(Assets.FontTimes,  "${perk.curLevel}/${perk.maxLevel()}",
                PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize,
                x + PerkRect.PerkRectWidth - TextElement.wordWidth(Assets.FontTimes, "${perk.curLevel}/${perk.maxLevel()}") - DefaultPadding,
                y + lineHeightTimes + DefaultPadding*1.5f,
                Color.WHITE, null, padding = 0.0f),
            // perk name
            TextElement(Assets.FontKurale12, perk.name,
                PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize,
                x + PerkRect.PerkDrawableSize + DefaultPadding, y + PerkRect.PerkRectHeight - DefaultPadding*1.5f,
                Color.WHITE, null, padding = 0.0f),
            // perk stat
            TextElement(Assets.FontKurale12, perk.descriptionShort(),
                PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize,
                x + PerkRect.PerkDrawableSize + DefaultPadding, y + lineHeightKurale + DefaultPadding*1.5f,
                Color.GREEN, null, padding = 0.0f)
        )

        val spentOPText = "Потратить 1 ОП"
        val spentOpEl = TextElement(Assets.FontKurale, spentOPText, screenWidth,
            backing.bounds.right() - TextElement.wordWidth(Assets.FontKurale, spentOPText),
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale) * 2.5f + DefaultPadding, Color.WHITE,
            Runnable {
                when(perk.perkType) {
                    PerkType.Permanent -> {
                        // permanent perks apply just-in-place
                        // so we execute it right now
                        // to change overall state
                        perk.nextLevel()
                        perk.execute()

                        (findElementByName("statsTable") as com.kmortyk.game.ui.game.QualificationTable).update()
                        gameState.player.lvl.spentOperationPoint()
                        updateOpCounter()
                        backward()
                    }
                    PerkType.Skill -> {
                        openSelectPerkSlotDialog(perk, Callback {
                            perk.nextLevel()
                            gameState.player.lvl.spentOperationPoint()
                            updateOpCounter()
                            backward()
                        })
                    }
                    PerkType.Passive -> {
                        // passive perks applies right now
                        perk.nextLevel()
                        perk.execute()

                        (findElementByName("statsTable") as com.kmortyk.game.ui.game.QualificationTable).update()
                        gameState.player.lvl.spentOperationPoint()
                        updateOpCounter()
                        backward()
                    }
                    PerkType.SkillUpgrade -> {
                        // TODO no upgrades yet
                    }
                }
            }, positivePadding = false)
        addElements(spentOpEl)

        val descBacking = Backing(
            backing.bounds.left() + DefaultPadding,
               textureElement.bounds.bottom() - DefaultPadding,
                backing.bounds.width - DefaultPadding * 2,
            (spentOpEl.bounds.top() -(textureElement.bounds.bottom() - DefaultPadding*2)),
            fill = false,
            borderColor = Color.valueOf("#c0a077")
        )
        val descText = TextElement(Assets.FontTimes14, perk.descriptionLong(),
        descBacking.width() - DefaultPadding*2, descBacking.bounds.left() + DefaultPadding,
        descBacking.bounds.bottom() - DefaultPadding, Color.WHITE, null, padding = 0.0f)
        addElements(descBacking, descText)

        addCloseButton(backing)
    }

    private fun openSelectPerkSlotDialog(perk: Perk, callback: Callback) {
        forward(GameUIStateType.SelectPerkSlotDialog, true)

        val backing = Backing(screenWidth*0.35f, screenHeight*0.2f, screenWidth*0.3f, screenHeight*0.6f)
        backing.isTouchable = true
        addElements(backing)

        val textureElement = TextureRegionBacking(perk.texture,
            backing.bounds.left() + DefaultPadding, backing.bounds.top() - perk.texture.regionHeight - DefaultPadding)
        addElements(textureElement)

        val x = backing.bounds.left() + DefaultPadding
        val y = textureElement.bounds.bottom()
        val lineHeightKurale = TextElement.lineHeight(Assets.FontKurale12, PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize)
        val lineHeightTimes = TextElement.lineHeight(Assets.FontTimes, PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize)

        addElements(
            // perk levels so far
            TextElement(Assets.FontTimes,  "${perk.curLevel}/${perk.maxLevel()}",
                PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize,
                x + PerkRect.PerkRectWidth - TextElement.wordWidth(Assets.FontTimes, "${perk.curLevel}/${perk.maxLevel()}") - DefaultPadding,
                y + lineHeightTimes + DefaultPadding*1.5f,
                Color.WHITE, null, padding = 0.0f),
            // perk name
            TextElement(Assets.FontKurale12, perk.name,
                PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize,
                x + PerkRect.PerkDrawableSize + DefaultPadding, y + PerkRect.PerkRectHeight - DefaultPadding*1.5f,
                Color.WHITE, null, padding = 0.0f),
            // perk stat
            TextElement(Assets.FontKurale12, perk.descriptionShort(),
                PerkRect.PerkRectWidth - PerkRect.PerkDrawableSize,
                x + PerkRect.PerkDrawableSize + DefaultPadding, y + lineHeightKurale + DefaultPadding*1.5f,
                Color.GREEN, null, padding = 0.0f)
        )

        val expText = "Выберите слот:"
        val expEl = TextElement(Assets.FontKurale, expText, screenWidth,
            backing.bounds.left() + DefaultPadding*2,
            textureElement.bounds.bottom() - DefaultPadding, Color.WHITE,
            Runnable { backward() }, padding = 0.0f)
        addElements(expEl)

        // perks slots
        val perkSlotTex = assetManager[Assets["ui_perk_slot"]]
        for(i in 0 until 3) {
            val slot = TextureButton(textureElement.bounds.left(), expEl.bounds.bottom() - perkSlotTex.height*(i+1) - DefaultPadding, perkSlotTex, null)
            elements.add(slot)

            val but = TextElement(Assets.FontTimes18, "Выбрать слот ${i+1}", 1000.0f, slot.bounds.right() + DefaultPadding,
            slot.bounds.centerY() + TextElement.lineHeight(Assets.FontTimes18)*0.5f, Color.WHITE, Runnable {

                gameState.player.perkSlots[i] = perk
                (findElementByName("perksSlots") as PerkSlots).updatePerk(i)

                backward()
                callback.run()
                }, padding = 0.0f)
            elements.add(but)
        }

        addCloseButton(backing)
    }

    private fun updateOpCounter() {
        val el = findElementByName("opCounter") ?: return
        val counterEl = el as TextElement
        val ops = gameState.player.lvl.curOperationPoints()

        counterEl.text = "ОП: $ops"
        counterEl.color = if(ops > 0) Color.valueOf("#ffe15e") else Color.WHITE
    }

    private fun addCloseButton(backing: InterfaceElement) {
        val closeText = "Закрыть"
        val closeEl = TextElement(Assets.FontKurale, closeText, screenWidth,
            backing.bounds.right() - TextElement.wordWidth(Assets.FontKurale, closeText),
            backing.bounds.bottom() + TextElement.lineHeight(Assets.FontKurale), Color.WHITE,
            Runnable { backward() }, positivePadding = false)
        addElements(closeEl)
    }
}