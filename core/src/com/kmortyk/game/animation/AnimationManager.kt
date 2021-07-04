package com.kmortyk.game.animation

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.kmortyk.game.Assets

class AnimationManager {
    companion object {
        fun getDefaultFrame(animationName: String): TextureRegion {
            val serialized = Assets.Animations[animationName]!!
            val atlas = Assets.atlas(serialized.atlas)
            return atlas.findRegion("default")
        }
    }

    private val animations: MutableMap<String, GameAnimation> = mutableMapOf()
    private var curAnimation: String = ""

    private var prevAnimation: String = ""
    private var playOnceFlag: Boolean = false

    fun addAnimation(name: String) {
        val serialized = Assets.Animations[name]!!

        val type = when(serialized.type) {
            "loop" -> Animation.PlayMode.LOOP
            else -> Animation.PlayMode.NORMAL
        }

        animations[serialized.name] =
                GameAnimation(
                        serialized.duration,
                        type,
                        Assets.atlas(serialized.atlas),
                        serialized.keyframes
                )
    }

    private fun assertExistence(animationName: String) {
        if(animationName !in animations)
            addAnimation(animationName)
    }

    fun switchTo(animationName: String) {
        assertExistence(animationName)

        curAnimation = animationName
    }

    fun playOnce(animationName: String) {
        assertExistence(animationName)

        prevAnimation = curAnimation
        curAnimation = animationName
        playOnceFlag = true
        animations[animationName]!!.reset()
    }

    fun frame(delta: Float) : TextureRegion {
        if(playOnceFlag && animations[curAnimation]!!.isFinished()) {
            playOnceFlag = false
            curAnimation = prevAnimation
            prevAnimation = ""
        }

        return animations[curAnimation]!!.frame(delta)
    }
}