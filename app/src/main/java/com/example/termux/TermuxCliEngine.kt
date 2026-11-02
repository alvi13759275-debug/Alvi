package com.example.termux

import com.example.compiler.binary.BinaryInspector
import com.example.compiler.interpreter.OmniInterpreter
import com.example.compiler.lexer.OmniLexer
import com.example.compiler.model.LanguageProfile
import com.example.compiler.parser.OmniParser
import com.example.compiler.transpiler.OmniTranspiler
import com.example.compiler.transpiler.TranspileTarget
import com.example.compiler.vm.BytecodeCompiler
import com.example.compiler.vm.VirtualMachine
import com.example.knowledge.FormatCatalog

data class CommandOutput(
    val command: String,
    val text: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class TermuxCliEngine {
    private var currentSourceCode = LanguageProfile.OmniScript.sampleCode

    fun setSourceCode(code: String) {
        currentSourceCode = code
    }

    fun execute(input: String): CommandOutput {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return CommandOutput("", "")

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].lowercase()

        return when (cmd) {
            "help", "omnic", "omnic --help" -> CommandOutput(
                command = trimmed,
                text = """
============================================================
 OMNILANG CLI TOOL v2.4.0 (TERMUX EDITION)
 CREATOR / LEAD COMPILER ARCHITECT: ALVI
============================================================
Usage: omnic <command> [options]

Core Commands:
  omnic run                Execute current source code in AST Interpreter
  omnic build              Compile to Bytecode and execute in VM
  omnic lex                Tokenize source and print token stream table
  omnic ast                Parse tokens and output hierarchical AST
  omnic transpile --to=<t> Transpile code to: kt, ts, js, py, c, smali
  omnic hex                Inspect compiled Dalvik/DEX 16-byte hex dump
  omnic formats <query>    Query 700+ language & binary format database
  omnic termux-install     Generate Android Termux shell bootstrap script
  omnic snippet <name>     Load snippet (fib, fact, pipe, dex)

System Commands:
  whoami                   Print creator and architect profile
  uname -a                 Display virtual OS & runtime telemetry
  clear                    Clear terminal screen
  version                  Show OmniLang & Termux toolchain version
============================================================
"""
            )

            "whoami" -> CommandOutput(
                command = trimmed,
                text = """
CREATOR & ARCHITECT: ALVI
ROLE: Principal Compiler Architect, Language Designer & Binary Analyst
SYSTEM: OmniLang Compiler Labs
ORGANIZATION: High-Tech Mobile & Cloud Systems
STATUS: AUTHORIZED // LEVEL 5 COMPILER CORE ACCESS
"""
            )

            "uname", "uname -a" -> CommandOutput(
                command = trimmed,
                text = "Linux localhost 5.15.0-omnilang-termux aarch64 Android ART/DEX-039 ALVI-ENGINE"
            )

            "version", "omnic --version", "omnic -v" -> CommandOutput(
                command = trimmed,
                text = "OmniLang Studio Toolchain v2.4.0-termux (built by ALVI) for Android Dalvik & Multi-Target"
            )

            "omnic run", "run" -> {
                val lexer = OmniLexer(currentSourceCode)
                val parser = OmniParser(lexer.tokenize())
                val ast = parser.parse()
                if (parser.errors.isNotEmpty()) {
                    return CommandOutput(
                        command = trimmed,
                        text = "PARSE ERRORS:\n" + parser.errors.joinToString("\n"),
                        isError = true
                    )
                }
                val interpreter = OmniInterpreter()
                val res = interpreter.execute(ast)
                val sb = StringBuilder()
                sb.append("[OMNIC RUN] Execution finished in ${String.format("%.2f", res.executionTimeMs)} ms\n")
                sb.append("--- STDOUT ---\n")
                if (res.output.isEmpty()) {
                    sb.append("(No stdout produced)\n")
                } else {
                    res.output.forEach { sb.append("> $it\n") }
                }
                sb.append("--- VARIABLES IN SCOPE ---\n")
                res.variables.forEach { (k, v) -> sb.append("$k = $v\n") }
                CommandOutput(command = trimmed, text = sb.toString())
            }

            "omnic build", "build" -> {
                val lexer = OmniLexer(currentSourceCode)
                val parser = OmniParser(lexer.tokenize())
                val ast = parser.parse()
                val compiler = BytecodeCompiler()
                val instructions = compiler.compile(ast)
                val vm = VirtualMachine(instructions)
                val out = vm.runAll()
                val sb = StringBuilder()
                sb.append("[OMNIC BUILD] Compiled ${instructions.size} Bytecode Instructions\n")
                sb.append("--- VM EXECUTION OUTPUT ---\n")
                out.forEach { sb.append("> $it\n") }
                sb.append("--- REGISTERS ---\n")
                vm.registers.forEach { (r, v) -> sb.append("$r: $v  ") }
                sb.append("\nSTATUS: HALTED SUCCESSFULLY\n")
                CommandOutput(command = trimmed, text = sb.toString())
            }

            "omnic lex", "lex" -> {
                val lexer = OmniLexer(currentSourceCode)
                val tokens = lexer.tokenize()
                val sb = StringBuilder()
                sb.append("[OMNIC LEX] Tokenized ${tokens.size} tokens:\n")
                sb.append("LINE:COL | CATEGORY   | TYPE         | LEXEME\n")
                sb.append("---------+------------+--------------+--------------\n")
                tokens.take(20).forEach { t ->
                    val pos = "${t.line}:${t.column}".padEnd(8)
                    val cat = t.type.category.padEnd(10)
                    val typ = t.type.name.take(12).padEnd(12)
                    sb.append("$pos | $cat | $typ | ${t.lexeme}\n")
                }
                if (tokens.size > 20) {
                    sb.append("... (${tokens.size - 20} more tokens emitted)\n")
                }
                CommandOutput(command = trimmed, text = sb.toString())
            }

            "omnic ast", "ast" -> {
                val lexer = OmniLexer(currentSourceCode)
                val parser = OmniParser(lexer.tokenize())
                val ast = parser.parse()
                val pretty = ast.toPrettyString(0)
                CommandOutput(command = trimmed, text = "[OMNIC AST] Generated Abstract Syntax Tree:\n$pretty")
            }

            "omnic hex", "hex" -> {
                val lexer = OmniLexer(currentSourceCode)
                val parser = OmniParser(lexer.tokenize())
                val ast = parser.parse()
                val compiler = BytecodeCompiler()
                val instructions = compiler.compile(ast)
                val report = BinaryInspector.analyzeDex(instructions)
                val sb = StringBuilder()
                sb.append("[OMNIC HEX] Format: ${report.formatName}\n")
                sb.append("Magic Bytes: ${report.magicBytesHex}\n")
                sb.append("Total Size: ${report.totalBytes} bytes\n\n")
                report.hexDump.take(8).forEach { row ->
                    sb.append("${row.offsetHex}: ${row.hexBytes.joinToString(" ")} |${row.ascii}|\n")
                }
                CommandOutput(command = trimmed, text = sb.toString())
            }

            "omnic termux-install" -> CommandOutput(
                command = trimmed,
                text = """
# ========================================================
# ANDROID TERMUX BOOTSTRAP SCRIPT
# CREATED BY: ALVI (OMNILANG ARCHITECT)
# ========================================================
# Run this inside Android Termux:

pkg update -y && pkg upgrade -y
pkg install -y openjdk-17 kotlin nodejs python clang git curl

# Create omnic CLI wrapper:
cat << 'EOF' > ${'$'}PREFIX/bin/omnic
#!/data/data/com.termux/files/usr/bin/bash
echo "[OmniLang CLI by ALVI] Invoking compiler core..."
if [ "${'$'}1" == "help" ] || [ -z "${'$'}1" ]; then
    echo "OmniLang Termux Tool - Multi-Paradigm Compiler"
    echo "Usage: omnic [run|build|lex|ast|transpile|hex] <file>"
    exit 0
fi
echo "Processing target with OmniLang ART Engine..."
EOF

chmod +x ${'$'}PREFIX/bin/omnic
echo "OmniLang CLI successfully installed in Termux!"
"""
            )

            else -> {
                if (trimmed.startsWith("omnic transpile")) {
                    val target = when {
                        trimmed.contains("kt") -> TranspileTarget.KOTLIN
                        trimmed.contains("ts") -> TranspileTarget.TYPESCRIPT
                        trimmed.contains("js") -> TranspileTarget.JAVASCRIPT
                        trimmed.contains("py") -> TranspileTarget.PYTHON
                        trimmed.contains("c") -> TranspileTarget.ANSI_C
                        trimmed.contains("smali") -> TranspileTarget.SMALI
                        else -> TranspileTarget.KOTLIN
                    }
                    val lexer = OmniLexer(currentSourceCode)
                    val parser = OmniParser(lexer.tokenize())
                    val ast = parser.parse()
                    val transpiler = OmniTranspiler()
                    val code = transpiler.transpile(ast, target)
                    return CommandOutput(
                        command = trimmed,
                        text = "[OMNIC TRANSPILE -> ${target.displayName}]\n\n$code"
                    )
                }

                if (trimmed.startsWith("omnic formats")) {
                    val q = trimmed.removePrefix("omnic formats").trim()
                    val results = FormatCatalog.search(q)
                    val sb = StringBuilder()
                    sb.append("[OMNIC FORMATS] Found ${results.size} matching specifications:\n")
                    results.take(10).forEach { item ->
                        sb.append("• ${item.name} (${item.extensions.joinToString(", ")}) - ${item.executionModel}\n")
                    }
                    if (results.size > 10) sb.append("... and ${results.size - 10} more in database\n")
                    return CommandOutput(command = trimmed, text = sb.toString())
                }

                CommandOutput(
                    command = trimmed,
                    text = "omnic: command not found: '$trimmed'. Type 'help' or 'omnic' for available commands.",
                    isError = true
                )
            }
        }
    }
}
