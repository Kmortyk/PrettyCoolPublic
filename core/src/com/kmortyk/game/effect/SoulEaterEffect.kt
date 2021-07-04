package com.kmortyk.game.effect

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Assets
import com.kmortyk.game.Position
import com.kmortyk.game.person.Person

class SoulEaterEffect(val person: Person) : Effect() {
    companion object {
        val duration: Float = 1.5f
    }

    var time: Float = 0.0f
    val texDesc = Assets["eff_sl_etr"]
    val ctr: Vector2 = Vector2()
    val prsPos: Vector2 = Vector2()

    override fun onExtend(delta: Float): Boolean {
        time += Gdx.graphics.deltaTime
        return time <= duration
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val ctr = Position.getHexCenter(person.gridPosition(), ctr)
        val tex = assetManager[texDesc]

        person.getDrawPosition(prsPos)

        spriteBatch.draw(tex, ctr.x - tex.width*0.5f, prsPos.y)
    }
}