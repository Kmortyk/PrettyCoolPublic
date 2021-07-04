package com.kmortyk.game.script

import com.kmortyk.game.script.abs.Expr
import com.kmortyk.game.script.abs.Stmt
import com.kmortyk.game.script.error.Message
import java.text.ParseException
import java.lang.Exception

val None = Expr.Literal(null)

class Parser(private val tokens: List<Token>) {
    private var current = 0

    // -- Parser -------------------------------------------------------------------------------------------------------

    fun parse(): List<Stmt?> {
        val statements = ArrayList<Stmt?>()

        while(!finished()) {
            statements.add(declaration())
        }

        return statements
    }

    // -- Statements ---------------------------------------------------------------------------------------------------

    private fun declaration(): Stmt? {
        return try {
            if (consumeIf(TokenType.FUN)) return function("function");
            if (consumeIf(TokenType.VAR))
                varDeclaration()
            else
                statement()

        } catch (error: Exception) {
            error.printStackTrace()
            recover()
            null
        }
    }

    private fun function(kind: String): Stmt.Function {
        val name = consumeAssert(TokenType.IDENTIFIER, "Expect $kind name.")
        consumeAssert(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters: MutableList<Token> = ArrayList()
        if (!currentEqualsTo(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peekToken(), "Can't have more than 255 parameters.")
                }
                parameters.add(
                    consumeAssert(TokenType.IDENTIFIER, "Expect parameter name.")
                )
            } while (currentEqualsTo(TokenType.COMMA))
        }
        consumeAssert(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consumeAssert(TokenType.LEFT_BRACE, "Expect '{' before $kind body.")
        val body: List<Stmt?> = blockStatement().statements
        return Stmt.Function(name, parameters, body)
    }

