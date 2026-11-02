package com.example.compiler.interpreter

import com.example.compiler.model.*

class InterpreterEnvironment(private val parent: InterpreterEnvironment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: String, value: Any?): Boolean {
        if (values.containsKey(name)) {
            values[name] = value
            return true
        }
        if (parent != null) {
            return parent.assign(name, value)
        }
        return false
    }

    fun get(name: String): Any? {
        if (values.containsKey(name)) return values[name]
        if (parent != null) return parent.get(name)
        throw RuntimeException("Undefined variable '$name'")
    }

    fun getAllVariables(): Map<String, Any?> {
        val all = mutableMapOf<String, Any?>()
        parent?.getAllVariables()?.let { all.putAll(it) }
        all.putAll(values)
        return all
    }
}

class ReturnException(val value: Any?) : RuntimeException()

class OmniInterpreter {
    val stdout = mutableListOf<String>()
    val executionLog = mutableListOf<String>()
    private val functions = mutableMapOf<String, FunctionDefNode>()

    fun execute(program: ProgramNode): InterpreterResult {
        stdout.clear()
        executionLog.clear()
        functions.clear()
        val globalEnv = InterpreterEnvironment()

        val startTime = System.nanoTime()
        try {
            // First pass: register functions
            for (stmt in program.statements) {
                if (stmt is FunctionDefNode) {
                    functions[stmt.name] = stmt
                }
            }

            // Second pass: execute top-level statements
            for (stmt in program.statements) {
                if (stmt !is FunctionDefNode) {
                    executeStatement(stmt, globalEnv)
                }
            }

            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
            return InterpreterResult(
                isSuccess = true,
                output = stdout,
                variables = globalEnv.getAllVariables(),
                executionTimeMs = elapsedMs
            )
        } catch (e: Exception) {
            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
            return InterpreterResult(
                isSuccess = false,
                output = stdout,
                variables = globalEnv.getAllVariables(),
                errorMessage = e.message ?: "Execution error",
                executionTimeMs = elapsedMs
            )
        }
    }

    private fun executeStatement(node: AstNode, env: InterpreterEnvironment) {
        when (node) {
            is VariableDeclNode -> {
                val initVal = evaluate(node.initializer, env)
                env.define(node.name, initVal)
            }

            is AssignmentNode -> {
                val newVal = evaluate(node.value, env)
                if (!env.assign(node.target, newVal)) {
                    env.define(node.target, newVal)
                }
            }

            is PrintNode -> {
                val res = evaluate(node.expression, env)
                val outStr = res?.toString() ?: "null"
                stdout.add(outStr)
            }

            is IfNode -> {
                val cond = evaluate(node.condition, env)
                if (isTruthy(cond)) {
                    executeBlock(node.thenBranch, InterpreterEnvironment(env))
                } else if (node.elseBranch != null) {
                    executeBlock(node.elseBranch, InterpreterEnvironment(env))
                }
            }

            is WhileNode -> {
                var iterations = 0
                while (isTruthy(evaluate(node.condition, env))) {
                    executeBlock(node.body, InterpreterEnvironment(env))
                    iterations++
                    if (iterations > 100_000) {
                        throw RuntimeException("Infinite loop detected (> 100k iterations)")
                    }
                }
            }

            is BlockNode -> {
                executeBlock(node, InterpreterEnvironment(env))
            }

            is ReturnNode -> {
                val value = node.expression?.let { evaluate(it, env) }
                throw ReturnException(value)
            }

            is RegisterOpNode -> {
                executeRegisterOp(node, env)
            }

            else -> {
                // Expression statement
                evaluate(node, env)
            }
        }
    }

    private fun executeBlock(block: BlockNode, env: InterpreterEnvironment) {
        for (stmt in block.statements) {
            executeStatement(stmt, env)
        }
    }

    private fun executeRegisterOp(node: RegisterOpNode, env: InterpreterEnvironment) {
        when (node.opcode) {
            "const" -> {
                env.define(node.destReg, node.immediate ?: 0L)
            }
            "move" -> {
                val src = node.srcReg1?.let { env.get(it) } ?: 0L
                env.define(node.destReg, src)
            }
            "add-int" -> {
                val v1 = (node.srcReg1?.let { env.get(it) } as? Number)?.toLong() ?: 0L
                val v2 = (node.srcReg2?.let { env.get(it) } as? Number)?.toLong() ?: 0L
                env.define(node.destReg, v1 + v2)
            }
            "mul-int" -> {
                val v1 = (node.srcReg1?.let { env.get(it) } as? Number)?.toLong() ?: 0L
                val v2 = (node.srcReg2?.let { env.get(it) } as? Number)?.toLong() ?: 0L
                env.define(node.destReg, v1 * v2)
            }
            "print" -> {
                val valToPrint = env.get(node.destReg)
                stdout.add(valToPrint?.toString() ?: "0")
            }
        }
    }

