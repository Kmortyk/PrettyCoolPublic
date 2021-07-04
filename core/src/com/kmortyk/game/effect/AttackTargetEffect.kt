package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.GridPosition
import com.kmortyk.game.person.Person

class AttackTargetEffect(val person: Person, targetPos: GridPosition, callback: Callback = Callback { /* none */ }) : Effect() {
    companion object {
        const val DefaultSpeed = MovePersonEffect.DefaultSpeed*1.5f
    }

    enum class EffectState {
        Forward,
        Backward
    }

    private val backward: MovePersonEffect = MovePersonEffect(person, person.position.row(), person.position.col(), speed=DefaultSpeed)
    private val forward: MovePersonEffect = MovePersonEffect(person, targetPos.row, targetPos.col, speed=DefaultSpeed)

    private var curState = EffectState.Forward

    init {
        this.callback = callback
    }

    override fun onExtend(delta: Float): Boolean {
        when(curState) {
            EffectState.Forward -> {
                if(!forward.onExtend(delta)) {
                    curState = EffectState.Backward
                }
                return true
            }
            EffectState.Backward -> {
                if(!backward.onExtend(delta)) {
                    return false // end of effect
                }
                return true
            }
        }
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) { /* empty */ }
}