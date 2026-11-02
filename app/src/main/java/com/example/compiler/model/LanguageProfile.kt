package com.example.compiler.model

enum class LanguageParadigm(val displayName: String, val description: String) {
    MULTI_PARADIGM("Multi-Paradigm", "Blend of Imperative, Functional, and Structured"),
    IMPERATIVE("Imperative", "Sequential statements, mutable state, explicit loops"),
    FUNCTIONAL("Functional", "Expressions, immutability, first-class functions, pipe operators"),
    OBJECT_ORIENTED("Object-Oriented", "Structs, methods, encapsulation, interfaces"),
    DECLARATIVE("Declarative", "Schemas, specifications, configuration logic"),
    LOW_LEVEL_ASM("Low-Level Assembly", "Register banks, bytecode opcodes, immediate values"),
    META_LANGUAGE("Meta-Language", "Code generation targets (Transpiles to Kotlin, JS, TS, C)")
}

enum class TargetExecution(val displayName: String, val badge: String) {
    DIRECT_AST_INTERPRETER("Direct AST Interpreter", "AST Eval"),
    TRANSPILER("Multi-Target Transpiler", "Transpile"),
    BYTECODE_VM("Bytecode Stack/Register VM", "Bytecode VM"),
    NATIVE_BINARY("Binary Inspector & Disasm", "Binary Hex")
}

data class GrammarRule(
    val nonTerminal: String,
    val production: String,
    val explanation: String
)

