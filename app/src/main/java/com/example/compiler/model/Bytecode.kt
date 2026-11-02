package com.example.compiler.model

enum class Opcode(val mnemonic: String, val operandCount: Int, val description: String) {
    CONST("CONST", 1, "Push constant onto stack"),
    LOAD("LOAD", 1, "Load local variable onto stack"),
    STORE("STORE", 1, "Store top of stack into local variable"),
    ADD("ADD", 0, "Pop 2, push sum"),
    SUB("SUB", 0, "Pop 2, push difference"),
    MUL("MUL", 0, "Pop 2, push product"),
    DIV("DIV", 0, "Pop 2, push quotient"),
    MOD("MOD", 0, "Pop 2, push remainder"),
    EQ("CMP_EQ", 0, "Pop 2, push equality bool"),
    LT("CMP_LT", 0, "Pop 2, push less-than bool"),
    GT("CMP_GT", 0, "Pop 2, push greater-than bool"),
    JUMP("JUMP", 1, "Unconditional jump to address"),
    JUMP_IF_FALSE("JUMP_FALSE", 1, "Pop condition, jump if false"),
    CALL("CALL", 2, "Call function with arg count"),
    RET("RET", 0, "Return from current frame"),
    PRINT("PRINT", 0, "Pop and print top of stack"),
    REG_MOVE("REG_MOV", 2, "Move register value: dest, src"),
    REG_ADD("REG_ADD", 3, "dest = src1 + src2"),
    HALT("HALT", 0, "Halt execution")
}

data class Instruction(
    val address: Int,
    val opcode: Opcode,
    val operandA: Any? = null,
    val operandB: Any? = null,
    val operandC: Any? = null,
    val sourceLine: Int = 1
) {
    override fun toString(): String {
        val ops = listOfNotNull(operandA, operandB, operandC).joinToString(", ")
        val addrStr = address.toString().padStart(4, '0')
        return "$addrStr: ${opcode.mnemonic.padEnd(12)} $ops"
    }
}

data class VmFrame(
    val functionName: String,
    val returnAddress: Int,
    val locals: MutableMap<String, Any?> = mutableMapOf()
)

data class VmStepState(
    val programCounter: Int,
    val currentInstruction: Instruction?,
    val operandStack: List<Any?>,
    val registers: Map<String, Long>,
    val localVariables: Map<String, Any?>,
    val outputLog: List<String>,
    val isHalted: Boolean
)
