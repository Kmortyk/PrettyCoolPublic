package com.kmortyk.game.animation

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.Assets
import com.kmortyk.game.ui.element.InterfaceElement

class Particle(name: String, cx: Float, cy: Float) : InterfaceElement(cx, cy, 0.0f, 0.0f) {
    private val animation: GameAnimation
    private val duration: Float
    private val framesCount: Int

    init {
        val p = Assets.Particles["$name.atlas"]!!
        val atlas = Assets.atlas(p.name)

        val regions = atlas.regions
        val keyframes = Array(regions.size) { "" }
        for(i in 0 until regions.size) {
            keyframes[i] = regions[i].name
        }

        val type = when(p.type) {
            "loop" -> Animation.PlayMode.LOOP
            "pong" -> Animation.PlayMode.LOOP_PINGPONG
            else -> Animation.PlayMode.NORMAL
        }

        duration = p.duration
        framesCount = keyframes.size
        animation = GameAnimation(p.duration, type, atlas, keyframes)
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        val frame = animation.frame(Gdx.graphics.deltaTime)
        spriteBatch.draw(frame,
                bounds.x + frame.regionWidth*0.5f,
                bounds.y + frame.regionHeight*0.5f)
    }

    override fun onTouch(x: Float, y: Float): Boolean { return false }

    fun randomOffset() {
        val o = Math.random()*animation.framesCount()
        animation.frame((duration*o).toFloat())
    }

    fun setPlayMode(pm: Animation.PlayMode) {
        animation.animation.playMode = pm
    }

    fun normalizeDuration() {
        val newStateTime = animation.stateTime % animation.animation.animationDuration
        if(newStateTime < animation.animation.animationDuration*0.2f)
            return
        animation.stateTime = newStateTime
    }

    fun isFinished() : Boolean {
        return animation.isFinished()
    }
}