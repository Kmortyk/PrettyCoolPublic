package com.kmortyk.game.effect

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.ui.element.Backing

// refactored effect from
// https://github.com/Kmortyk/canekoandthechessking/blob/master/CanekoAndTheChessKing
class MoveCameraEffect(
        val cam: Camera,
        val camBounds: Rectangle?,
        toX: Float, toY: Float) : Effect() {

    companion object {
        const val Speed = 2f
        const val Err = 0.2f
    }

    init {
        normalizeCamBounds()
    }

    private val pos: Vector3 = cam.position
    private val to: Vector2 = Vector2(toX, toY)

    private var lastPos: Vector2 = Vector2(pos.x, pos.y)
    private var lastDst: Float = pos.dst(to.x, to.y, 0.0f)

    override fun onExtend(delta: Float) : Boolean {
        val pos = cam.position
        val speed = calcSpeed(Vector2.dst(pos.x, pos.y, to.x, to.y))

        if (pos.x < to.x) pos.x += speed * 1.5f
        if (pos.x > to.x) pos.x -= speed * 1.5f

        if (pos.y < to.y) pos.y += speed
        if (pos.y > to.y) pos.y -= speed

        if(camBounds != null) {
            if(pos.x < camBounds.x) pos.x = camBounds.x
            if(pos.x > camBounds.x + camBounds.width) pos.x = camBounds.x + camBounds.width
            if(pos.y < camBounds.y) pos.y = camBounds.y
            if(pos.y > camBounds.y + camBounds.height) pos.y = camBounds.y + camBounds.height
        }

        val dst = Vector2.dst(pos.x, pos.y, to.x, to.y)

        // if the distance change was too small, complete
        return if (lastDst - dst < Err) {
            false
        } else {
            // save [this] method value
            lastPos.set(pos.x, pos.y)
            // save dst
            lastDst = dst
            cam.position.set(pos.x, pos.y, 0.0f)
            true
        }
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(PrettyCoolGame.Debug) {
            //val b = Backing(camBounds, true)
            //b.draw(assetManager, spriteBatch)
            //val b2 = Backing(Rectangle(pos.x, pos.y, 10.0f, 10.0f), true)
            //b2.draw(assetManager, spriteBatch)
        }
    }

    // dst2, dst - the closer, the lower the speed
    // speed, delta - smooth
    private fun calcSpeed(dst: Float): Float {
        // linear
        // var v: Float = Speed * dst
        // v /= 100f

        // quadratic
         var v = Speed * dst * dst - dst
             v /= 10000
        return v
    }

    private fun normalizeCamBounds() {
        if(camBounds == null)
            return

        if(camBounds.width < 0) {
            camBounds.x += camBounds.width
            camBounds.width = kotlin.math.abs(camBounds.width)
        }
        if(camBounds.height < 0) {
            camBounds.y += camBounds.height
            camBounds.height = kotlin.math.abs(camBounds.height)
        }
    }
}