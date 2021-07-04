package com.kmortyk.game.state

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.GridPosition
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.effect.*
import com.kmortyk.game.hexagon.Hexagon
import com.kmortyk.game.item.Item
import com.kmortyk.game.item.ItemType
import com.kmortyk.game.map.GameMap
import com.kmortyk.game.map.LoadPlayerPosType
import com.kmortyk.game.map.MapName
import com.kmortyk.game.person.Person
import com.kmortyk.game.person.PersonState
import com.kmortyk.game.person.Player
import com.kmortyk.game.quest.*
import com.kmortyk.game.scenery.Scenery
import com.kmortyk.game.script.ScriptEngine
import com.kmortyk.game.sound.SoundMaster
import com.kmortyk.game.ui.element.TextElement
import java.util.concurrent.CopyOnWriteArrayList

enum class ControlState {
    TouchToWalk,
    TouchToAttack,
}

class GameState(val game: PrettyCoolGame, val scriptEngine: ScriptEngine) {
    // persons updates with map change, persons[0] == player
    val persons: CopyOnWriteArrayList<Person> = CopyOnWriteArrayList()

    // items represents all items on the map
    val items: MutableList<Item> = mutableListOf()

    // scenery represents all decorations on the map
    val scenery: MutableList<Scenery> = mutableListOf()

    // in every game state we has player somewhere ...
    var player: Player = Player(this@GameState)

    // player overall perks
    val playerPerks: PlayerPerks = PlayerPerks(this@GameState, scriptEngine)

    // current selected item (if nothing selected = -1)
    var selectedItemIdx: Int = 0

    // selected by the user hexagon
    private val selectionHexagon: Hexagon = Hexagon(0, 0, Assets.atlas("hexes").findRegion("hex_select"))

    // gameMap - often changes with screen updates
    lateinit var gameMap: GameMap

    var mapsProjectName = "mainStory"

    // object that controls game statistic
    val statistic: GameStatistic = GameStatistic()

    var controlState: ControlState = ControlState.TouchToWalk