    private fun varDeclaration(): Stmt {
        val name = consumeAssert(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (consumeIf(TokenType.EQUAL)) {
            initializer = expression()
        }
        return Stmt.Var(name, initializer!!)
    }

    private fun statement(): Stmt {
        if(consumeIf(TokenType.LEFT_BRACE)) return blockStatement()
        if(consumeIf(TokenType.IF)) return ifStatement()
        if(consumeIf(TokenType.RETURN)) return returnStatement();
        if(consumeIf(TokenType.FOR)) return forStatement()

        return expressionStatement()
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value: Expr = expression()

        return Stmt.Return(keyword, value)
    }

    private fun forStatement(): Stmt {
        // max possible iterations
        return if(consumeIf(TokenType.LEFT_BRACE)) { // if we see block statement - no condition at all
            val body = statement()

            Stmt.For(null, body)
        } else if(currentEqualsTo(TokenType.IDENTIFIER) && nextIs(TokenType.IN)) { // if we see identifier and after that - range construct
            val variable = consume()                                               // get the for-range
            consumeAssert(TokenType.IN, "expected IN after IDENTIFIER")

            var includeLeft = false
            if(consumeIf(TokenType.LEFT_SQ_PAREN)) {
                includeLeft = true
            } else {
                consumeAssert(TokenType.LEFT_PAREN, "expected '(' or '[' after IN statement")
            }

            val from = expression()

            consumeAssert(TokenType.COMMA, "expected COMMA as range delimiter")

            val to = expression()

            var includeRight = false
            if(consumeIf(TokenType.RIGHT_SQ_PAREN)) {
                includeRight = true
            } else {
                consumeAssert(TokenType.RIGHT_PAREN, "expected ')' or ']' in range loop")
            }

            val body = statement() as Stmt.Block

            Stmt.ForRange(variable, from, includeLeft, to, includeRight, body)
        } else { // else get range with condition
            val condition = expression()
            val body = statement()

            Stmt.For(condition, body)
        }
    }

    private fun ifStatement(): Stmt {
        val condition = expression()
        val thenBranch = statement()

        var elseBranch: Stmt? = null
        if (consumeIf(TokenType.ELSE)) {
            elseBranch = statement()
        }

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun blockStatement(): Stmt.Block {
        val statements: MutableList<Stmt?> = ArrayList()
        while (!currentEqualsTo(TokenType.RIGHT_BRACE) && !finished()) {
            statements.add(declaration())
        }

        consumeAssert(TokenType.RIGHT_BRACE, "Expect '}' after block.")

        return Stmt.Block(statements)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        return Stmt.ExprStmt(expr)
    }

    // -- Expressions --------------------------------------------------------------------------------------------------

    // expression
    private fun expression(): Expr {
        return assignment()
    }

    // assignment -> IDENTIFIER "=" assignment
    private fun assignment(): Expr {
        val expr = or()

        if (consumeIf(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun or(): Expr {
        var expr: Expr = and()

        while (currentEqualsTo(TokenType.OR)) {
            val operator = previous()
            val right = and()

            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (currentEqualsTo(TokenType.AND)) {
            val operator = previous()
            val right: Expr = equality()

            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expr {
        var expr: Expr = comparison()
        while (consumeIf(
                TokenType.BANG_EQUAL,
                TokenType.EQUAL_EQUAL
            )) {
            val operator: Token = previous()
            val right: Expr = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private fun comparison(): Expr {
        var expr: Expr = term()
        while (consumeIf(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL
            )) {
            val operator = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr: Expr = factor()
        while (consumeIf(
                TokenType.MINUS,
                TokenType.PLUS
            )) {
            val operator = previous()
            val right: Expr = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr: Expr = unary()
        while (consumeIf(
                TokenType.SLASH,
                TokenType.STAR
            )) {
            val operator = previous()
            val right: Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // unary -> ( "!" | "-" ) unary | primary ;
    private fun unary(): Expr {
        if (consumeIf(
                TokenType.BANG,
                TokenType.MINUS
            )) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return call()
    }

    private fun call(): Expr {
        var expr: Expr = primary()

        while (true) {
            if (consumeIf(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }

        return expr
    }

    private fun primary(): Expr {
        if (consumeIf(TokenType.FALSE)) return Expr.Literal(false)
        if (consumeIf(TokenType.TRUE)) return Expr.Literal(true)
        if (consumeIf(TokenType.NONE)) return None
        if (consumeIf(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous().literal!!)
        if (consumeIf(TokenType.IDENTIFIER)) return Expr.Variable(previous())
        if (consumeIf(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consumeAssert(TokenType.RIGHT_PAREN, "Expect ')' after expression.")

            return Expr.Grouping(expr)
        }

        throw error(peekToken(), "expect expression");
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments: MutableList<Expr> = ArrayList()
        if (!currentEqualsTo(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peekToken(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression())
            } while (consumeIf(TokenType.COMMA))
        }
        val paren = consumeAssert(
            TokenType.RIGHT_PAREN,
            "Expect ')' after arguments."
        )
        return Expr.Call(callee, paren, arguments)
    }

    // -- utils --------------------------------------------------------------------------------------------------------
    private fun recover() {
        consume()

        while (!finished()) {
            when(peekToken().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR,
                TokenType.FOR, TokenType.IF,
                TokenType.RETURN -> return

                else -> {}
            }
            consume()
        }
    }

    private fun consumeAssert(type: TokenType, messageIfError: String) : Token {
        if (currentEqualsTo(type))
            return consume()

        throw error(peekToken(), messageIfError);
    }

    private fun error(token: Token, message: String): ParseException {
        if (token.type == TokenType.EOF) {
            Message(true, token.line, "at end $message").print()
        } else {
            Message(true, token.line, "at '${token.lexeme}' $message").print()
        }
        return ParseException(message, token.line)
    }

    private fun consumeIf(vararg types: TokenType): Boolean {
        for (type in types) {
            if (currentEqualsTo(type)) {
                consume()
                return true
            }
        }
        return false
    }

    private fun consume(): Token {
        if (!finished()) current++
        return previous()
    }

    private fun currentEqualsTo(type: TokenType) = if (finished()) false else peekToken().type === type

    private fun nextIs(type: TokenType) = if(finished() || current + 1 >= tokens.size) false else tokens[current + 1].type === type

    private fun finished() = peekToken().type == TokenType.EOF

    private fun peekToken() = tokens[current]

    private fun previous() = tokens[current - 1]
}