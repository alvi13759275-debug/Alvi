package com.example

import com.example.compiler.binary.BinaryInspector
import com.example.compiler.interpreter.OmniInterpreter
import com.example.compiler.lexer.OmniLexer
import com.example.compiler.model.TokenType
import com.example.compiler.parser.OmniParser
import com.example.compiler.transpiler.OmniTranspiler
import com.example.compiler.transpiler.TranspileTarget
import com.example.compiler.vm.BytecodeCompiler
import com.example.compiler.vm.VirtualMachine
import org.junit.Assert.*
import org.junit.Test

class CompilerPipelineTest {

    @Test
    fun testPhase2Lexer() {
        val code = "let mut count: Int = 10; count |> double;"
        val lexer = OmniLexer(code)
        val tokens = lexer.tokenize()

        assertTrue(tokens.isNotEmpty())
        assertEquals(TokenType.LET, tokens[0].type)
        assertEquals(TokenType.MUT, tokens[1].type)
        assertEquals(TokenType.IDENTIFIER, tokens[2].type)
        assertEquals("count", tokens[2].lexeme)

        val pipeToken = tokens.find { it.type == TokenType.PIPE_FORWARD }
        assertNotNull(pipeToken)
        assertEquals("|>", pipeToken?.lexeme)
    }

    @Test
    fun testPhase3Parser() {
        val code = """
            fn addOne(x: Int): Int {
                return x + 1;
            }
            let res = addOne(5);
        """.trimIndent()

        val lexer = OmniLexer(code)
        val tokens = lexer.tokenize()
        val parser = OmniParser(tokens)
        val ast = parser.parse()

        assertEquals(2, ast.statements.size)
        assertTrue(parser.errors.isEmpty())
    }

    @Test
    fun testPhase4Interpreter() {
        val code = """
            fn square(n: Int): Int {
                return n * n;
            }
            let a = 4;
            let result = a |> square;
            print(result);
        """.trimIndent()

        val lexer = OmniLexer(code)
        val parser = OmniParser(lexer.tokenize())
        val ast = parser.parse()

        val interpreter = OmniInterpreter()
        val result = interpreter.execute(ast)

        assertTrue(result.isSuccess)
        assertEquals(1, result.output.size)
        assertEquals("16", result.output[0])
        assertEquals(16L, (result.variables["result"] as? Number)?.toLong())
    }

    @Test
    fun testPhase4BytecodeVM() {
        val code = """
            let a = 10;
            let b = 25;
            let c = a + b;
            print(c);
        """.trimIndent()

        val lexer = OmniLexer(code)
        val parser = OmniParser(lexer.tokenize())
        val ast = parser.parse()

        val compiler = BytecodeCompiler()
        val instructions = compiler.compile(ast)
        assertTrue(instructions.isNotEmpty())

        val vm = VirtualMachine(instructions)
        val output = vm.runAll()

        assertEquals(1, output.size)
        assertEquals("35", output[0])
    }

    @Test
    fun testPhase4Transpiler() {
        val code = "let count = 42; print(count);"
        val lexer = OmniLexer(code)
        val parser = OmniParser(lexer.tokenize())
        val ast = parser.parse()

        val transpiler = OmniTranspiler()
        val kotlinCode = transpiler.transpile(ast, TranspileTarget.KOTLIN)
        val pythonCode = transpiler.transpile(ast, TranspileTarget.PYTHON)

        assertTrue(kotlinCode.contains("val count = 42"))
        assertTrue(kotlinCode.contains("println(count)"))
        assertTrue(pythonCode.contains("count = 42"))
        assertTrue(pythonCode.contains("print(count)"))
    }

    @Test
    fun testBinaryInspector() {
        val code = "const v0, 42; const v1, 10; add-int v2, v0, v1; print v2;"
        val lexer = OmniLexer(code)
        val parser = OmniParser(lexer.tokenize())
        val ast = parser.parse()

        val compiler = BytecodeCompiler()
        val instructions = compiler.compile(ast)

        val report = BinaryInspector.analyzeDex(instructions)
        assertEquals("Android Dalvik Executable (DEX)", report.formatName)
        assertTrue(report.magicBytesHex.contains("64 65 78 0A"))
        assertTrue(report.totalBytes > 112)
        assertTrue(report.hexDump.isNotEmpty())
    }
}