    private fun evaluate(node: AstNode, env: InterpreterEnvironment): Any? {
        return when (node) {
            is LiteralNode -> node.value

            is IdentifierNode -> env.get(node.name)

            is UnaryOpNode -> {
                val operand = evaluate(node.operand, env)
                when (node.operator) {
                    "-" -> when (operand) {
                        is Long -> -operand
                        is Int -> -operand
                        is Double -> -operand
                        else -> 0L
                    }
                    "!" -> !isTruthy(operand)
                    else -> operand
                }
            }

            is BinaryOpNode -> {
                val left = evaluate(node.left, env)
                val right = evaluate(node.right, env)
                applyBinaryOp(left, node.operator, right)
            }

            is PipeNode -> {
                // Functional pipeline: left |> right
                // Evaluate left, then call right passing left as the first argument
                val leftVal = evaluate(node.left, env)
                when (val rightNode = node.right) {
                    is CallNode -> {
                        val newArgs = mutableListOf<AstNode>(LiteralNode(leftVal, "Any"))
                        newArgs.addAll(rightNode.arguments)
                        callFunction(rightNode.callee, newArgs.map { evaluate(it, env) }, env)
                    }
                    is IdentifierNode -> {
                        callFunction(rightNode.name, listOf(leftVal), env)
                    }
                    else -> throw RuntimeException("Right side of pipe operator must be a function call or identifier")
                }
            }

            is CallNode -> {
                val args = node.arguments.map { evaluate(it, env) }
                callFunction(node.callee, args, env)
            }

            else -> null
        }
    }

    private fun callFunction(callee: String, args: List<Any?>, env: InterpreterEnvironment): Any? {
        if (callee == "print") {
            val text = args.joinToString(" ") { it?.toString() ?: "null" }
            stdout.add(text)
            return null
        }

        val fnDef = functions[callee] ?: throw RuntimeException("Undefined function '$callee'")
        val fnEnv = InterpreterEnvironment(null)

        fnDef.parameters.forEachIndexed { index, param ->
            val argVal = if (index < args.size) args[index] else null
            fnEnv.define(param.name, argVal)
        }

        return try {
            executeBlock(fnDef.body, fnEnv)
            null
        } catch (ret: ReturnException) {
            ret.value
        }
    }

    private fun applyBinaryOp(left: Any?, op: String, right: Any?): Any? {
        // String concatenation
        if (op == "+" && (left is String || right is String)) {
            return "${left ?: ""}${right ?: ""}"
        }

        // Numeric operations
        if (left is Number && right is Number) {
            val l = left.toDouble()
            val r = right.toDouble()
            val isInt = (left is Long || left is Int) && (right is Long || right is Int)

            return when (op) {
                "+" -> if (isInt) left.toLong() + right.toLong() else l + r
                "-" -> if (isInt) left.toLong() - right.toLong() else l - r
                "*" -> if (isInt) left.toLong() * right.toLong() else l * r
                "/" -> if (r == 0.0) throw RuntimeException("Division by zero") else if (isInt) left.toLong() / right.toLong() else l / r
                "%" -> if (r == 0.0) throw RuntimeException("Modulo by zero") else left.toLong() % right.toLong()
                "==" -> left == right
                "!=" -> left != right
                "<" -> l < r
                "<=" -> l <= r
                ">" -> l > r
                ">=" -> l >= r
                else -> null
            }
        }

        return when (op) {
            "==" -> left == right
            "!=" -> left != right
            "&&" -> isTruthy(left) && isTruthy(right)
            "||" -> isTruthy(left) || isTruthy(right)
            else -> null
        }
    }

    private fun isTruthy(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is Number -> value.toDouble() != 0.0
            is String -> value.isNotEmpty()
            else -> true
        }
    }
}

data class InterpreterResult(
    val isSuccess: Boolean,
    val output: List<String>,
    val variables: Map<String, Any?>,
    val errorMessage: String? = null,
    val executionTimeMs: Double = 0.0
)
