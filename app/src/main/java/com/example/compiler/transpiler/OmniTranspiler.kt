package com.example.compiler.transpiler

import com.example.compiler.model.*

enum class TranspileTarget(val id: String, val displayName: String, val extension: String) {
    KOTLIN("kt", "Kotlin", ".kt"),
    TYPESCRIPT("ts", "TypeScript", ".ts"),
    JAVASCRIPT("js", "JavaScript", ".js"),
    PYTHON("py", "Python 3", ".py"),
    ANSI_C("c", "ANSI C", ".c"),
    SMALI("smali", "Dalvik Smali", ".smali")
}

class OmniTranspiler {

    fun transpile(program: ProgramNode, target: TranspileTarget): String {
        return when (target) {
            TranspileTarget.KOTLIN -> transpileToKotlin(program)
            TranspileTarget.TYPESCRIPT -> transpileToTypeScript(program)
            TranspileTarget.JAVASCRIPT -> transpileToJavaScript(program)
            TranspileTarget.PYTHON -> transpileToPython(program)
            TranspileTarget.ANSI_C -> transpileToC(program)
            TranspileTarget.SMALI -> transpileToSmali(program)
        }
    }

    private fun transpileToKotlin(program: ProgramNode): String {
        val sb = StringBuilder()
        sb.append("// Transpiled from OmniLang to Kotlin\n")
        sb.append("package generated\n\n")

        val functions = mutableListOf<FunctionDefNode>()
        val topLevel = mutableListOf<AstNode>()

        for (stmt in program.statements) {
            if (stmt is FunctionDefNode) functions.add(stmt)
            else topLevel.add(stmt)
        }

        // Functions
        for (fn in functions) {
            val params = fn.parameters.joinToString(", ") { "${it.name}: ${mapType(it.type, "kt")}" }
            val retType = mapType(fn.returnType, "kt")
            sb.append("fun ${fn.name}($params): $retType {\n")
            for (s in fn.body.statements) {
                sb.append("    ${transpileNode(s, "kt")}\n")
            }
            sb.append("}\n\n")
        }

        // Main entry
        sb.append("fun main() {\n")
        for (stmt in topLevel) {
            sb.append("    ${transpileNode(stmt, "kt")}\n")
        }
        sb.append("}\n")
        return sb.toString()
    }

    private fun transpileToTypeScript(program: ProgramNode): String {
        val sb = StringBuilder()
        sb.append("// Transpiled from OmniLang to TypeScript\n\n")
        for (stmt in program.statements) {
            if (stmt is FunctionDefNode) {
                val params = stmt.parameters.joinToString(", ") { "${it.name}: ${mapType(it.type, "ts")}" }
                val retType = mapType(stmt.returnType, "ts")
                sb.append("function ${stmt.name}($params): $retType {\n")
                for (s in stmt.body.statements) {
                    sb.append("  ${transpileNode(s, "ts")}\n")
                }
                sb.append("}\n\n")
            } else {
                sb.append("${transpileNode(stmt, "ts")}\n")
            }
        }
        return sb.toString()
    }

    private fun transpileToJavaScript(program: ProgramNode): String {
        val sb = StringBuilder()
        sb.append("// Transpiled from OmniLang to JavaScript (ES6+)\n\n")
        for (stmt in program.statements) {
            if (stmt is FunctionDefNode) {
                val params = stmt.parameters.joinToString(", ") { it.name }
                sb.append("function ${stmt.name}($params) {\n")
                for (s in stmt.body.statements) {
                    sb.append("  ${transpileNode(s, "js")}\n")
                }
                sb.append("}\n\n")
            } else {
                sb.append("${transpileNode(stmt, "js")}\n")
            }
        }
        return sb.toString()
    }

    private fun transpileToPython(program: ProgramNode): String {
        val sb = StringBuilder()
        sb.append("# Transpiled from OmniLang to Python 3\n\n")
        for (stmt in program.statements) {
            if (stmt is FunctionDefNode) {
                val params = stmt.parameters.joinToString(", ") { "${it.name}: ${mapType(it.type, "py")}" }
                sb.append("def ${stmt.name}($params) -> ${mapType(stmt.returnType, "py")}:\n")
                if (stmt.body.statements.isEmpty()) {
                    sb.append("    pass\n")
                } else {
                    for (s in stmt.body.statements) {
                        sb.append("    ${transpileNode(s, "py")}\n")
                    }
                }
                sb.append("\n")
            } else {
                sb.append("${transpileNode(stmt, "py")}\n")
            }
        }
        return sb.toString()
    }

