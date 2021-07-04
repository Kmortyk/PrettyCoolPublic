package com.kmortyk.game.script.utils

import com.kmortyk.game.script.abs.Expr
import com.kmortyk.game.script.abs.Stmt
import java.lang.StringBuilder


internal class AstPrinter : Expr.Visitor<String>,
                            Stmt.Visitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(
            expr.operator.lexeme,
            expr.left, expr.right
        )
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    override fun visitAssignExpr(expr: Expr.Assign): String { TODO("Not yet implemented") }

    override fun visitCallExpr(expr: Expr.Call): String { TODO("Not yet implemented") }

    override fun visitGetExpr(expr: Expr.Get): String { TODO("Not yet implemented") }

    override fun visitLogicalExpr(expr: Expr.Logical): String { TODO("Not yet implemented") }

    override fun visitSetExpr(expr: Expr.Set): String { TODO("Not yet implemented") }

    override fun visitSuperExpr(expr: Expr.Super): String { TODO("Not yet implemented") }

    override fun visitThisExpr(expr: Expr.This): String { TODO("Not yet implemented") }

    override fun visitVariableExpr(expr: Expr.Variable): String { TODO("Not yet implemented") }

    override fun visitBlockStmt(stmt: Stmt.Block): String {
        TODO("Not yet implemented")
    }

    override fun visitClassStmt(stmt: Stmt.Class): String {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStmt(stmt: Stmt.ExprStmt): String {
        TODO("Not yet implemented")
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): String {
        TODO("Not yet implemented")
    }

    override fun visitIfStmt(stmt: Stmt.If): String {
        TODO("Not yet implemented")
    }

    override fun visitReturnStmt(stmt: Stmt.Return): String {
        TODO("Not yet implemented")
    }

    override fun visitVarStmt(stmt: Stmt.Var): String {
        TODO("Not yet implemented")
    }

    override fun visitForStmt(stmt: Stmt.For): String {
        TODO("Not yet implemented")
    }

    override fun visitForRangeStmt(stmt: Stmt.ForRange): String {
        TODO("Not yet implemented")
    }
}