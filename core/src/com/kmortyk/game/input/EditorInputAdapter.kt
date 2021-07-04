package com.kmortyk.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.kmortyk.game.Assets
import com.kmortyk.game.PrettyCoolGame
import com.kmortyk.game.map.editor.EditorState
import com.kmortyk.game.state.GameState
import com.kmortyk.game.ui.element.TextureBacking
import com.kmortyk.game.ui.group.ElementsGroup

// process input directly to game's state
// camera is used for projection screenCoords -> worldCoords
class EditorInputAdapter(private val game: PrettyCoolGame,
                         private val gameCamera: Camera,
                         private val uiCamera: Camera,
                         private val ui: ElementsGroup,
                         private val editorState: EditorState) : InputAdapter() {

    // -- interface ----------------------------------------------------------------------------------------------------
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val uiPos = uiPos(screenX, screenY)
        val worldPos = worldPos(screenX, screenY)

        if (!ui.touch(uiPos.x, uiPos.y)) {
            editorState.touch(worldPos.x, worldPos.y)
        }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val uiPos = uiPos(screenX, screenY)
        val worldPos = worldPos(screenX, screenY)

        if (!ui.touchUp(uiPos.x, uiPos.y)) {
            editorState.touchUp(worldPos.x, worldPos.y)
        }

        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val uiPos = uiPos(screenX, screenY)
        val worldPos = worldPos(screenX, screenY)

        if (!ui.drag(uiPos.x, uiPos.y)) {
            editorState.drag(worldPos.x, worldPos.y)
        }

        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        return ui.keyDown(keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        return ui.keyUp(keycode)
    }

    override fun keyTyped(character: Char): Boolean {
        return ui.keyTyped(character)
    }

    // -- utils --------------------------------------------------------------------------------------------------------
    private fun worldPos(screenX: Int, screenY: Int) : Vector3 {
        return gameCamera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0.0f))
    }

    private fun uiPos(screenX: Int, screenY: Int) : Vector2 {
        val uiX = screenX.toFloat()
        val uiY = screenY.toFloat()

        val posVec3 = uiCamera.unproject(Vector3(uiX, uiY, 0.0f))

        /// DEBUG touched position
        if(PrettyCoolGame.Debug && PrettyCoolGame.DrawTouchedPosEditor) {
            ui.addElement("dbg_touched", TextureBacking(
                game.assetManager[Assets["ui_debug_grid_pos"]], posVec3.x, posVec3.y)
            )
        }

        return Vector2(posVec3.x, posVec3.y)
    }
}