package com.example.compiler.vm

import com.example.compiler.model.*

class BytecodeCompiler {
    private val instructions = mutableListOf<Instruction>()
    private var nextAddress = 0

    fun compile(program: ProgramNode): List<Instruction> {
        instructions.clear()
        nextAddress = 0

        for (stmt in program.statements) {
            compileStatement(stmt)
        }

        emit(Opcode.HALT)
        return instructions.toList()
    }

    private fun compileStatement(node: AstNode) {
        when (node) {
            is VariableDeclNode -> {
                compileExpression(node.initializer)
                emit(Opcode.STORE, node.name, sourceLine = node.line)
            }

            is AssignmentNode -> {
                compileExpression(node.value)
                emit(Opcode.STORE, node.target, sourceLine = node.line)
            }

            is PrintNode -> {
                compileExpression(node.expression)
                emit(Opcode.PRINT, sourceLine = node.line)
            }

            is ReturnNode -> {
                if (node.expression != null) {
                    compileExpression(node.expression)
                } else {
                    emit(Opcode.CONST, null, sourceLine = node.line)
                }
                emit(Opcode.RET, sourceLine = node.line)
            }

            is WhileNode -> {
                val loopStart = nextAddress
                compileExpression(node.condition)
                val jumpFalseIdx = emitPlaceholder(Opcode.JUMP_IF_FALSE, sourceLine = node.line)

                // Body
                for (s in node.body.statements) {
                    compileStatement(s)
                }
                emit(Opcode.JUMP, loopStart, sourceLine = node.line)

                // Patch jump false to target after loop
                patchJump(jumpFalseIdx, nextAddress)
            }

            is IfNode -> {
                compileExpression(node.condition)
                val jumpFalseIdx = emitPlaceholder(Opcode.JUMP_IF_FALSE, sourceLine = node.line)

                for (s in node.thenBranch.statements) {
                    compileStatement(s)
                }

                if (node.elseBranch != null) {
                    val jumpEndIdx = emitPlaceholder(Opcode.JUMP, sourceLine = node.line)
                    patchJump(jumpFalseIdx, nextAddress)
                    for (s in node.elseBranch.statements) {
                        compileStatement(s)
                    }
                    patchJump(jumpEndIdx, nextAddress)
                } else {
                    patchJump(jumpFalseIdx, nextAddress)
                }
            }

            is RegisterOpNode -> {
                when (node.opcode) {
                    "const" -> {
                        emit(Opcode.CONST, node.immediate ?: 0L, sourceLine = node.line)
                        emit(Opcode.STORE, node.destReg, sourceLine = node.line)
                    }
                    "move" -> {
                        emit(Opcode.LOAD, node.srcReg1 ?: "v0", sourceLine = node.line)
                        emit(Opcode.STORE, node.destReg, sourceLine = node.line)
                    }
                    "add-int" -> {
                        emit(Opcode.LOAD, node.srcReg1 ?: "v0", sourceLine = node.line)
                        emit(Opcode.LOAD, node.srcReg2 ?: "v1", sourceLine = node.line)
                        emit(Opcode.ADD, sourceLine = node.line)
                        emit(Opcode.STORE, node.destReg, sourceLine = node.line)
                    }
                    "mul-int" -> {
                        emit(Opcode.LOAD, node.srcReg1 ?: "v0", sourceLine = node.line)
                        emit(Opcode.LOAD, node.srcReg2 ?: "v1", sourceLine = node.line)
                        emit(Opcode.MUL, sourceLine = node.line)
                        emit(Opcode.STORE, node.destReg, sourceLine = node.line)
                    }
                    "print" -> {
                        emit(Opcode.LOAD, node.destReg, sourceLine = node.line)
                        emit(Opcode.PRINT, sourceLine = node.line)
                    }
                }
            }

            is BlockNode -> {
                for (s in node.statements) compileStatement(s)
            }

            else -> {
                // Expression statement
                compileExpression(node)
            }
        }
    }

    private fun compileExpression(node: AstNode) {
        when (node) {
            is LiteralNode -> {
                emit(Opcode.CONST, node.value, sourceLine = node.line)
            }

            is IdentifierNode -> {
                emit(Opcode.LOAD, node.name, sourceLine = node.line)
            }

            is BinaryOpNode -> {
                compileExpression(node.left)
                compileExpression(node.right)
                when (node.operator) {
                    "+" -> emit(Opcode.ADD, sourceLine = node.line)
                    "-" -> emit(Opcode.SUB, sourceLine = node.line)
                    "*" -> emit(Opcode.MUL, sourceLine = node.line)
                    "/" -> emit(Opcode.DIV, sourceLine = node.line)
                    "%" -> emit(Opcode.MOD, sourceLine = node.line)
                    "==" -> emit(Opcode.EQ, sourceLine = node.line)
                    "<" -> emit(Opcode.LT, sourceLine = node.line)
                    ">" -> emit(Opcode.GT, sourceLine = node.line)
                }
            }

            is PipeNode -> {
                // Pipe left |> right
                compileExpression(node.left)
                when (val right = node.right) {
                    is CallNode -> {
                        for (arg in right.arguments) {
                            compileExpression(arg)
                        }
                        emit(Opcode.CALL, right.callee, right.arguments.size + 1, sourceLine = node.line)
                    }
                    is IdentifierNode -> {
                        emit(Opcode.CALL, right.name, 1, sourceLine = node.line)
                    }
                    else -> {}
                }
            }

            is CallNode -> {
                for (arg in node.arguments) {
                    compileExpression(arg)
                }
                emit(Opcode.CALL, node.callee, node.arguments.size, sourceLine = node.line)
            }

            else -> {}
        }
    }

    private fun emit(
        opcode: Opcode,
        operandA: Any? = null,
        operandB: Any? = null,
        sourceLine: Int = 1
    ): Int {
        val addr = nextAddress++
        instructions.add(Instruction(addr, opcode, operandA, operandB, null, sourceLine))
        return addr
    }

    private fun emitPlaceholder(opcode: Opcode, sourceLine: Int): Int {
        val addr = nextAddress++
        instructions.add(Instruction(addr, opcode, -1, null, null, sourceLine))
        return instructions.size - 1
    }

    private fun patchJump(index: Int, targetAddress: Int) {
        val old = instructions[index]
        instructions[index] = old.copy(operandA = targetAddress)
    }
}
