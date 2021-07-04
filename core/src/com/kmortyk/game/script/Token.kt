package com.kmortyk.game.script

enum class TokenType {
    // chars
    LEFT_PAREN, RIGHT_PAREN, LEFT_SQ_PAREN, RIGHT_SQ_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SLASH, STAR,

    // logical expressions
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // literals.
    IDENTIFIER, STRING, NUMBER,

    // keywords.
    IF, ELSE, VAR, FOR, IN,
    AND, OR, TRUE, FALSE,
    FUN, RETURN,
    CLASS, NONE, SUPER, THIS,

    // End of file
    EOF
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val line: Int) {

    override fun toString(): String =
        "$type['$lexeme': $literal]"
}