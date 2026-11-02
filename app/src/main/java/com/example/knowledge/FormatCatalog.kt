package com.example.knowledge

import com.example.compiler.model.LanguageParadigm
import com.example.compiler.model.TargetExecution

enum class FormatCategory(val displayName: String, val countLabel: String, val icon: String) {
    WEB_MARKUP("Web, Markup & Templates", "50+ formats", "globe"),
    SCRIPTING_TYPED("Scripting, Typed & Web-Scale", "100+ languages", "code"),
    SYSTEMS_BYTECODE("Systems, Assembly & Binaries", "100+ formats", "cpu"),
    MOBILE_GAMES("Mobile, OS & Game Engines", "100+ formats", "gamepad"),
    DATA_SERIALIZATION("Database, Query & Serialization", "100+ formats", "database"),
    SHELL_ADMIN("Shell, CLI & Admin Scripts", "80+ tools", "terminal"),
    HARDWARE_SHADERS("Hardware, GPU & Embedded", "70+ formats", "chip"),
    ESOTERIC_MATH("Esoteric & Domain-Specific", "100+ formats", "flask")
}

data class FormatItem(
    val name: String,
    val extensions: List<String>,
    val category: FormatCategory,
    val paradigm: LanguageParadigm,
    val executionModel: String,
    val magicSignature: String? = null,
    val syntaxSignature: String,
    val sampleSnippet: String,
    val description: String
)

