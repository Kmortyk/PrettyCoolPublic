package com.kmortyk.game.script

import com.kmortyk.game.LogColors
import com.kmortyk.game.script.abs.Expr
import com.kmortyk.game.script.error.RuntimeError
import com.kmortyk.game.script.abs.Stmt
import com.kmortyk.game.script.error.AnyLine
import com.kmortyk.game.script.error.Message
import com.kmortyk.game.script.error.ReturnException

interface Callable {
    fun name(): String
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) : Callable {
    override fun name() = declaration.name.lexeme

    override fun arity() = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for (i in declaration.params.indices) {
            environment.define(
                declaration.params[i].lexeme,
                arguments[i]
            )
        }

        // TODO change error to simple return call
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: ReturnException) {
            return returnValue.value
        }

        return null
    }

    override fun toString() = "<fn ${name()}>"
}

class Interpreter(extensionFuns: Array<Callable>) : Expr.Visitor<Any?>, Stmt.Visitor<Any?> {
    companion object {
        val AnyArity = Integer.MAX_VALUE
    }


    val globals = Environment()
    private var environment = globals

    init {
        for(fn in extensionFuns) {
            globals.define(fn.name(), fn)
        }
    }

    fun interpret(statements: List<Stmt?>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Message(true, AnyLine, error.message!!).print()
        }
    }

    private fun execute(stmt: Stmt?) {
        if(stmt != null) {
            stmt.accept(this)
        } else { // TODO delete null check
            Message(true, AnyLine, "statement is null").print()
        }
    }

    private fun stringify(`object`: Any?): String {
        if (`object` == null) return "nil"
        if (`object` is Double) {
            var text = `object`.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return `object`.toString()
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }
    private fun isTrue(`object`: Any?): Boolean {
        if (`object` == null) return false
        return if (`object` is Boolean) `object` else true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        return if (a == null) false else a == b
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }
            TokenType.BANG -> {
                checkNumberOperand(expr.operator, right)
                return !isTrue(right)
            }
            else -> {  }
        }
        return null
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return left as Double - right as Double
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                return left as Double / right as Double
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return left as Double * right as Double
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double)
                    return left + right
                if (left is String && right is String)
                    return left + right
            }
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return left as Double > right as Double
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return left as Double >= right as Double
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (left as Double) < right as Double
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return left as Double <= right as Double
            }
            TokenType.BANG_EQUAL -> {
                return !isEqual(left, right)
            }
            TokenType.EQUAL_EQUAL -> {
                return isEqual(left, right)
            }
            else -> {  }
        }

        // Unreachable.
        return null
    }

    // runtime checks
    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name.lexeme, value)
        return value
    }

    fun executeBlock(statements: List<Stmt?>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments: MutableList<Any?> = ArrayList()
        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is Callable) {
            throw RuntimeError(
                expr.paren,
                "can't call $callee"
            )
        }

        val function: Callable = callee

        if (function.arity() != AnyArity && arguments.size != function.arity()) {
            throw RuntimeError(
                expr.paren, "Expected " +
                        function.arity().toString() + " arguments but got " +
                        arguments.size.toString() + "."
            )
        }

        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? { TODO("Not yet implemented") }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTrue(left)) return left
        } else {
            if (!isTrue(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): Any? { TODO("Not yet implemented") }

    override fun visitSuperExpr(expr: Expr.Super): Any? { TODO("Not yet implemented") }

    override fun visitThisExpr(expr: Expr.This): Any? { TODO("Not yet implemented") }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment[expr.name]
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Any? {
        executeBlock(stmt.statements, Environment(environment))
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Class): Any? {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStmt(stmt: Stmt.ExprStmt): Any? {
        evaluate(stmt.expression)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Any? {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Any? {
        if (isTrue(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if(stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Any {
        val value: Any? = evaluate(stmt.value)

        throw ReturnException(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var): Any? {
        val value: Any? = evaluate(stmt.initializer)

        environment.define(stmt.name.lexeme, value!!)
        return null
    }

    override fun visitForStmt(stmt: Stmt.For): Any? {
        val maxIterations = 10_000
        var curIteration = 0

        if(stmt.condition != null) {
            while (isTrue(evaluate(stmt.condition)) && curIteration < maxIterations) {
                execute(stmt.body)
                curIteration++
            }
        } else { // TODO run in parallel thread
            while (curIteration < maxIterations) {
                execute(stmt.body)
                curIteration++
            }
        }

        if(curIteration >= maxIterations) {
            println("${LogColors.RED}exceed max iterations limit '$maxIterations'; exit from 'for' loop${LogColors.RESET}")
        }

        return null
    }

    override fun visitForRangeStmt(stmt: Stmt.ForRange): Any? {
        val fromEval = evaluate(stmt.from) as Double
        val toEval = evaluate(stmt.to) as Double

        val from = if(stmt.includeFrom) fromEval else fromEval + 1
        val to = if(stmt.includeTo) toEval else toEval - 1

        val loopEnv = Environment(environment)
        loopEnv.defineAssign(stmt.variableName.lexeme, from)

        var idx = from
        while(idx <= to) {
            loopEnv.assign(stmt.variableName.lexeme, idx)
            executeBlock(stmt.body.statements, loopEnv)
            idx++
        }

        return null
    }
}