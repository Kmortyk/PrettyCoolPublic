package com.kmortyk.game.script.utils

import com.kmortyk.game.script.error.AnyLine
import com.kmortyk.game.script.error.Message
import com.kmortyk.game.script.error.SuccessMessage

enum class VarType {
    Int,
    Float,
    Boolean,
    String
}

data class Var(val name: String, val type: VarType, var value: Any)

class VarsState {

    val state: MutableMap<String, Var> = mutableMapOf()

    fun setVar(name: String, type: VarType, value: Any) {
        state[name] = Var(name, type, value)
    }

    fun saveState() {
        // TODO
    }

    fun loadState() {
        // TODO
    }

    fun serializeState() : String {
        return "" // TODO
    }

    fun deserializeState(str: String) {
        // TODO
    }

    // -- Values -------------------------------------------------------------------------------------------------------
    fun setValue(name: String, value: String) : Message {
        // typeCheck(name, value)
        if(name in state) {
            state[name]!!.value = value
        } else {
            return Message(true, AnyLine,"not found $name")
        }
        return SuccessMessage
    }

    fun <T> getValue(name: String, default: T) : Pair<T, Message> {
        if(name in state) {
            val v = state[name]!!.value
            return try {
                val i = v as T
                Pair(i, SuccessMessage)
            } catch (e: java.lang.NumberFormatException) {
                Pair(default, Message(true, AnyLine,"parse error"))
            }
        }
        return Pair(default, Message(true, AnyLine,"not found $name"))
    }

    fun <T> hasValue(name: String, value: T) : Boolean {
        if(name !in state)
            return false

        val v = state[name]!!.value

        return v == value
    }
}