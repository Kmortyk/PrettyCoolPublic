package com.kmortyk.game.script

import com.kmortyk.game.script.error.AnyLine
import com.kmortyk.game.script.error.RuntimeError

class Environment {
    private val values: MutableMap<String, Any?> = HashMap()
    private val enclosing: Environment?

    constructor() {
        enclosing = null
    }

    constructor(env: Environment) {
        enclosing = env
    }

    fun assign(name: String, value: Any?) {
        if (values.containsKey(name)) {
            values[name] = value!!
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return
        }

        throw RuntimeError(Token(TokenType.VAR, name, name, AnyLine), "Undefined variable '$name'.")
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun defineAssign(name: String, value: Any?) {
        if(has(name)) {
            assign(name, value)
        } else {
            values[name] = value
        }
    }

    fun has(name: String) : Boolean {
        return if(values.containsKey(name)) true
        else enclosing?.has(name) ?: false
    }

    operator fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if (enclosing != null) return enclosing[name]

        throw RuntimeError(name, "Undefined variable '" + name.lexeme + "'.")
    }
}