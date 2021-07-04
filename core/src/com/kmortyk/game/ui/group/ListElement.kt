package com.kmortyk.game.ui.group

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.kmortyk.game.GameCamera
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.log
import com.kmortyk.game.ui.game.InventoryCells
import com.kmortyk.game.ui.editor.EditorPaneElement
import com.kmortyk.game.ui.element.Border
import com.kmortyk.game.ui.element.InterfaceElement
import com.kmortyk.game.ui.element.TextureRegionBacking

interface OnElementSelected {
    fun onElementSelected(idx: Int, element: InterfaceElement)
}

abstract class ListElement (
    private val gCam: GameCamera,
    val left: Float, val top: Float,
    val width: Float, val height: Float,
    private val onElementSelected: OnElementSelected?,
    private val extendSelectionTextureSize: Int = EditorPaneElement.ExtendTexSize,
    private val selectionPadding: Float = 0.0f,
    private val yAdjust: Float = 1.0f,
    private val xAdjust: Float = 1.0f) : ElementsGroup(left, top - height, width, height) {

    protected val list: MutableList<InterfaceElement> = mutableListOf()
    protected var blockSize = 0.0f
    protected var offset = 0.0f
    protected var lastOffset = 0.0f

    protected var touchedPoint: Vector2 = Vector2()
    private var touchedDown: Boolean = false
    private var wasDragged: Boolean = false

    private var selectedIdx: Int = 0
    protected var selection: InterfaceElement? = null

    var centering = true

    protected val cam: Camera = OrthographicCamera(gCam.uiCamera.viewportWidth, gCam.uiCamera.viewportHeight)

    private val projectedBounds: Rectangle = Rectangle()
    private lateinit var border: Border

    init {
        alignElements()

        gCam.projectedBounds(bounds, projectedBounds)
        projectedBounds.x -= xAdjust
        projectedBounds.width += xAdjust*2
        projectedBounds.y -= yAdjust
        projectedBounds.height += yAdjust*2

        cam.translate(cam.viewportWidth * 0.5f, cam.viewportHeight * 0.5f, 0.0f)
        cam.update()

        /// DEBUG show scissors bounds
        if(PrettyCoolGame.Debug && PrettyCoolGame.DrawScissors) {
            border = Border(bounds, borderColor = Color.BLUE, useBatchMatrix = false)
        }
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        spriteBatch.flush() // save drawn image

        if (!ScissorStack.pushScissors(projectedBounds)) {
            log.error("can't push list scissors $projectedBounds")
            return
        }

        super.onDraw(assetManager, spriteBatch)
        if (list.size > 0)
            selection?.draw(assetManager, spriteBatch)

        spriteBatch.flush()

        ScissorStack.popScissors()

        /// DEBUG show scissors bounds
        if(PrettyCoolGame.Debug && PrettyCoolGame.DrawScissors) {
            border.bounds.set(projectedBounds)
            border.draw(assetManager, spriteBatch)
        }
    }

    fun addListElement(el: InterfaceElement) {
        if (el.bounds.height > blockSize) {
            blockSize = el.bounds.height
        }

        list.add(el)
        addElement("list${list.size}", el)

        alignElements()
    }

    fun alignElements() {
        var visibleIdx = 0

        for (idx in 0 until list.size) {
            if(list[idx].isVisible) {
                alignElement(idx, visibleIdx, list[idx], centering)
                visibleIdx++
            }
        }
        selectAlign(selectedIdx)
    }

    abstract fun alignElement(actualIdx: Int, visibleIdx: Int, el: InterfaceElement, centering: Boolean)
    abstract fun calcOffset(x: Float, y: Float) : Float

    override fun onTouchDragged(x: Float, y: Float): Boolean {
        if(!touchedDown)
            return false

        if(touchedPoint.dst2(x, y) > InventoryCells.startDragDst) {
            offset = calcOffset(x, y)
            alignElements()
            wasDragged = true
        }
        return true
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        touchedPoint.set(x, y)
        touchedDown = true
        lastOffset = offset
        return true
    }

    override fun onTouchUp(x: Float, y: Float): Boolean {
        if (!wasDragged) {
            val selectedIdx = findSelectedIdx(x, y)
            if (selectedIdx != -1) {
                if (selection != null)
                    selectAlign(selectedIdx)
                onElementSelected?.onElementSelected(selectedIdx, list[selectedIdx])
            }
        }

        touchedDown = false
        wasDragged = false
        return true
    }

    fun selectAlign(selectedIdx: Int) {
        if (list.size == 0)
            return
        val el = list[selectedIdx]
        selection?.bounds?.set(
            el.bounds.x + extendSelectionTextureSize,
            el.bounds.y + extendSelectionTextureSize - selectionPadding,
            bounds.width,
            blockSize + selectionPadding
        )
        this.selectedIdx = selectedIdx
    }

    fun select(selectedIdx: Int) {
        if (list.size == 0)
            return
        this.selectedIdx = selectedIdx
        onElementSelected?.onElementSelected(selectedIdx, list[selectedIdx])
    }

    private fun findSelectedIdx(x: Float, y: Float): Int {
        for (idx in 0 until list.size) {
            if (list[idx].isVisible && list[idx].contains(x, y))
                return idx
        }
        return -1
    }

    override fun contains(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    fun clear() {
        for (el in list) {
            elements.remove(el)
        }
        list.clear()
        //visibleFlags.clear()
        selectedIdx = 0
        blockSize = 0.0f
        offset = 0.0f
        lastOffset = 0.0f
        touchedPoint.set(0.0f, 0.0f)
        touchedDown = false
        wasDragged = false
    }

    fun setSelectionElement(selection: InterfaceElement?) {
        this.selection = selection
        if (list.size > 0)
            selectAlign(0)
    }

    fun setSelectionTexture(selectionTex: TextureRegion?) {
        if(selectionTex != null)
            setSelectionElement(TextureRegionBacking(selectionTex, 0.0f, 0.0f))
    }

    public fun size() = list.size

    public operator fun get(idx: Int) = list[idx]
}