package com.kmortyk.game.script.error

data class Message(val error: Boolean, val line: Int, val message: String) {
    fun print() {
        System.err.println("[line $line] : $message")
    }
}

const val AnyLine = 0

val SuccessMessage = Message(false, AnyLine, "success")