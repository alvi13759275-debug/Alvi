package com.example.compiler.vm

import com.example.compiler.model.*

class VirtualMachine(private val instructions: List<Instruction>) {
    var pc: Int = 0
        private set

    val stack = mutableListOf<Any?>()
    val registers = mutableMapOf<String, Long>()
    val locals = mutableMapOf<String, Any?>()
    val output = mutableListOf<String>()
    var isHalted: Boolean = false
        private set

    init {
        // Initialize registers v0..v7 with 0
        for (i in 0..7) {
            registers["v$i"] = 0L
        }
    }

    fun reset() {
        pc = 0
        stack.clear()
        locals.clear()
        output.clear()
        isHalted = false
        for (i in 0..7) {
            registers["v$i"] = 0L
        }
    }

    fun step(): VmStepState {
        if (isHalted || pc >= instructions.size) {
            isHalted = true
            return getCurrentState()
        }

        val instr = instructions[pc]
        pc++

        when (instr.opcode) {
            Opcode.CONST -> {
                stack.add(instr.operandA)
            }

            Opcode.LOAD -> {
                val name = instr.operandA.toString()
                if (registers.containsKey(name)) {
                    stack.add(registers[name])
                } else {
                    val value = locals[name] ?: 0L
                    stack.add(value)
                }
            }

            Opcode.STORE -> {
                val name = instr.operandA.toString()
                val value = if (stack.isNotEmpty()) stack.removeAt(stack.size - 1) else 0L
                if (registers.containsKey(name)) {
                    registers[name] = (value as? Number)?.toLong() ?: 0L
                } else {
                    locals[name] = value
                }
            }

            Opcode.ADD -> {
                val b = popNumber()
                val a = popNumber()
                stack.add(a + b)
            }

            Opcode.SUB -> {
                val b = popNumber()
                val a = popNumber()
                stack.add(a - b)
            }

            Opcode.MUL -> {
                val b = popNumber()
                val a = popNumber()
                stack.add(a * b)
            }

            Opcode.DIV -> {
                val b = popNumber()
                val a = popNumber()
                stack.add(if (b == 0L) 0L else a / b)
            }

            Opcode.MOD -> {
                val b = popNumber()
                val a = popNumber()
                stack.add(if (b == 0L) 0L else a % b)
            }

            Opcode.EQ -> {
                val b = if (stack.isNotEmpty()) stack.removeAt(stack.size - 1) else null
                val a = if (stack.isNotEmpty()) stack.removeAt(stack.size - 1) else null
                stack.add(a == b)
            }

            Opcode.LT -> {
                val b = popNumber()
                val a = popNumber()
                stack.add(a < b)
            }

            Opcode.GT -> {
                val b = popNumber()
                val a = popNumber()
                stack.add(a > b)
            }

            Opcode.JUMP -> {
                val target = (instr.operandA as? Number)?.toInt() ?: pc
                pc = target
            }

            Opcode.JUMP_IF_FALSE -> {
                val cond = if (stack.isNotEmpty()) stack.removeAt(stack.size - 1) else false
                val isFalse = when (cond) {
                    is Boolean -> !cond
                    is Number -> cond.toLong() == 0L
                    null -> true
                    else -> false
                }
                if (isFalse) {
                    val target = (instr.operandA as? Number)?.toInt() ?: pc
                    pc = target
                }
            }

            Opcode.PRINT -> {
                val value = if (stack.isNotEmpty()) stack.removeAt(stack.size - 1) else null
                output.add(value?.toString() ?: "null")
            }

            Opcode.RET -> {
                // In simple script mode, top of stack is return value
            }

            Opcode.REG_MOVE -> {
                val dest = instr.operandA.toString()
                val src = instr.operandB.toString()
                registers[dest] = registers[src] ?: 0L
            }

            Opcode.REG_ADD -> {
                val dest = instr.operandA.toString()
                val s1 = instr.operandB.toString()
                val s2 = instr.operandC.toString()
                registers[dest] = (registers[s1] ?: 0L) + (registers[s2] ?: 0L)
            }

            Opcode.CALL -> {
                // Handled as builtin or simulated call
            }

            Opcode.HALT -> {
                isHalted = true
            }
        }

        if (pc >= instructions.size) {
            isHalted = true
        }

        return getCurrentState()
    }

    fun runAll(maxSteps: Int = 100_000): List<String> {
        var steps = 0
        while (!isHalted && steps < maxSteps) {
            step()
            steps++
        }
        return output
    }

    fun getCurrentState(): VmStepState {
        val currentInstr = if (pc in instructions.indices) instructions[pc] else null
        return VmStepState(
            programCounter = pc,
            currentInstruction = currentInstr,
            operandStack = stack.toList(),
            registers = registers.toMap(),
            localVariables = locals.toMap(),
            outputLog = output.toList(),
            isHalted = isHalted
        )
    }

    private fun popNumber(): Long {
        if (stack.isEmpty()) return 0L
        val top = stack.removeAt(stack.size - 1)
        return (top as? Number)?.toLong() ?: 0L
    }
}
