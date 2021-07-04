package com.kmortyk.game.script

import com.kmortyk.game.*
import com.kmortyk.game.effect.PopUpText
import com.kmortyk.game.modifier.ModifierStat
import com.kmortyk.game.script.Interpreter.Companion.AnyArity
import com.kmortyk.game.script.error.Message
import com.kmortyk.game.script.error.SuccessMessage
import com.kmortyk.game.state.GameState

import java.io.File

class ScriptEngine(val game: PrettyCoolGame) {

    // test run, full path
    fun executeFile(path: String) : Message {
        val f = File(path)
        println("${LogColors.CYAN}RUN SCRIPT ${LogColors.YELLOW}:${LogColors.GREEN}${f.name}${LogColors.RESET}${LogColors.YELLOW}:")
        println("${LogColors.CYAN}OUTPUT${LogColors.YELLOW}'${LogColors.RESET}\n")

        val program: String = readFile(path)
        return execute(program)
    }

    // in-game run
    fun executeScript(scriptAssetPath: String) {
        println("${LogColors.CYAN}RUN SCRIPT ${LogColors.YELLOW}:${LogColors.GREEN}${scriptAssetPath}${LogColors.RESET}${LogColors.YELLOW}:")
        println("${LogColors.CYAN}OUTPUT${LogColors.YELLOW}'${LogColors.RESET}\n")

        execute(Assets.readFile(scriptAssetPath))
    }

    private fun execute(program: String) : Message {
        val tokenize = Tokenize(program)
        val tokens: List<Token> = tokenize.allTokens()

        val parser = Parser(tokens)
        val statements = parser.parse()

        val fns = arrayOf<Callable>(
            object : Callable {
                var prev = System.currentTimeMillis()

                override fun name() = "clock"
                override fun arity() = 0
                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    val delta = System.currentTimeMillis() - prev
                    prev = System.currentTimeMillis()
                    return delta
                }
            },
            object : Callable {
                override fun name() = "print"
                override fun arity() = AnyArity
                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    println("${LogColors.CYAN}> ${LogColors.WHITE}${arguments.joinToString(separator = " ")}${LogColors.RESET}")
                    return None
                }
            },
            object : Callable {
                override fun name() = "effect"
                override fun arity() = 1
                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    val name = arguments[0] as String
                    when(name) {
                        "splash" -> {
                            val gameState = game.gameDrawFun().gameState
                            gameState.game.addEffect(
                                PopUpText(
                                    gameState.player.position.actualX(),
                                    gameState.player.position.actualY(), "Splash!!!")
                            )
                        }
                    }

                    return None
                }
            },
            object : Callable {
                override fun name(): String = "modifier"
                override fun arity(): Int = 3
                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                    val gameState = game.gameDrawFun().gameState
                    gameState.player.modifiers.addModifier(ModifierStat(
                        gameState.player.stats,
                        arguments[0] as String,
                        (arguments[1] as Double).toInt(),
                        (arguments[2] as Double).toInt()
                    ))

                    return None
                }
            }
        )

        val interpreter = Interpreter(fns)
        interpreter.interpret(statements)

        return SuccessMessage
    }
}

fun readFile(path: String) : String {
    val file = File(path)
    return String(file.readBytes())
}

fun main() {
    val basePath = "/mnt/sda1/Projects/LibGDX/PrettyCool/android/assets/scripts/"

    val scriptEngine = ScriptEngine(PrettyCoolGame())
    scriptEngine.executeFile("$basePath/v1/perks/gain.pcs")
}