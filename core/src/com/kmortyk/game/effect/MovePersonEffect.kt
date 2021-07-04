package com.kmortyk.game.effect

import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Position
import com.kmortyk.game.person.Person

class MovePersonEffect(private val person: Person, toRow: Int, toCol: Int, private val speed: Float = DefaultSpeed) : Effect() {
    companion object {
        const val DefaultSpeed = 2f
        const val Err = 0.001f
    }

    private val to: Vector2 = Vector2(Position.projectGridToActual(toRow, toCol))
    private var lastDst: Float = Float.MAX_VALUE

    override fun onExtend(delta: Float): Boolean {
        val pos = person.position.actual
        val speed = calcSpeed(Vector2.dst(pos.x, pos.y, to.x, to.y))

        if (pos.x < to.x) pos.x += speed * 1.5f
        if (pos.x > to.x) pos.x -= speed * 1.5f

        if (pos.y < to.y) pos.y += speed
        if (pos.y > to.y) pos.y -= speed

        val dst = Vector2.dst(pos.x, pos.y, to.x, to.y)

        // if the distance change was too small, complete
        return if (lastDst - dst < Err) {
            false
        } else {
            // save dst
            lastDst = dst
            person.position.actual.set(pos.x, pos.y)
            true
        }
    }

    // TODO make parabola speed
    private fun calcSpeed(dst: Float): Float {
        return speed
    }
}