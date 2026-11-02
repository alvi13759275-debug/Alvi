package com.example.compiler.model

sealed interface AstNode {
    val line: Int
    val nodeType: String

    fun toPrettyString(indent: Int = 0): String
    fun toJsonString(): String
}

data class ProgramNode(
    val statements: List<AstNode>,
    override val line: Int = 1,
    override val nodeType: String = "Program"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${pad}Program (${statements.size} stmts):\n")
        statements.forEach { sb.append(it.toPrettyString(indent + 1)).append("\n") }
        return sb.toString().trimEnd()
    }

    override fun toJsonString(): String {
        val children = statements.joinToString(",") { it.toJsonString() }
        return """{"type":"Program","line":$line,"body":[$children]}"""
    }
}

data class FunctionDefNode(
    val name: String,
    val parameters: List<ParameterNode>,
    val returnType: String = "Any",
    val body: BlockNode,
    override val line: Int = 1,
    override val nodeType: String = "FunctionDef"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val params = parameters.joinToString(", ") { "${it.name}: ${it.type}" }
        return "${pad}fn $name($params): $returnType\n${body.toPrettyString(indent + 1)}"
    }

    override fun toJsonString(): String {
        val paramsJson = parameters.joinToString(",") { """{"name":"${it.name}","type":"${it.type}"}""" }
        return """{"type":"FunctionDef","name":"$name","params":[$paramsJson],"returnType":"$returnType","body":${body.toJsonString()}}"""
    }
}

data class ParameterNode(
    val name: String,
    val type: String = "Any"
)

data class VariableDeclNode(
    val name: String,
    val type: String? = null,
    val isMutable: Boolean = false,
    val initializer: AstNode,
    override val line: Int = 1,
    override val nodeType: String = "VarDecl"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val kw = if (isMutable) "let mut" else "let"
        val typeStr = if (type != null) ": $type" else ""
        return "${pad}$kw $name$typeStr = ${initializer.toPrettyString(0).trim()}"
    }

    override fun toJsonString(): String {
        return """{"type":"VarDecl","name":"$name","mut":$isMutable,"init":${initializer.toJsonString()}}"""
    }
}

data class AssignmentNode(
    val target: String,
    val value: AstNode,
    override val line: Int = 1,
    override val nodeType: String = "Assignment"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        return "${pad}$target = ${value.toPrettyString(0).trim()}"
    }

    override fun toJsonString(): String {
        return """{"type":"Assignment","target":"$target","value":${value.toJsonString()}}"""
    }
}

data class BinaryOpNode(
    val left: AstNode,
    val operator: String,
    val right: AstNode,
    override val line: Int = 1,
    override val nodeType: String = "BinaryOp"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        return "${pad}(${left.toPrettyString(0).trim()} $operator ${right.toPrettyString(0).trim()})"
    }

    override fun toJsonString(): String {
        return """{"type":"BinaryOp","op":"$operator","left":${left.toJsonString()},"right":${right.toJsonString()}}"""
    }
}

data class UnaryOpNode(
    val operator: String,
    val operand: AstNode,
    override val line: Int = 1,
    override val nodeType: String = "UnaryOp"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        return "${pad}($operator${operand.toPrettyString(0).trim()})"
    }

    override fun toJsonString(): String {
        return """{"type":"UnaryOp","op":"$operator","operand":${operand.toJsonString()}}"""
    }
}

data class LiteralNode(
    val value: Any?,
    val literalType: String,
    override val line: Int = 1,
    override val nodeType: String = "Literal"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val formatted = if (value is String) "\"$value\"" else value.toString()
        return "${pad}$formatted"
    }

    override fun toJsonString(): String {
        val safeVal = if (value is String) "\"$value\"" else value.toString()
        return """{"type":"Literal","valType":"$literalType","value":$safeVal}"""
    }
}

data class IdentifierNode(
    val name: String,
    override val line: Int = 1,
    override val nodeType: String = "Identifier"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        return "  ".repeat(indent) + name
    }

    override fun toJsonString(): String {
        return """{"type":"Identifier","name":"$name"}"""
    }
}

data class CallNode(
    val callee: String,
    val arguments: List<AstNode>,
    override val line: Int = 1,
    override val nodeType: String = "Call"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val args = arguments.joinToString(", ") { it.toPrettyString(0).trim() }
        return "${pad}$callee($args)"
    }

    override fun toJsonString(): String {
        val argsJson = arguments.joinToString(",") { it.toJsonString() }
        return """{"type":"Call","callee":"$callee","args":[$argsJson]}"""
    }
}

