package com.kmortyk.game.person

import com.kmortyk.game.item.Item
import com.kmortyk.game.item.ItemType
import com.kmortyk.game.item.actions.AttackAction
import com.kmortyk.game.person.perk.Perk
import com.kmortyk.game.sound.SoundMaster
import com.kmortyk.game.state.*

class Player(val gameState: GameState) : Person("player", 0, 0) {

    // items of the player (and only)
    val items: PlayerItems = PlayerItems()

    // level state
    val lvl: PlayerLevelState = PlayerLevelState()

    // stats state
    val stats: PlayerStats = PlayerStats()

    // quests state
    val questsState: PlayerQuestsState = PlayerQuestsState()

    // active perks
    val perkSlots: Array<Perk?> = Array(3) { null }

    // perk modifiers
    val modifiers: PlayerModifiers = PlayerModifiers()

    // journal holds metadata for perks or other modifiers
    val journal: PlayerStepJournal = PlayerStepJournal()


    init { animationManager.switchTo("player_iddle") }

    override fun attack(gameState: GameState, target: Person, power: Int) {
        if(gameState.isItemSelected()) {
            val item = items[gameState.selectedItemIdx]
            if(item != null && item.type == ItemType.ActivateOnWorldUse) {
                item.executeActions(gameState, target.position.row(), target.position.col())
            }
        } else {
            // decrease target hp's
            target.healthPoints -= power
            if(target.healthPoints <= 0) {
                gameState.removePerson(this, target)
            }
            // focus on player after attack
            target.focusOnTarget(gameState, this)
            // update journal
            journal.attack(power)
            // play punch sound
            SoundMaster.playSound("punch")
        }
    }

    override fun canAttack(gameState: GameState, target: Person) : Boolean {
        if(gameState.isItemSelected()) {
            val item = items[gameState.selectedItemIdx]
            if(item != null && item.type == ItemType.ActivateOnWorldUse) {
                for(act in item.actions()) {
                    if(act is AttackAction)
                        return act.canAttack(target.gridPosition())
                }
            }
        }

        return super.canAttack(gameState, target)
    }

    fun ammoItemForSelectedWeapon(gameState: GameState) : Item? {
        if(gameState.isItemSelected()) {
            val item = items[gameState.selectedItemIdx]
            if(item != null && item.type == ItemType.ActivateOnWorldUse) {
                for(act in item.actions()) {
                    if(act is AttackAction) {
                        val ammoItemName = act.ammoName ?: return null
                        return items.getResource(ammoItemName)
                    }
                }
            }
        }
        return null
    }
}