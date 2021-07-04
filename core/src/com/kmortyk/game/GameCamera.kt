package com.kmortyk.game

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import kotlin.math.min
import kotlin.math.max

class GameCamera(val uiCamera: Camera, val gameCamera: Camera) {
    fun projectedBounds(bounds: Rectangle, out: Rectangle) {
        val p1 = uiCamera.project(Vector3(bounds.x, bounds.y, 0.0f))
        val p2 = uiCamera.project(Vector3(bounds.x + bounds.width, bounds.y + bounds.height, 0.0f))

        out.set(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y)
    }
}