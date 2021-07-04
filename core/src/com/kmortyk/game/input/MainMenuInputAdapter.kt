package com.kmortyk.game.input

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.kmortyk.game.Assets
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.ui.element.TextureBacking
import com.kmortyk.game.ui.group.ElementsGroup

// process input directly to game's state
// camera is used for projection screenCoords -> worldCoords
class MainMenuInputAdapter(
    private val game: PrettyCoolGame,
    private val camera: Camera,
    private val ui: ElementsGroup
    ) : InputAdapter() {

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val uiPos = uiPos(screenX, screenY)
        return ui.touch(uiPos.x, uiPos.y)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val uiPos = uiPos(screenX, screenY)
        return ui.touchUp(uiPos.x, uiPos.y)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val uiPos = uiPos(screenX, screenY)
        return ui.drag(uiPos.x, uiPos.y)
    }

    private fun uiPos(screenX: Int, screenY: Int) : Vector2 {
        val uiX = screenX.toFloat()
        val uiY = screenY.toFloat()

        val posVec3 = camera.unproject(Vector3(uiX, uiY, 0.0f))

        /// DEBUG touched position
        if(PrettyCoolGame.Debug && PrettyCoolGame.DrawTouchedPosMenu) {
            ui.addElement("dbg_touched", TextureBacking(
                game.assetManager[Assets["ui_debug_grid_pos"]], posVec3.x, posVec3.y
            ))
        }

        return Vector2(posVec3.x, posVec3.y)
    }
}