data class BlockNode(
    val statements: List<AstNode>,
    override val line: Int = 1,
    override val nodeType: String = "Block"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${pad}{\n")
        statements.forEach { sb.append(it.toPrettyString(indent + 1)).append("\n") }
        sb.append("${pad}}")
        return sb.toString()
    }

    override fun toJsonString(): String {
        val stmts = statements.joinToString(",") { it.toJsonString() }
        return """{"type":"Block","stmts":[$stmts]}"""
    }
}

data class IfNode(
    val condition: AstNode,
    val thenBranch: BlockNode,
    val elseBranch: BlockNode? = null,
    override val line: Int = 1,
    override val nodeType: String = "If"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${pad}if (${condition.toPrettyString(0).trim()}) \n")
        sb.append(thenBranch.toPrettyString(indent))
        if (elseBranch != null) {
            sb.append("\n${pad}else \n")
            sb.append(elseBranch.toPrettyString(indent))
        }
        return sb.toString()
    }

    override fun toJsonString(): String {
        val elsePart = elseBranch?.toJsonString() ?: "null"
        return """{"type":"If","cond":${condition.toJsonString()},"then":${thenBranch.toJsonString()},"else":$elsePart}"""
    }
}

data class WhileNode(
    val condition: AstNode,
    val body: BlockNode,
    override val line: Int = 1,
    override val nodeType: String = "While"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        return "${pad}while (${condition.toPrettyString(0).trim()}) \n${body.toPrettyString(indent)}"
    }

    override fun toJsonString(): String {
        return """{"type":"While","cond":${condition.toJsonString()},"body":${body.toJsonString()}}"""
    }
}

data class ReturnNode(
    val expression: AstNode?,
    override val line: Int = 1,
    override val nodeType: String = "Return"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val expr = expression?.toPrettyString(0)?.trim() ?: ""
        return "${pad}return $expr"
    }

    override fun toJsonString(): String {
        val exprJson = expression?.toJsonString() ?: "null"
        return """{"type":"Return","value":$exprJson}"""
    }
}

data class PrintNode(
    val expression: AstNode,
    override val line: Int = 1,
    override val nodeType: String = "Print"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        return "${pad}print(${expression.toPrettyString(0).trim()})"
    }

    override fun toJsonString(): String {
        return """{"type":"Print","arg":${expression.toJsonString()}}"""
    }
}

// Functional Paradigm: Pipe operator (e.g. data |> transform)
data class PipeNode(
    val left: AstNode,
    val right: AstNode,
    override val line: Int = 1,
    override val nodeType: String = "Pipe"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        return "${pad}(${left.toPrettyString(0).trim()} |> ${right.toPrettyString(0).trim()})"
    }

    override fun toJsonString(): String {
        return """{"type":"Pipe","left":${left.toJsonString()},"right":${right.toJsonString()}}"""
    }
}

// Low-Level Dalvik/Register mode
data class RegisterOpNode(
    val opcode: String,
    val destReg: String,
    val srcReg1: String? = null,
    val srcReg2: String? = null,
    val immediate: Long? = null,
    override val line: Int = 1,
    override val nodeType: String = "RegisterOp"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val parts = mutableListOf(opcode, destReg)
        srcReg1?.let { parts.add(it) }
        srcReg2?.let { parts.add(it) }
        immediate?.let { parts.add("#0x${it.toString(16)}") }
        return "$pad${parts.joinToString(" ")}"
    }

    override fun toJsonString(): String {
        return """{"type":"RegisterOp","opcode":"$opcode","dest":"$destReg","src1":"$srcReg1","src2":"$srcReg2","imm":$immediate}"""
    }
}

// Object-Oriented / Declarative struct
data class StructDefNode(
    val name: String,
    val fields: List<Pair<String, String>>,
    override val line: Int = 1,
    override val nodeType: String = "StructDef"
) : AstNode {
    override fun toPrettyString(indent: Int): String {
        val pad = "  ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${pad}struct $name {\n")
        fields.forEach { (fieldName, fieldType) ->
            sb.append("$pad  $fieldName: $fieldType\n")
        }
        sb.append("$pad}")
        return sb.toString()
    }

    override fun toJsonString(): String {
        val fJson = fields.joinToString(",") { """{"name":"${it.first}","type":"${it.second}"}""" }
        return """{"type":"StructDef","name":"$name","fields":[$fJson]}"""
    }
}
