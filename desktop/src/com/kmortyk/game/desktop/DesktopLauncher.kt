package com.kmortyk.game.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.kmortyk.game.PrettyCoolGame

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()

        // configure
        config.forceExit = false
        config.width = PrettyCoolGame.ScreenWidth
        config.height = PrettyCoolGame.ScreenHeight

        LwjglApplication(PrettyCoolGame(), config)
    }
}