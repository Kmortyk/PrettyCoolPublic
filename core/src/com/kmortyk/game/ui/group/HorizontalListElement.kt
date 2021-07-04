package com.kmortyk.game.ui.group

import com.kmortyk.game.GameCamera
import com.kmortyk.game.ui.editor.EditorPaneElement
import com.kmortyk.game.ui.element.InterfaceElement
import com.kmortyk.game.ui.screens.GameUI

class HorizontalListElement(
    gCam: GameCamera,
    left: Float, top: Float,
    width: Float, height: Float,
    onElementSelected: OnElementSelected?,
    extendTex: Int = EditorPaneElement.ExtendTexSize,
    selectionPad: Float = 0.0f
) : ListElement(
    gCam,
    left, top,
    width, height,
    onElementSelected,
    extendTex,
    selectionPad) {

    override fun calcOffset(x: Float, y: Float) : Float {
        return lastOffset + x - touchedPoint.x
    }

    override fun alignElement(actualIdx: Int, visibleIdx: Int, el: InterfaceElement, centering: Boolean) {
        val x = left + (blockSize + GameUI.DefaultPadding) * visibleIdx + offset

        if(centering) {
            el.offsetTo(x, top + (height - el.bounds.height)*0.5f - height)
        } else {
            el.offsetTo(x, top - height)
        }
    }
}