object FormatCatalog {
    val items: List<FormatItem> = listOf(
        // Category 1: WEB, MARKUP & TEMPLATES
        FormatItem(
            name = "HTML5 / XHTML",
            extensions = listOf(".html", ".htm", ".xhtml"),
            category = FormatCategory.WEB_MARKUP,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Browser DOM Parser / WebKit / Blink",
            syntaxSignature = "<!DOCTYPE html><html>",
            sampleSnippet = "<!DOCTYPE html>\n<html>\n  <body>\n    <h1>OmniLang Web</h1>\n  </body>\n</html>",
            description = "Standard markup language for documents designed to be displayed in a web browser."
        ),
        FormatItem(
            name = "XML / SVG",
            extensions = listOf(".xml", ".svg", ".xaml"),
            category = FormatCategory.WEB_MARKUP,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "SAX / DOM / XPath Parser",
            magicSignature = "3C 3F 78 6D 6C ('<?xml')",
            syntaxSignature = "<?xml version=\"1.0\"?>",
            sampleSnippet = "<svg viewBox=\"0 0 100 100\">\n  <circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"#38BDF8\"/>\n</svg>",
            description = "Extensible Markup Language & Scalable Vector Graphics for hierarchical vector schemas."
        ),
        FormatItem(
            name = "MDX / Markdown",
            extensions = listOf(".mdx", ".md", ".markdown"),
            category = FormatCategory.WEB_MARKUP,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Remark / Unified / Babel JSX Transpiler",
            syntaxSignature = "# Heading, [link](url), <Component />",
            sampleSnippet = "# Compiler Specs\n\nWelcome to <CustomBadge text=\"OmniLang\" />.",
            description = "Markdown for the component era, embedding interactive JSX components inside markdown."
        ),
        FormatItem(
            name = "Svelte",
            extensions = listOf(".svelte"),
            category = FormatCategory.WEB_MARKUP,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Svelte AOT Compiler to Vanilla JS DOM Ops",
            syntaxSignature = "<script> let count = 0; </script>",
            sampleSnippet = "<script>\n  let count = " + '$' + "state(0);\n</script>\n<button onclick={() => count++}>{count}</button>",
            description = "Cybernetically enhanced web apps compiled directly into imperatively updating DOM nodes."
        ),
        FormatItem(
            name = "Vue Single File Component",
            extensions = listOf(".vue"),
            category = FormatCategory.WEB_MARKUP,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Vue Template Compiler / Virtual DOM",
            syntaxSignature = "<template>, <script setup>, <style scoped>",
            sampleSnippet = "<script setup>\nconst msg = ref('Hello Vue')\n</script>\n<template><h1>{{ msg }}</h1></template>",
            description = "Component-based architecture bundling reactive logic, templates, and scoped styles."
        ),
        FormatItem(
            name = "JSX / TSX",
            extensions = listOf(".jsx", ".tsx"),
            category = FormatCategory.WEB_MARKUP,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "React Virtual DOM / Transpiled to React.createElement",
            syntaxSignature = "const element = <div className=\"app\">{val}</div>",
            sampleSnippet = "export function Button({ label }: { label: string }) {\n  return <button>{label}</button>;\n}",
            description = "XML-like syntax extension for ECMAScript combining declarative view trees with full JS/TS power."
        ),

        // Category 2: SCRIPTING, TYPED & WEB-SCALE
        FormatItem(
            name = "TypeScript",
            extensions = listOf(".ts", ".mts", ".cts"),
            category = FormatCategory.SCRIPTING_TYPED,
            paradigm = LanguageParadigm.MULTI_PARADIGM,
            executionModel = "Type Stripped -> Node.js / V8 / Deno / Bun",
            syntaxSignature = "interface Token<T> { type: T; value: string; }",
            sampleSnippet = "interface ASTNode {\n  type: string;\n  evaluate(): number;\n}\nconst node: ASTNode = { type: 'Lit', evaluate: () => 42 };",
            description = "Typed superset of JavaScript that compiles to plain JavaScript with rich structural typing."
        ),
        FormatItem(
            name = "Kotlin Multiplatform (KMP)",
            extensions = listOf(".kt", ".kts"),
            category = FormatCategory.SCRIPTING_TYPED,
            paradigm = LanguageParadigm.MULTI_PARADIGM,
            executionModel = "Kotlin JVM / Native LLVM / Kotlin/Wasm / JS",
            syntaxSignature = "fun <T> List<T>.customFilter(p: (T) -> Boolean): List<T>",
            sampleSnippet = "sealed interface AstNode\ndata class Lit(val v: Long): AstNode\nfun eval(node: AstNode): Long = when(node) { is Lit -> node.v }",
            description = "Modern concise typed language with coroutines, smart casts, and multiplatform native targets."
        ),
        FormatItem(
            name = "Rust",
            extensions = listOf(".rs"),
            category = FormatCategory.SCRIPTING_TYPED,
            paradigm = LanguageParadigm.MULTI_PARADIGM,
            executionModel = "rustc LLVM -> Native Machine Code",
            syntaxSignature = "fn main() -> Result<(), Box<dyn Error>>",
            sampleSnippet = "fn parse_tokens(input: &str) -> Vec<Token> {\n    input.split_whitespace().map(Token::from).collect()\n}",
            description = "Empowering everyone to build reliable and efficient software with affine borrow checker."
        ),
        FormatItem(
            name = "Python 3",
            extensions = listOf(".py", ".pyw", ".pyi"),
            category = FormatCategory.SCRIPTING_TYPED,
            paradigm = LanguageParadigm.MULTI_PARADIGM,
            executionModel = "CPython Bytecode VM / PyPy JIT",
            magicSignature = "A1 0D 0D 0A (pyc header)",
            syntaxSignature = "def parse(source: str) -> AST:",
            sampleSnippet = "def tokenize(code: str) -> list[str]:\n    return [w for w in code.split() if w]",
            description = "High-level dynamically typed programming language emphasizing code readability and vast ecosystem."
        ),
        FormatItem(
            name = "Go (Golang)",
            extensions = listOf(".go"),
            category = FormatCategory.SCRIPTING_TYPED,
            paradigm = LanguageParadigm.IMPERATIVE,
            executionModel = "Go Compiler Native Binary with CSP Runtime",
            syntaxSignature = "func Parse(r io.Reader) (*AST, error)",
            sampleSnippet = "package main\nimport \"fmt\"\nfunc main() {\n    ch := make(chan int)\n    go func() { ch <- 42 }()\n    fmt.Println(<-ch)\n}",
            description = "Open source programming language making it easy to build simple, fast, and reliable software."
        ),
        FormatItem(
            name = "Zig",
            extensions = listOf(".zig"),
            category = FormatCategory.SCRIPTING_TYPED,
            paradigm = LanguageParadigm.IMPERATIVE,
            executionModel = "Zig LLVM / Self-hosted Native Backend",
            syntaxSignature = "pub fn main() !void { const stdout = ...; }",
            sampleSnippet = "const std = @import(\"std\");\npub fn main() !void {\n    std.debug.print(\"OmniLang Zig Target\\n\", .{});\n}",
            description = "General-purpose programming language and toolchain for maintaining robust, optimal and reusable software."
        ),

        // Category 3: SYSTEMS, ASSEMBLY & BYTECODE BINARIES
        FormatItem(
            name = "Android Dalvik / ART Bytecode (DEX)",
            extensions = listOf(".dex", ".smali"),
            category = FormatCategory.SYSTEMS_BYTECODE,
            paradigm = LanguageParadigm.LOW_LEVEL_ASM,
            executionModel = "Android Runtime (ART) Ahead-Of-Time / JIT compiler",
            magicSignature = "64 65 78 0A 30 33 39 00 ('dex\\n039\\0')",
            syntaxSignature = ".method public static main([Ljava/lang/String;)V",
            sampleSnippet = "const v0, 0x2a\nconst v1, 0x10\nadd-int v2, v0, v1\nreturn v2",
            description = "Standard Dalvik register-based executable format powering 3+ billion Android devices worldwide."
        ),
        FormatItem(
            name = "WebAssembly (WASM & WAT)",
            extensions = listOf(".wasm", ".wat"),
            category = FormatCategory.SYSTEMS_BYTECODE,
            paradigm = LanguageParadigm.LOW_LEVEL_ASM,
            executionModel = "Wasmtime / V8 Liftoff & TurboFan / Browser Runtime",
            magicSignature = "00 61 73 6D ('\\0asm')",
            syntaxSignature = "(module (func " + '$' + "add (param " + '$' + "lhs i32) (param " + '$' + "rhs i32) (result i32) ...))",
            sampleSnippet = "(module\n  (func (export \"add\") (param i32 i32) (result i32)\n    local.get 0\n    local.get 1\n    i32.add))",
            description = "Binary instruction format for a stack-based virtual machine designed as a portable compilation target."
        ),
        FormatItem(
            name = "JVM Bytecode (.class)",
            extensions = listOf(".class", ".jar"),
            category = FormatCategory.SYSTEMS_BYTECODE,
            paradigm = LanguageParadigm.LOW_LEVEL_ASM,
            executionModel = "HotSpot JVM JIT (C1/C2) / GraalVM",
            magicSignature = "CA FE BA BE",
            syntaxSignature = "iload_1, iload_2, iadd, ireturn",
            sampleSnippet = "bipush 42\nistore_1\niload_1\ninvokestatic PrintStream/println(I)V",
            description = "Stack-oriented bytecode executed on Java Virtual Machine with class verification."
        ),
        FormatItem(
            name = "LLVM Intermediate Representation (LLVM IR)",
            extensions = listOf(".ll", ".bc"),
            category = FormatCategory.SYSTEMS_BYTECODE,
            paradigm = LanguageParadigm.LOW_LEVEL_ASM,
            executionModel = "LLVM Optimization Passes & Native Target Codegen",
            magicSignature = "42 43 C0 DE (Bitcode)",
            syntaxSignature = "define i32 @add(i32 %a, i32 %b) #0 { %1 = add nsw i32 %a, %b ret i32 %1 }",
            sampleSnippet = "define i32 @main() {\n    %res = add i32 10, 32\n    ret i32 %res\n}",
            description = "Static Single Assignment (SSA) based representation for universal compiler backends."
        ),
        FormatItem(
            name = "ARM64 Assembly (AArch64)",
            extensions = listOf(".s", ".asm"),
            category = FormatCategory.SYSTEMS_BYTECODE,
            paradigm = LanguageParadigm.LOW_LEVEL_ASM,
            executionModel = "Direct CPU Silicon Execution (Cortex / Apple Silicon)",
            syntaxSignature = "mov x0, #42; add x2, x0, x1; ret",
            sampleSnippet = ".global _start\n_start:\n    mov x0, #42\n    mov x16, #1\n    svc #0x80",
            description = "64-bit RISC instruction set architecture standard for modern smartphones and servers."
        ),

        // Category 4: MOBILE, OS & GAME DEV ENGINES
        FormatItem(
            name = "Jetpack Compose",
            extensions = listOf(".kt"),
            category = FormatCategory.MOBILE_GAMES,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Compose Runtime Positional Memoization Slot Table",
            syntaxSignature = "@Composable fun Screen() { Column { ... } }",
            sampleSnippet = "@Composable\nfun CompilerDashboard() {\n    Text(\"OmniLang Active\", color = CyanPrimary)\n}",
            description = "Android modern declarative UI toolkit that simplifies and accelerates UI development."
        ),
        FormatItem(
            name = "Godot GDScript",
            extensions = listOf(".gd"),
            category = FormatCategory.MOBILE_GAMES,
            paradigm = LanguageParadigm.OBJECT_ORIENTED,
            executionModel = "Godot Engine C++ Core Interpreter / GDExtension",
            syntaxSignature = "extends Node2D; func _ready(): pass",
            sampleSnippet = "extends CharacterBody2D\nfunc _physics_process(delta):\n    velocity.y += 980 * delta\n    move_and_slide()",
            description = "High-level, dynamically typed language tightly integrated into the Godot game engine."
        ),
        FormatItem(
            name = "Roblox Luau",
            extensions = listOf(".luau", ".lua"),
            category = FormatCategory.MOBILE_GAMES,
            paradigm = LanguageParadigm.MULTI_PARADIGM,
            executionModel = "Luau AOT/JIT Bytecode VM",
            syntaxSignature = "local player: Player = Players.LocalPlayer",
            sampleSnippet = "type Vector3 = { x: number, y: number, z: number }\nlocal function dist(v: Vector3): number\n    return math.sqrt(v.x^2 + v.y^2)\nend",
            description = "Fast, small, safe, gradually typed embeddable scripting language derived from Lua 5.1."
        ),

        // Category 5: DATABASE, QUERY & DATA SERIALIZATION
        FormatItem(
            name = "Protocol Buffers (Protobuf v3)",
            extensions = listOf(".proto"),
            category = FormatCategory.DATA_SERIALIZATION,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "protoc Compiler -> Code Generators",
            syntaxSignature = "syntax = \"proto3\"; message Request { ... }",
            sampleSnippet = "syntax = \"proto3\";\nmessage AstPayload {\n  string node_type = 1;\n  int64 line = 2;\n  bytes binary_blob = 3;\n}",
            description = "Google language-neutral, platform-neutral, extensible mechanism for serializing structured data."
        ),
        FormatItem(
            name = "JSON5 / JSONC",
            extensions = listOf(".json5", ".jsonc", ".json"),
            category = FormatCategory.DATA_SERIALIZATION,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Zero-copy Streaming JSON Parsers",
            syntaxSignature = "{ // Comments allowed, trailingComma: true, hex: 0x1F }",
            sampleSnippet = "{\n  // Compiler Configuration\n  \"target\": \"DEX\",\n  \"optimization\": 0x03,\n}",
            description = "Human-friendly superset of JSON with comments, trailing commas, and unquoted keys."
        ),
        FormatItem(
            name = "GraphQL",
            extensions = listOf(".graphql", ".gql"),
            category = FormatCategory.DATA_SERIALIZATION,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "GraphQL Execution Engine / Resolver Tree",
            syntaxSignature = "query GetTree(" + '$' + "id: ID!) { ast(id: " + '$' + "id) { type } }",
            sampleSnippet = "query CompileProject(" + '$' + "source: String!) {\n  compile(code: " + '$' + "source) {\n    tokens { type lexeme }\n    astJson\n  }\n}",
            description = "Query language for APIs and runtime for fulfilling queries with existing data."
        ),

        // Category 6: SHELL, CLI, SYSTEM ADMIN & SCRIPTS
        FormatItem(
            name = "Bash / POSIX Shell",
            extensions = listOf(".sh", ".bash"),
            category = FormatCategory.SHELL_ADMIN,
            paradigm = LanguageParadigm.IMPERATIVE,
            executionModel = "GNU Bash Command Evaluator & Subshell Forking",
            syntaxSignature = "#!/bin/bash; if [[ " + '$' + "? -eq 0 ]]; then ... fi",
            sampleSnippet = "#!/usr/bin/env bash\nset -euo pipefail\necho \"Building OmniLang Artifacts...\"\ngradle :app:assembleDebug",
            description = "Unix shell and command language written for the GNU Project."
        ),
        FormatItem(
            name = "CMake / Ninja",
            extensions = listOf("CMakeLists.txt", ".cmake", ".ninja"),
            category = FormatCategory.SHELL_ADMIN,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Meta-Build System Generating Ninja / Makefiles",
            syntaxSignature = "cmake_minimum_required(VERSION 3.20); add_executable(app ...)",
            sampleSnippet = "cmake_minimum_required(VERSION 3.25)\nproject(OmniCompiler LANGUAGES CXX)\nadd_executable(omnic src/main.cpp)",
            description = "Cross-platform open-source build system generator for native toolchains."
        ),

        // Category 7: HARDWARE, GPU, SHADERS & EMBEDDED
        FormatItem(
            name = "WGSL (WebGPU Shading Language)",
            extensions = listOf(".wgsl"),
            category = FormatCategory.HARDWARE_SHADERS,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Tint Compiler -> Vulkan SPIR-V / Metal MSL / HLSL",
            syntaxSignature = "@vertex fn vs_main(@builtin(vertex_index) in_vertex_index: u32) -> @builtin(position) vec4<f32>",
            sampleSnippet = "@fragment\nfn fs_main() -> @location(0) vec4<f32> {\n    return vec4<f32>(0.22, 0.74, 0.97, 1.0);\n}",
            description = "Modern shader language designed for the WebGPU specification across Vulkan, Metal, and DirectX."
        ),
        FormatItem(
            name = "Verilog / SystemVerilog",
            extensions = listOf(".v", ".sv", ".svh"),
            category = FormatCategory.HARDWARE_SHADERS,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "FPGA Synthesis (Yosys/Vivado) / Verilator Simulation",
            syntaxSignature = "module Alu (input logic [31:0] a, b, output logic [31:0] out);",
            sampleSnippet = "module RegBank (\n    input clk, input [3:0] addr,\n    output logic [31:0] data\n);\n    always_ff @(posedge clk) begin\n        data <= registers[addr];\n    end\nendmodule",
            description = "Hardware description language used to model electronic systems, ASICs, and FPGAs."
        ),

        // Category 8: ESOTERIC, MATHEMATICAL & DOMAIN-SPECIFIC
        FormatItem(
            name = "Brainfuck",
            extensions = listOf(".bf", ".b"),
            category = FormatCategory.ESOTERIC_MATH,
            paradigm = LanguageParadigm.LOW_LEVEL_ASM,
            executionModel = "Turing Complete 30,000-byte tape machine",
            syntaxSignature = "><+-.,[]",
            sampleSnippet = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.",
            description = "Minimalist Turing-complete esoteric language consisting of only 8 commands."
        ),
        FormatItem(
            name = "Typst",
            extensions = listOf(".typ"),
            category = FormatCategory.ESOTERIC_MATH,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Typst Rust Incremental Typesetting Engine",
            syntaxSignature = "#set page(paper: \"a4\"); = Heading",
            sampleSnippet = "#set text(font: \"JetBrains Mono\", size: 10pt)\n= Compiler Architecture\n" + '$' + " integral_0^infinity e^(-x^2) dif x = sqrt(pi) / 2 " + '$',
            description = "New markup-based typesetting system designed to be as powerful as LaTeX yet simpler to learn."
        ),
        FormatItem(
            name = "Mermaid Diagram Spec",
            extensions = listOf(".mmd", ".mermaid"),
            category = FormatCategory.ESOTERIC_MATH,
            paradigm = LanguageParadigm.DECLARATIVE,
            executionModel = "Mermaid.js SVG Graph Renderer",
            syntaxSignature = "graph TD; A[Source] --> B[Lexer]; B --> C[Parser];",
            sampleSnippet = "graph LR\n  Source[Code Source] --> Lexer\n  Lexer --> Tokens\n  Tokens --> Parser\n  Parser --> AST\n  AST --> ExecEngine",
            description = "JavaScript-based diagramming and charting tool that uses Markdown-inspired text definitions."
        )
    )

    fun search(query: String, category: FormatCategory? = null, paradigm: LanguageParadigm? = null): List<FormatItem> {
        val q = query.trim().lowercase()
        return items.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesParadigm = paradigm == null || item.paradigm == paradigm
            val matchesQuery = q.isEmpty() ||
                    item.name.lowercase().contains(q) ||
                    item.extensions.any { it.lowercase().contains(q) } ||
                    item.syntaxSignature.lowercase().contains(q) ||
                    item.description.lowercase().contains(q)

            matchesCategory && matchesParadigm && matchesQuery
        }
    }
}