data class LanguageProfile(
    val id: String,
    val name: String,
    val fileExtension: String,
    val version: String,
    val paradigm: LanguageParadigm,
    val defaultExecution: TargetExecution,
    val keywords: List<String>,
    val operators: List<Pair<String, Int>>, // Operator, Precedence level
    val typeNames: List<String>,
    val grammarSpecs: List<GrammarRule>,
    val sampleCode: String
) {
    companion object {
        val OmniScript = LanguageProfile(
            id = "omniscript",
            name = "OmniScript",
            fileExtension = ".omni",
            version = "1.0-alpha",
            paradigm = LanguageParadigm.MULTI_PARADIGM,
            defaultExecution = TargetExecution.DIRECT_AST_INTERPRETER,
            keywords = listOf("fn", "let", "mut", "if", "else", "while", "return", "print", "match", "struct", "pipe"),
            operators = listOf(
                "|>" to 1,
                "=" to 2,
                "||" to 3,
                "&&" to 4,
                "==" to 5, "!=" to 5,
                "<" to 6, "<=" to 6, ">" to 6, ">=" to 6,
                "+" to 7, "-" to 7,
                "*" to 8, "/" to 8, "%" to 8
            ),
            typeNames = listOf("Int", "Float", "String", "Bool", "List", "Unit"),
            grammarSpecs = listOf(
                GrammarRule("Program", "Statement* EOF", "A sequence of top-level declarations and expressions"),
                GrammarRule("FunctionDef", "'fn' IDENT '(' ParamList? ')' (':' Type)? Block", "Function declaration with typed parameters"),
                GrammarRule("VarDecl", "'let' 'mut'? IDENT (':' Type)? '=' Expression ';'", "Variable binding with optional mutability"),
                GrammarRule("IfStmt", "'if' '(' Expression ')' Block ('else' Block)?", "Conditional branching statement"),
                GrammarRule("WhileStmt", "'while' '(' Expression ')' Block", "Standard pre-condition loop"),
                GrammarRule("PipeExpr", "Expression '|>' FunctionCall", "Left-to-right functional pipeline evaluation"),
                GrammarRule("BinaryExpr", "Expression ('+' | '-' | '*' | '/' | '==' | '<') Expression", "Infix binary operations with precedence climbing")
            ),
            sampleCode = """// OmniScript Multi-Paradigm Demo
fn double(x: Int): Int {
  return x * 2;
}

fn addTen(y: Int): Int {
  return y + 10;
}

let mut count = 0;
while (count < 3) {
  let result = count |> double;
  print(result);
  count = count + 1;
}

let total = 25 |> double |> addTen;
print(total);
"""
        )

        val DexAsm = LanguageProfile(
            id = "dexasm",
            name = "DexAsm",
            fileExtension = ".dexasm",
            version = "Dalvik-ART-039",
            paradigm = LanguageParadigm.LOW_LEVEL_ASM,
            defaultExecution = TargetExecution.BYTECODE_VM,
            keywords = listOf("method", "end", "registers", "const", "move", "add", "sub", "mul", "return", "print"),
            operators = listOf(
                "=" to 1,
                "+" to 2,
                "-" to 2
            ),
            typeNames = listOf("v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "Reg32", "Int"),
            grammarSpecs = listOf(
                GrammarRule("Method", "'.method' IDENT '(' Registers ')' Instruction* '.end'", "Dalvik register method declaration"),
                GrammarRule("ConstOp", "'const' Register ',' Immediate", "Loads 32-bit immediate constant into register"),
                GrammarRule("MoveOp", "'move' DestReg ',' SrcReg", "Copies value from source register to destination"),
                GrammarRule("AddOp", "'add-int' DestReg ',' Src1 ',' Src2", "Binary 32-bit integer addition into register"),
                GrammarRule("ReturnOp", "'return' Register", "Returns value stored in designated register")
            ),
            sampleCode = """.method main(registers 4)
  const v0, 42
  const v1, 18
  add-int v2, v0, v1
  print v2
  const v3, 10
  mul-int v2, v2, v3
  print v2
  return v2
.end
"""
        )

        val FuncCore = LanguageProfile(
            id = "funccore",
            name = "FuncCore",
            fileExtension = ".fcore",
            version = "0.9",
            paradigm = LanguageParadigm.FUNCTIONAL,
            defaultExecution = TargetExecution.TRANSPILER,
            keywords = listOf("fn", "let", "match", "return", "print"),
            operators = listOf(
                "|>" to 1,
                "->" to 2,
                "+" to 3, "*" to 4
            ),
            typeNames = listOf("Int", "String", "Bool"),
            grammarSpecs = listOf(
                GrammarRule("Lambda", "'(' ParamList? ')' '->' Expression", "Anonymous lambda expression"),
                GrammarRule("Pipeline", "Expr ('|>' Expr)+", "Function composition pipeline")
            ),
            sampleCode = """fn square(n: Int): Int {
  return n * n;
}

fn increment(n: Int): Int {
  return n + 1;
}

let input = 7;
let calc = input |> square |> increment;
print(calc);
"""
        )

        val MetaLanguage = LanguageProfile(
            id = "metalang",
            name = "MetaCompiler",
            fileExtension = ".meta",
            version = "2.0",
            paradigm = LanguageParadigm.META_LANGUAGE,
            defaultExecution = TargetExecution.TRANSPILER,
            keywords = listOf("fn", "let", "mut", "if", "else", "while", "return", "print", "struct"),
            operators = listOf("=" to 1, "+" to 2, "-" to 2, "*" to 3, "/" to 3),
            typeNames = listOf("Int", "String", "Float", "Bool"),
            grammarSpecs = listOf(
                GrammarRule("TargetDef", "'emit' TargetLang Block", "Generates native target output code"),
                GrammarRule("AstMeta", "Quote(AstNode)", "Quoted AST expression for metaprogramming")
            ),
            sampleCode = """struct Vector2 {
  x: Int
  y: Int
}

fn computeDistance(v1: Vector2, v2: Vector2): Int {
  let dx = v1.x - v2.x;
  let dy = v1.y - v2.y;
  return (dx * dx) + (dy * dy);
}

print(100);
"""
        )

        val Presets = listOf(OmniScript, DexAsm, FuncCore, MetaLanguage)
    }
}