    private fun transpileToC(program: ProgramNode): String {
        val sb = StringBuilder()
        sb.append("// Transpiled from OmniLang to ANSI C99\n")
        sb.append("#include <stdio.h>\n#include <stdbool.h>\n\n")

        val functions = mutableListOf<FunctionDefNode>()
        val topLevel = mutableListOf<AstNode>()

        for (stmt in program.statements) {
            if (stmt is FunctionDefNode) functions.add(stmt)
            else topLevel.add(stmt)
        }

        // Functions
        for (fn in functions) {
            val params = if (fn.parameters.isEmpty()) "void" else fn.parameters.joinToString(", ") { "${mapType(it.type, "c")} ${it.name}" }
            val retType = mapType(fn.returnType, "c")
            sb.append("$retType ${fn.name}($params) {\n")
            for (s in fn.body.statements) {
                sb.append("    ${transpileNode(s, "c")};\n")
            }
            sb.append("}\n\n")
        }

        sb.append("int main(int argc, char** argv) {\n")
        for (stmt in topLevel) {
            sb.append("    ${transpileNode(stmt, "c")};\n")
        }
        sb.append("    return 0;\n")
        sb.append("}\n")
        return sb.toString()
    }

    private fun transpileToSmali(program: ProgramNode): String {
        val sb = StringBuilder()
        sb.append("# Android Dalvik/Smali Assembly\n")
        sb.append(".class public Lcom/example/generated/OmniProgram;\n")
        sb.append(".super Ljava/lang/Object;\n\n")

        sb.append(".method public static main([Ljava/lang/String;)V\n")
        sb.append("    .registers 8\n\n")

        var regIndex = 0
        for (stmt in program.statements) {
            when (stmt) {
                is VariableDeclNode -> {
                    val r = "v${regIndex % 8}"
                    regIndex++
                    val initStr = transpileNode(stmt.initializer, "smali")
                    sb.append("    # ${stmt.name} = $initStr\n")
                    sb.append("    const/4 $r, 0x1\n")
                }
                is PrintNode -> {
                    sb.append("    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;\n")
                    sb.append("    const-string v1, \"[OmniLang Execution]\"\n")
                    sb.append("    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V\n")
                }
                is RegisterOpNode -> {
                    when (stmt.opcode) {
                        "const" -> sb.append("    const ${stmt.destReg}, 0x${(stmt.immediate ?: 0L).toString(16)}\n")
                        "move" -> sb.append("    move ${stmt.destReg}, ${stmt.srcReg1}\n")
                        "add-int" -> sb.append("    add-int ${stmt.destReg}, ${stmt.srcReg1}, ${stmt.srcReg2}\n")
                        "mul-int" -> sb.append("    mul-int ${stmt.destReg}, ${stmt.srcReg1}, ${stmt.srcReg2}\n")
                        "print" -> sb.append("    # print register ${stmt.destReg}\n")
                    }
                }
                else -> {
                    sb.append("    # ${stmt.nodeType}\n")
                }
            }
        }

        sb.append("    return-void\n")
        sb.append(".end method\n")
        return sb.toString()
    }

