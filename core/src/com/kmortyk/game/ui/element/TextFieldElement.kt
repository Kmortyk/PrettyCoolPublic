package com.kmortyk.game.ui.element

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin

interface OnTextChanged {
    fun onTextChanged(oldText: String, newText: String)
}

class TextFieldElement(assetManager: AssetManager,
                       x: Float, y: Float, width: Float, height: Float,
                       val maxChars: Int = 15,
                       val filterChars: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_0123456789",
                       val initText: String = "text"
) : InterfaceElement(x, y, width, height) {
    private val textField: TextField = TextField(initText, assetManager["gdx/skins/uiskin.json", Skin::class.java])
    private val stage = Stage()

    public var onTextChanged: OnTextChanged? = null

    // private val border = Border(x ,y, width, height)

    init {
        stage.keyboardFocus = textField

        textField.setPosition(x, y)
        textField.setSize(width, height)
        textField.maxLength = maxChars
        textField.selectAll()

        if(filterChars.isNotEmpty()) {
            textField.filter = TextField.TextFieldFilter {
                    _, c -> return@TextFieldFilter (c in filterChars)
            }
        }

        textField.stage = stage
    }

    override fun onDraw(assetManager: AssetManager, spriteBatch: SpriteBatch) {
        textField.act(Gdx.graphics.deltaTime)
        textField.draw(spriteBatch, 1.0f)
        // border.draw(assetManager, spriteBatch)
    }

    fun setText(text: String) {
        textField.setText(text)
    }

    fun getText() : String {
        return textField.getText()
    }

    // -- input events -------------------------------------------------------------------------------------------------
    override fun onTouch(x: Float, y: Float): Boolean {
        return textField.defaultInputListener.touchDown(InputEvent(), x, y, 0, 0)
    }

    override fun onTouchUp(x: Float, y: Float): Boolean {
        val ev = InputEvent()
        ev.listenerActor = textField
        textField.defaultInputListener.touchUp(ev, x, y, 0, 0)
        return true
    }

    override fun onTouchDragged(x: Float, y: Float): Boolean {
        val ev = InputEvent()
        ev.listenerActor = textField
        textField.defaultInputListener.touchDragged(ev, x, y, 0)
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        return textField.defaultInputListener.keyDown(InputEvent(), keycode)
    }

    override fun keyUp(keycode: Int): Boolean {
        val old = textField.text
        val result = textField.defaultInputListener.keyUp(InputEvent(), keycode)
        val new = textField.text

        onTextChanged?.onTextChanged(old, new)

        return result
    }

    override fun keyTyped(character: Char): Boolean {
        return textField.defaultInputListener.keyTyped(InputEvent(), character)
    }
}