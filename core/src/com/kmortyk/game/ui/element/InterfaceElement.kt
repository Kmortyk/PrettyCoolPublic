package com.kmortyk.game.ui.element

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.kmortyk.game.PrettyCoolGame

// Used for elements that are constantly on the screen
// in one place and with which you can interact.
abstract class InterfaceElement() {
    // bounds - actionable bound of this element
    var bounds: Rectangle = Rectangle()
    // isVisible - flag that signals about element visibility
    var isVisible = true
    // isTouchable - flag that signals if we can touch element
    var isTouchable = true
    /// DEBUG bounds
    var debugBound: Border? = null

    constructor(left: Float, bottom: Float, w: Float, h: Float) : this() {
        bounds.set(left, bottom, w, h)
        /// DEBUG bounds
        if(PrettyCoolGame.Debug && PrettyCoolGame.ShowUIBounds && this !is Border) {
            debugBound = if(this is Backing && this.useBatchMatrix) {
                Border(bounds, this.useBatchMatrix)
            } else {
                Border(bounds, false)
            }
        }
    }

    open fun contains(x: Float, y: Float): Boolean = bounds.contains(x, y)

    open fun offset(dx: Float, dy: Float) {
        bounds.x += dx
        bounds.y += dy
    }

    open fun offsetTo(newX: Float, newY: Float) {
        bounds.x = newX
        bounds.y = newY
    }

    fun centering() { offset(-bounds.width * 0.5f, -bounds.height * 0.5f) }

    fun scaleBounds(scale: Float) {
        val dw = bounds.width * (1 - scale)
        val dh = bounds.height * (1 - scale)

        bounds.width -= dw
        bounds.height -= dh

        bounds.x += dw * 0.5f
        bounds.y += dh * 0.5f
    }

    // alias
    fun width(): Float { return bounds.width }

    // alias
    fun height(): Float { return bounds.height }

    fun draw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if (isVisible) { // if group is visible
            onDraw(assetManager, spriteBatch)
            /// DEBUG bounds
            if(PrettyCoolGame.Debug && PrettyCoolGame.ShowUIBounds) {
                debugBound?.bounds = bounds
                debugBound?.onDraw(assetManager, spriteBatch)
            }
        }
    }

    fun touch(x: Float, y: Float) : Boolean {
        if (isTouchable)
            return onTouch(x, y)
        return false
    }

    fun touchUp(x: Float, y: Float) : Boolean {
        if(isTouchable)
            return onTouchUp(x, y)
        return false
    }

    fun drag(x: Float, y: Float) : Boolean {
        return onTouchDragged(x, y)
    }

    // onDraw - called to draw an element on the canvas
    abstract fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch)

    // onTouch - called when directly clicked
    // TODO x y naming
    abstract fun onTouch(x: Float, y: Float): Boolean

    // onTouchDown - called when touched down
    // TODO x y naming
    open fun onTouchUp(x: Float, y: Float) : Boolean { return false }

    // onTouchDragged - called when touch dragged
    // TODO x y naming
    open fun onTouchDragged(x: Float, y: Float) : Boolean { return false }

    open fun keyDown(keycode: Int): Boolean { return false }

    open fun keyUp(keycode: Int): Boolean { return false }

    open fun keyTyped(character: Char): Boolean { return false }
}