package com.kmortyk.game.item.actions

import com.kmortyk.game.item.ItemAction
import com.kmortyk.game.sound.SoundMaster
import com.kmortyk.game.state.GameState

class SoundAction(private val soundMaster: SoundMaster, private val soundName: String) : ItemAction() {
    override fun executeOn(gameState: GameState, row: Int, col: Int) {
        soundMaster.playSound(soundName)
    }
}