package com.kmortyk.game.script

import com.kmortyk.game.script.error.Message
import com.kmortyk.game.script.error.SuccessMessage

// Many thanks to http://www.craftinginterpreters.com/scanning.html

class Tokenize (private val source: String) {
    private var keywords: Map<String, TokenType> = mapOf(
        "fun" to TokenType.FUN,
        "return" to TokenType.RETURN,
        //"super" to TokenType.SUPER,
        //"this" to TokenType.THIS,
        //"class" to TokenType.CLASS,

        "var" to TokenType.VAR,
        "none" to TokenType.NONE,

        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "and" to TokenType.AND,
        "or" to TokenType.OR,
        "false" to TokenType.FALSE,
        "true" to TokenType.TRUE,

        "for" to TokenType.FOR,
        "in" to TokenType.IN
    )

    private val tokens: ArrayList<Token> = ArrayList()

    // indexes
    private var start = 0
    private var current = 0
    private var line = 1

    fun allTokens() : List<Token> {
        while (!finished()) {
            // we are at the beginning of the next lexeme
            start = current

            val mes = nextToken()
            if(mes != SuccessMessage) {
                mes.print()
            }
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun finished() = current >= source.length

    // -- read token ---------------------------------------------------------------------------------------------------
    private fun nextToken() : Message {
        val c = consumeChar()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '[' -> addToken(TokenType.LEFT_SQ_PAREN)
            ']' -> addToken(TokenType.RIGHT_SQ_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            //';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            // logical expressions
            '!' -> addToken(if(consumeCharIf('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if(consumeCharIf('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if(consumeCharIf('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if(consumeCharIf('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            // comment
            '/' -> {
                if (consumeCharIf('/')) {
                    // a comment goes until the end of the line
                    while (peekChar() != '\n' && !finished()) consumeChar()
                    // ... ignore comment token
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            // special characters
            ' ', '\r', '\t' -> { /* ignore whitespace */ }
            '\n' -> { ++line }
            // string
            '"' -> return string()
            // number
            in '0'..'9' -> number()
            // identifier
            in 'a'..'z', in 'A'..'Z', '_' -> identifier()
            // unknown character
            else -> {
                return Message(true, line, "unexpected character '$c'")
            }
        }
        return SuccessMessage
    }

    private fun consumeChar() = source[++current - 1]

    private fun consumeCharIf(expected: Char): Boolean {
        if (finished()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peekChar(): Char {
        return if (finished()) '\u0000' else source[current]
    }

    private fun peekNextChar(): Char {
        return if (current + 1 >= source.length) '\u0000' else source[current + 1]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text: String = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    // literals
    private fun string() : Message {
        while (peekChar() != '"' && !finished()) {
            if (peekChar() == '\n') line++
            consumeChar()
        }

        if (finished()) {
            return Message(true, line, "unterminated string")
        }

        // the closing "
        consumeChar()

        // trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)

        return SuccessMessage
    }

    private fun isDigit(c: Char) = c in '0'..'9'

    private fun number() {
        while (isDigit(peekChar())) consumeChar()

        // Look for a fractional part.
        if (peekChar() == '.' && isDigit(peekNextChar())) {
            // consume the "."
            consumeChar()
            while (isDigit(peekChar())) consumeChar()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    // identifiers
    private fun identifier() {
        while (isAlphaNumeric(peekChar())) consumeChar()
        val text = source.substring(start, current)

        var type = keywords[text]
        if (type == null)
            type = TokenType.IDENTIFIER

        addToken(type)
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }
}