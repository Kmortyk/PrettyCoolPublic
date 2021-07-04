package com.kmortyk.game.ui.group

import com.kmortyk.game.GameCamera
import com.kmortyk.game.ui.editor.EditorPaneElement
import com.kmortyk.game.ui.element.InterfaceElement
import com.kmortyk.game.ui.screens.GameUI

class VerticalListElement(
    gCam: GameCamera,
    left: Float, top: Float,
    width: Float, height: Float,
    onElementSelected: OnElementSelected?,
    extendTex: Int = EditorPaneElement.ExtendTexSize,
    selectionPad: Float = 0.0f,

    val verticalPadding: Float = GameUI.DefaultPadding,
    yAdjust: Float = 1.0f,
    xAdjust: Float = 1.0f

) : ListElement(
    gCam,
    left, top,
    width, height,
    onElementSelected,
    extendTex,
    selectionPad,
    yAdjust,
    xAdjust) {

    override fun calcOffset(x: Float, y: Float) : Float {
        return lastOffset + y - touchedPoint.y
    }

    override fun alignElement(actualIdx: Int, visibleIdx: Int, el: InterfaceElement, centering: Boolean) {
        val y = top - (blockSize + verticalPadding * 2) * (visibleIdx + 1) + offset + verticalPadding * 2

        if (centering) {
            el.offsetTo(left + (width - el.bounds.width) * 0.5f, y)
        } else {
            el.offsetTo(left, y)
        }
    }
}