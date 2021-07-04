package com.kmortyk.game.ui.group

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.kmortyk.game.Assets
import com.kmortyk.game.GameCamera
import com.kmortyk.game.ui.game.InventoryCells
import com.kmortyk.game.ui.element.Backing
import com.kmortyk.game.ui.element.TextElement
import com.kmortyk.game.ui.screens.GameUI

class DropDownList(
    private val gCam: GameCamera,
    val left: Float, val top: Float,
    val width: Float, val height: Float) : ElementsGroup() {

    enum class State { Opened, Closed }

    private val items: MutableList<TextElement> = mutableListOf()
    private val mainText: TextElement = TextElement(Assets.FontTimes18,
        "[text]", width, left+GameUI.DefaultPadding, top+height, Color.valueOf("#b4d272"), null,
        padding = 0.0f)
    private val mainItem: ElementsGroup = ElementsGroup(
        left, top,
        Backing(left, top, width, height, fill = true),
        mainText
    )
    private val secondBacking: Backing = Backing(bounds, fill = true, borderColor = Color.BLACK, useBatchMatrix = true)

    private var curState : State = State.Closed
    private var curIdx = 0
    private val blockSize = 20.0f

    private var offset = 0.0f
    private var lastOffset = 0.0f
    private var touchedPoint: Vector2 = Vector2()
    private var touchedDown: Boolean = false
    private var wasDragged: Boolean = false

    //private val border = Border(bounds)

    protected val cam: Camera = OrthographicCamera(gCam.uiCamera.viewportWidth, gCam.uiCamera.viewportHeight)
    private val projectedBounds: Rectangle = Rectangle()

    init {
        bounds.set(mainItem.bounds)

        cam.translate(cam.viewportWidth * 0.5f, cam.viewportHeight * 0.5f, 0.0f)
        cam.update()
    }

    fun addAllItems(vararg texts: String) {
        for(t in texts)
            addItem(t)
    }

    fun addItem(text: String) {
        if(items.size == 0)
            mainText.text = text

        items.add(TextElement(Assets.FontTimes18, text, 1000.0f, 0.0f, 0.0f, Color.WHITE, null))
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        if(curState == State.Closed) {
            mainItem.draw(assetManager, spriteBatch)
        } else {
            secondBacking.bounds.set(bounds)
            secondBacking.draw(assetManager, spriteBatch)

            spriteBatch.flush() // save drawn image
            if(!ScissorStack.pushScissors(projectedBounds))
                println("[ERROR] can't push vertical list scissors")

            for (idx in 0 until items.size) {
                val itm = items[idx]
                itm.bounds.setPosition(
                       left + GameUI.DefaultPadding,
                    top - (blockSize + GameUI.DefaultPadding*2) * (idx+1) + offset + GameUI.DefaultPadding*2)
                itm.draw(assetManager, spriteBatch)
            }

            spriteBatch.flush()
            ScissorStack.popScissors()

            mainItem.draw(assetManager, spriteBatch)
        }

        //border.bounds.set(bounds)
        //border.draw(assetManager, spriteBatch)
    }

    override fun contains(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        touchedPoint.set(x, y)
        touchedDown = true
        lastOffset = offset
        return false
    }

    override fun onTouchUp(x: Float, y: Float): Boolean {
        if(!wasDragged) {
            if(mainText.contains(x, y)) {
                toggleState()
                return true
            } else if(curState == State.Opened) {
                for (idx in 0 until items.size) {
                    val itm = items[idx]
                    if(itm.contains(x, y)) {
                        curIdx = idx
                        toggleState()
                        return true
                    }
                }
            }
        }

        touchedDown = false
        wasDragged = false
        return true
    }

    override fun onTouchDragged(x: Float, y: Float): Boolean {
        if(!touchedDown || curState != State.Opened)
            return false

        if(touchedPoint.dst2(x, y) > InventoryCells.startDragDst) {
            offset = lastOffset + y - touchedPoint.y
            wasDragged = true
        }
        return true
    }

    private fun toggleState() {
        if(curState == State.Opened) {
            curState = State.Closed
            mainText.text = items[curIdx].text
            bounds.set(mainItem.bounds)
            lastOffset = 0.0f
            offset = 0.0f
        } else {
            curState = State.Opened
            bounds.set(mainItem.bounds)
            for (idx in 0 until items.size) {
                val itm = items[idx]
                itm.bounds.setPosition(
                    left,
                    top - (blockSize + GameUI.DefaultPadding*2) * (idx+1+1) + GameUI.DefaultPadding*2)
                bounds.merge(itm.bounds)
            }
        }

        gCam.projectedBounds(bounds, projectedBounds)
    }

    public fun getSelectedItem() : String {
        return mainText.text
    }

    fun select(item: String) {
        for (idx in 0 until items.size) {
            val itm = items[idx]
            if(itm.text == item) {
                curIdx = idx
                mainText.text = items[curIdx].text
            }
        }
    }
}