package com.kmortyk.game.animation

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

class GameAnimation(duration: Float, type: Animation.PlayMode, atlas: TextureAtlas, keyframesNames: kotlin.Array<String>) {

    val animation: Animation<TextureRegion>
    var stateTime: Float = 0.0f

    init {
        val keyframes = mutableListOf<TextureRegion>()
        for(kfn in keyframesNames) {
            keyframes.add(atlas.findRegion(kfn))
        }

        animation = Animation(duration, Array(keyframes.toTypedArray()), type)
    }

    fun frame(delta: Float) : TextureRegion {
        stateTime += delta
        return animation.getKeyFrame(stateTime)
    }

    fun isFinished() : Boolean {
        return animation.isAnimationFinished(stateTime)
    }

    fun reset() {
        stateTime = 0.0f
    }

    fun framesCount() : Int {
        return animation.keyFrames.size
    }
}