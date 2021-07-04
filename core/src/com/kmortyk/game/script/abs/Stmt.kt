package com.kmortyk.game.script.abs

import com.kmortyk.game.script.Token

abstract class Stmt {
    interface Visitor<R> {
        fun visitBlockStmt(stmt: Block): R
        fun visitClassStmt(stmt: Class): R
        fun visitFunctionStmt(stmt: Function): R
        fun visitReturnStmt(stmt: Return): R

        fun visitIfStmt (stmt: If ): R
        fun visitVarStmt(stmt: Var): R

        fun visitForStmt(stmt: For): R
        fun visitForRangeStmt(stmt: ForRange): R

        fun visitExpressionStmt(stmt: ExprStmt): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    class Block(val statements: List<Stmt?>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }

        override fun toString() = statements.toString()
    }

    class Class(val name: Token, val superclass: Expr.Variable, val methods: List<Function>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitClassStmt(this)
        }
    }

    class ExprStmt(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExpressionStmt(this)
        }
    }

    class Function(val name: Token, val params: List<Token>, val body: List<Stmt?>) :
        Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFunctionStmt(this)
        }
    }

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }

        override fun toString() = "IF $condition THEN $thenBranch ELSE $elseBranch"
    }

    class Return(val keyword: Token, val value: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitReturnStmt(this)
        }
    }

    class Var(val name: Token, val initializer: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }

        override fun toString() = "VAR ${name.lexeme} = $initializer"
    }

    class For(val condition: Expr?, val body: Stmt) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitForStmt(this)
        }
    }

    class ForRange(val variableName: Token,
                   val from: Expr, val includeFrom: Boolean,
                   val to: Expr, val includeTo: Boolean,
                   val body: Block) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitForRangeStmt(this)
        }
    }
}