    // createGameMap - crates new game map or change current to the new
    fun createGameMap() {
        gameMap = GameMap()

        var mapName = "a1m0"
        if(PrettyCoolGame.Debug && PrettyCoolGame.MapPlayroom) {
            mapName = "playroom"
        }

        gameMap.loadMapFile(this, mapsProjectName, mapName, LoadPlayerPosType.LoadFromFile, null)
        selectionHexagon.setPosition(player.position.row(), player.position.col())
        game.addEffect(MapNameEffect(game, gameMap.description))

        /// DEBUG
        player.items.addItem(Item(this, "itm_pistol_ammo"))
        player.items.addItem(Item(this, "itm_pistol_ammo"))
        player.items.addItem(Item(this, "itm_shotgun_ammo"))
        player.items.addItem(Item(this, "itm_shotgun_ammo"))

        //game.putConstEffect("rain", RainEffect())
        //game.putConstEffect("snow", SnowEffect())
        //game.addEffect(PopUpText(player.position.actualX(), player.position.actualY(), "Hello, I'm PopUp!"))
        val q1 = BringItemToMeQuest(this, QuestData("", "", QuestType.BringItem, "My hat", "This is test description", mutableMapOf(QUEST_KEY_ITEM_NAME to "hat")))
        val q2 = BringItemToMeQuest(this, QuestData("", "", QuestType.Dummy, "Dummy quest", "This is test description", mutableMapOf(QUEST_KEY_ITEM_NAME to "hat")))

        q1.isFinishedCache = true
        q2.finishQuest()

        player.questsState.addQuest(q1)
        player.questsState.addQuest(q2)

        game.putConstEffect("movePlayer", MovePersonToCurPositionEffect(player))
        for(p in persons) {
            if(p != player)
                game.putConstEffect(p.toString(), p.movePersonEffect)
        }

        SoundMaster.playSound("wind_water", true)
    }

    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        // draw game map
        gameMap.draw(assetManager, spriteBatch)
        // draw ui-selection hexagon
        selectionHexagon.draw(assetManager, spriteBatch)
        // draw all entities
        gameMap.drawEntities(assetManager, spriteBatch)
        /// DEBUG PLAYER POSITION
        if(PrettyCoolGame.Debug && PrettyCoolGame.DrawPlayerPos) {
            TextElement(Assets.FontTimes, "${player.position.row()}-${player.position.col()}",
            1000f, player.position.actualX(), player.position.actualY(), Color.RED, null)
                    .draw(assetManager, spriteBatch)
        }
    }

    fun changeControlTo(controlState: ControlState) {
        game.gameDrawFun().updateControlState(controlState)
        this.controlState = controlState
    }

    // touch - user's touch at some point at the map
    // changes [player] state
    fun touch(worldX: Int, worldY: Int) {
        val selectedHex = findHexAt(worldX, worldY)
        if(selectedHex == null || selectedHex.gridPosition() != player.gridPosition()) {
            game.gameDrawFun().focusCameraOnPlayer()
        }
        if(selectedHex != null) {
            if(controlState == ControlState.TouchToWalk) {
                // reset journal before perform action
                player.journal.reset()
                // perform action
                touchToWalk(selectedHex)
            } else if(controlState == ControlState.TouchToAttack) {
                if(selectedHex.hasPerson() && player.canAttack(this, selectedHex.personSlot!!)) {
                    // reset journal before perform action
                    player.journal.reset()
                    // perform action
                    touchToAttack(selectedHex)
                }
                changeControlTo(ControlState.TouchToWalk)
            }
        }
    }

    private fun touchToWalk(selectedHex: Hexagon) {
        // set direction to draw
        if(player.gridPosition() != selectedHex.gridPosition())
            player.setDestination(selectedHex.gridPosition())

        // update ui
        selectionHexagon.setPosition(selectedHex.row(), selectedHex.col())

        // Player position == selected position
        if(player.gridPosition() == selectedHex.gridPosition()) {
            player.direction = Person.oppositeDirection(player.direction)
            // [1]. Hexagon has item to pick
            if(selectedHex.hasItems()) {
                pickUpItems(selectedHex)
                return step() // make game step
            }
            // Player position is nearby selected hexagon
        } else {
            // [2]. Hexagon has talking person - open dialog
            if(player.isNearPos(selectedHex.gridPosition()) && selectedHex.hasTalkingPerson()) {
                val talkingPerson = selectedHex.personSlot!!
                game.gameDrawFun().openDialog(talkingPerson.dialog!!)
            } else {
                // hexagon has no talking person - close dialog if opened
                game.gameDrawFun().uiBackward()

                // [3]. Hexagon has aggressive mob - attack if possible
                if(selectedHex.hasEnemyPerson() && player.canAttack(this, selectedHex.personSlot!!)) {
                    val person = selectedHex.personSlot!!
                    player.attack(this, person)
                    game.getConstEffect("movePlayer").disable()
                    game.addEffect(AttackTargetEffect(player, person.gridPosition(), callback=Callback {
                        touchFinished()
                        game.getConstEffect("movePlayer").enable()
                    }))
                    return // force wait player's animation
                } else {
                    // [4]. Apply item to scenery or person
                    if(player.isNearPos(selectedHex.gridPosition()) && selectedHex.blockedByScenery()) {
                        game.addEffect(QuestionEffect(game.assetManager, player))
                        // [5]. Hexagon [maybe] empty, and player [maybe] can move towards it
                    } else {
                        val path = gameMap.findPath(player.gridPosition(), selectedHex.gridPosition())
                        if(path.size > 0) {
                            val nextPosition = path[0]
                            if(!gameMap.isHexBlocked(nextPosition)) {
                                // move player to new position
                                movePersonTo(player, nextPosition)
                                // add moving effect
                                if(!gameMap.isExitHex(nextPosition)) {
                                    game.getConstEffect("movePlayer").create()
                                }
                            }
                        }
                    }
                }
            }
        }

        touchFinished()
    }

    private fun touchToAttack(selectedHex: Hexagon) {
        player.attack(this, selectedHex.personSlot!!, player.attackPower)
        touchFinished()
    }

    // callback function for touch
    private fun touchFinished() {
        player.modifiers.apply() // apply player's modifiers

        step() // make game step

        // check and load next map if hexagon has exit in it
        if(gameMap.isExitHex(player.gridPosition())) {
            gameMap.loadNextMapWithExitHex(this, player.gridPosition())
            game.addEffect(MapNameEffect(game, gameMap.description))
            game.putConstEffect("movePlayer", MovePersonToCurPositionEffect(player))
            game.gameDrawFun().focusCameraOnPlayer()
        }

        // other stuff
        SoundMaster.playSound("step")
    }

    // step - changer game's state from one correct value to another
    private fun step() {
        for(person in persons.subList(1, persons.size)) {
            when(person.state) {
                PersonState.Stand -> { /* nothing to do, just stand */ }
                PersonState.Walking -> {
                    // TODO random walking
                    val randRow = (Math.random() * gameMap.rows()).toInt()
                    val randCol = (Math.random() * gameMap.cols()).toInt()

                    val path = gameMap.findPath(person.gridPosition(), GridPosition(randRow, randCol))
                    person.setDestination(GridPosition(randRow, randCol))

                    if(path.size > 0) {
                        val nextPosition = path[0]
                        if(!gameMap.isHexBlocked(nextPosition)) {
                            movePersonTo(person, nextPosition)
                            person.movePersonEffect.create()
                        }
                    }
                }
                PersonState.Aggressive -> {
                    val target = player
                    val seenBefore = person.sawPerson(target)

                    person.updateSeenPersons(this)
                    // walk only if only seen at previous step
                    if(person.sawPerson(target) && seenBefore) {
                        person.setDestination(target.gridPosition())

                        if(person.canAttack(this, target)) {
                            person.attack(this, target)
                            game.addEffect(AttackTargetEffect(person, target.gridPosition()))
                        } else {
                            val path = gameMap.findPath(person.gridPosition(), target.gridPosition())
                            if(path.size > 0) {
                                val nextPosition = path[0]
                                if(!gameMap.isHexBlocked(nextPosition)) {
                                    movePersonTo(person, nextPosition)
                                    person.movePersonEffect.create()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun findHexAt(worldX: Int, worldY: Int) : Hexagon? {
        gameMap.hexes.forEach {
            for(hex in it) {
                if(hex.contains(worldX, worldY))
                    return hex
            }
        }
        // not found
        return null
    }

    fun addPerson(p: Person) {
        // occupy slot
        gameMap[p.position].personSlot = p
        // add to list of persons
        persons.add(p)
        // update person's state
        p.updateObservableHexes(this)
    }

    fun addPlayer(p: Player) {
        // occupy slot
        gameMap[p.position].personSlot = p
        if(persons.size > 0)
            persons[0] = p
        else
            persons.add(p)
        // update person's state
        p.updateObservableHexes(this)
    }

    fun addItem(it: Item) {
        // extend slots
        gameMap[it.position].itemSlots.add(it)
        // add to list of items
        items.add(it)
    }

    fun addScenery(scn: Scenery) {
        // extend slots
        gameMap[scn.position].scenerySlots.add(scn)
        // add to list of items
        scenery.add(scn)
    }

    private fun pickUpItems(hex: Hexagon) {
        val hexItems = gameMap[hex.gridPosition()].itemSlots
        hexItems.forEach {
            game.addEffect(ItemPickUpEffect(game.assetManager, it))

            if(it.type == ItemType.ActivateOnPickUp) {
                it.executeActions(this, player.position.row(), player.position.col())
                items.remove(it)
            } else if(player.items.addItem(it)) {
                items.remove(it)
            }
        }
        hexItems.clear()

        if(selectedItemIdx == -1) {
            selectedItemIdx = 0
        }

        SoundMaster.playSound("pick_up_weapon")
    }

    fun removePerson(attacker: Person, target: Person) {
        if(attacker == player) {
            // TODO calculate kill-exp
            player.lvl.addExp(10)
        }

        game.addEffect(DieEffect(target))

        // drop loot
        val loot = target.generateLoot(this, statistic)
        for(item in loot) {
            // by default set to current person position
            item.position.setGrid(target.gridPosition(), true)
            val pos = target.randomNearPos(this, statistic)
            // throw item near if possible with effect
            item.position.setGrid(pos, false)
            game.addEffect(MoveItemEffect(item, pos))
            // add item to state
            addItem(item)
        }

        val hex = gameMap[target.gridPosition()]
        hex.personSlot = null
        persons.remove(target)
    }

    private fun movePersonTo(p: Person, newPosition: GridPosition) {
        // clean-up slot
        gameMap[p.position].personSlot = null
        // occupy new slot
        gameMap[newPosition].personSlot = p
        // update person's position
        p.updatePosition(this, newPosition, updateActual=false)
        // trigger actions
        if(p is Player) {
            gameMap[newPosition].triggerActions(this)
        }
    }

    fun clearState() {
        persons.clear()
        items.clear()
        scenery.clear()
    }

    fun isItemSelected() = selectedItemIdx <= player.items.size() - 1

    fun loadMap(mapName: MapName, entrypoint: GridPosition?) {
        if(entrypoint != null)
            gameMap.loadMapFile(this, mapName.projectName, mapName.fileName, LoadPlayerPosType.SetEntrypoint, entrypoint)
        else
            gameMap.loadMapFile(this, mapName.projectName, mapName.fileName, LoadPlayerPosType.LoadFromFile, null)

        game.addEffect(MapNameEffect(game, gameMap.description))
        game.putConstEffect("movePlayer", MovePersonToCurPositionEffect(player))
        game.gameDrawFun().focusCameraOnPlayer()
    }
}