package com.kmortyk.game.ui.group

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kmortyk.game.ui.element.InterfaceElement
import java.util.*

/**
 * You can work as if with one element.
 */
open class ElementsGroup : InterfaceElement {
    // elements - all interface elements? that belongs to this group
    var elements: MutableList<InterfaceElement> = LinkedList()
    // map for finding elements
    var nameToElement: MutableMap<String, InterfaceElement> = mutableMapOf()

    constructor(cx: Float, cy: Float, w: Float, h: Float) : super(cx, cy, w, h)
    constructor() : super()
    constructor(cx: Float, cy: Float, vararg els: InterfaceElement) : super(cx, cy, 0.0f, 0.0f) {
        for(el in els) {
            addElements(el)
        }
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        elements.forEach {
            if(it.isVisible) // if element in group visible
                it.draw(assetManager, spriteBatch)
        }
    }

    override fun onTouch(x: Float, y: Float) : Boolean {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (e.contains(x, y) && e.touch(x, y)) {
                return true
            }
        }
        return false
    }

    override fun onTouchUp(x: Float, y: Float) : Boolean {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (e.contains(x, y) && e.touchUp(x, y)) {
                return true
            }
        }
        return false
    }

    override fun onTouchDragged(x: Float, y: Float) : Boolean {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (/*e.contains(x, y) &&*/ e.drag(x, y)) {
                return true
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (e.keyDown(keycode)) {
                return true
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (e.keyUp(keycode)) {
                return true
            }
        }
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (e.keyTyped(character)) {
                return true
            }
        }
        return false
    }

    fun addElement(name: String, element: InterfaceElement) {
        elements.add(element)
        nameToElement[name] = element
    }

    fun addElementTo(elements: MutableList<InterfaceElement>, name: String, element: InterfaceElement) {
        elements.add(element)
        nameToElement[name] = element
    }

    fun hideElementsWithNames(vararg names: String) {
        for(name in names) {
            nameToElement[name]!!.isVisible = false
        }
    }

    fun findElementByName(name: String) : InterfaceElement? {
        return nameToElement[name]
    }

    fun findElementByPosition(x: Float, y: Float) : InterfaceElement? {
        for (i in elements.indices.reversed()) {
            val e = elements[i]
            if (e.contains(x, y)) {
                return e
            }
        }
        return null
    }

    @Deprecated("use addElement for future finding")
    fun addElements(vararg es: InterfaceElement) {
        Collections.addAll(elements, *es)
        for(e in es) {
            bounds.merge(e.bounds)
        }
    }

    override fun contains(x: Float, y: Float) : Boolean { // TODO optimize
        for(e in elements)
            if(e.contains(x, y))
                return true
        return false
    }

    override fun offset(dx: Float, dy: Float) {
        super.offset(dx, dy)
        for (e in elements) {
            e.offset(dx, dy)
        }
    }

    override fun offsetTo(newX: Float, newY: Float) {
        val dx: Float = newX - bounds.x
        val dy: Float = newY - bounds.y
        offset(dx, dy)
    }
}