    private fun transpileNode(node: AstNode, lang: String): String {
        return when (node) {
            is VariableDeclNode -> {
                val init = transpileNode(node.initializer, lang)
                when (lang) {
                    "kt" -> "${if (node.isMutable) "var" else "val"} ${node.name} = $init"
                    "ts" -> "${if (node.isMutable) "let" else "const"} ${node.name} = $init;"
                    "js" -> "${if (node.isMutable) "let" else "const"} ${node.name} = $init;"
                    "py" -> "${node.name} = $init"
                    "c" -> "long ${node.name} = $init"
                    else -> "${node.name} = $init"
                }
            }

            is AssignmentNode -> {
                val v = transpileNode(node.value, lang)
                when (lang) {
                    "py" -> "${node.target} = $v"
                    else -> "${node.target} = $v;"
                }
            }

            is PrintNode -> {
                val arg = transpileNode(node.expression, lang)
                when (lang) {
                    "kt" -> "println($arg)"
                    "ts", "js" -> "console.log($arg);"
                    "py" -> "print($arg)"
                    "c" -> "printf(\"%ld\\n\", (long)($arg))"
                    else -> "print($arg)"
                }
            }

            is ReturnNode -> {
                val expr = node.expression?.let { transpileNode(it, lang) } ?: ""
                when (lang) {
                    "py" -> "return $expr"
                    else -> "return $expr;"
                }
            }

            is IfNode -> {
                val cond = transpileNode(node.condition, lang)
                val sb = StringBuilder()
                when (lang) {
                    "py" -> {
                        sb.append("if $cond:\n")
                        for (s in node.thenBranch.statements) sb.append("    ${transpileNode(s, lang)}\n")
                        if (node.elseBranch != null) {
                            sb.append("else:\n")
                            for (s in node.elseBranch.statements) sb.append("    ${transpileNode(s, lang)}\n")
                        }
                    }
                    else -> {
                        sb.append("if ($cond) {\n")
                        for (s in node.thenBranch.statements) sb.append("    ${transpileNode(s, lang)}\n")
                        sb.append("}")
                        if (node.elseBranch != null) {
                            sb.append(" else {\n")
                            for (s in node.elseBranch.statements) sb.append("    ${transpileNode(s, lang)}\n")
                            sb.append("}")
                        }
                    }
                }
                sb.toString()
            }

            is WhileNode -> {
                val cond = transpileNode(node.condition, lang)
                val sb = StringBuilder()
                when (lang) {
                    "py" -> {
                        sb.append("while $cond:\n")
                        for (s in node.body.statements) sb.append("    ${transpileNode(s, lang)}\n")
                    }
                    else -> {
                        sb.append("while ($cond) {\n")
                        for (s in node.body.statements) sb.append("    ${transpileNode(s, lang)}\n")
                        sb.append("}")
                    }
                }
                sb.toString()
            }

            is BinaryOpNode -> {
                val l = transpileNode(node.left, lang)
                val r = transpileNode(node.right, lang)
                "($l ${node.operator} $r)"
            }

            is PipeNode -> {
                // In Kotlin/JS/Python, translate pipe to standard nested call
                val left = transpileNode(node.left, lang)
                when (val right = node.right) {
                    is CallNode -> {
                        val args = mutableListOf(left)
                        args.addAll(right.arguments.map { transpileNode(it, lang) })
                        "${right.callee}(${args.joinToString(", ")})"
                    }
                    is IdentifierNode -> "${right.name}($left)"
                    else -> "/* pipe */"
                }
            }

            is CallNode -> {
                val args = node.arguments.joinToString(", ") { transpileNode(it, lang) }
                "${node.callee}($args)"
            }

            is LiteralNode -> {
                if (node.value is String) "\"${node.value}\"" else node.value.toString()
            }

            is IdentifierNode -> node.name

            is BlockNode -> {
                node.statements.joinToString("\n") { transpileNode(it, lang) }
            }

            is RegisterOpNode -> {
                "${node.opcode} ${node.destReg}"
            }

            else -> ""
        }
    }

    private fun mapType(type: String, lang: String): String {
        return when (lang) {
            "kt" -> when (type) {
                "Int" -> "Int"
                "Float" -> "Double"
                "String" -> "String"
                "Bool" -> "Boolean"
                else -> "Any"
            }
            "ts" -> when (type) {
                "Int", "Float" -> "number"
                "String" -> "string"
                "Bool" -> "boolean"
                else -> "any"
            }
            "py" -> when (type) {
                "Int" -> "int"
                "Float" -> "float"
                "String" -> "str"
                "Bool" -> "bool"
                else -> "Any"
            }
            "c" -> when (type) {
                "Int" -> "long"
                "Float" -> "double"
                "String" -> "const char*"
                "Bool" -> "bool"
                else -> "void*"
            }
            else -> type
        }
    }
}
