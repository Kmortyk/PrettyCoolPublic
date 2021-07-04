package com.kmortyk.game.effect

import com.badlogic.gdx.math.Vector2
import com.kmortyk.game.Position
import com.kmortyk.game.person.Person

class MovePersonToCurPositionEffect(private val person: Person) : ConstEffect() {
    companion object {
        const val DefaultSpeed = 2f
        const val Err = 0.001f
    }

    private var lastDst: Float = Float.MAX_VALUE
    private lateinit var to: Vector2

    override fun create() : ConstEffect {
        to = Vector2(Position.projectGridToActual(person.gridPosition()))
        val pos = person.position.actual
        lastDst = Vector2.dst(pos.x, pos.y, to.x, to.y)
        return this
    }

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
            person.position.actual.set(to.x, to.y)
            false
        } else {
            // save dst
            person.position.actual.set(pos.x, pos.y)
            lastDst = dst
            true
        }
    }

    // TODO make parabola speed
    private fun calcSpeed(dst: Float): Float {
        return DefaultSpeed
    }
}