package com.example.compiler.model

enum class TokenType(val category: String) {
    // Keywords
    KEYWORD("Keyword"),
    FN("Keyword"),
    LET("Keyword"),
    MUT("Keyword"),
    CONST("Keyword"),
    IF("Keyword"),
    ELSE("Keyword"),
    WHILE("Keyword"),
    RETURN("Keyword"),
    MATCH("Keyword"),
    STRUCT("Keyword"),
    TYPE("Keyword"),
    PRINT("Keyword"),
    ASYNC("Keyword"),
    AWAIT("Keyword"),
    EMIT("Keyword"),

    // Low-level & Bytecode
    REG_OPCODE("Opcode"),
    REGISTER("Register"), // v0..v15, r0..r15

    // Identifiers & Literals
    IDENTIFIER("Identifier"),
    INT_LITERAL("Number"),
    FLOAT_LITERAL("Number"),
    HEX_LITERAL("Number"),
    STRING_LITERAL("String"),
    BOOLEAN_LITERAL("Boolean"),

    // Operators
    PLUS("Operator"),
    MINUS("Operator"),
    STAR("Operator"),
    SLASH("Operator"),
    PERCENT("Operator"),
    ASSIGN("Operator"),
    EQUAL_EQUAL("Operator"),
    BANG_EQUAL("Operator"),
    LESS("Operator"),
    LESS_EQUAL("Operator"),
    GREATER("Operator"),
    GREATER_EQUAL("Operator"),
    AND("Operator"),
    OR("Operator"),
    BANG("Operator"),
    PIPE_FORWARD("Operator"), // |>
    ARROW("Operator"),        // ->
    FAT_ARROW("Operator"),    // =>

    // Delimiters
    LPAREN("Delimiter"),
    RPAREN("Delimiter"),
    LBRACE("Delimiter"),
    RBRACE("Delimiter"),
    LBRACKET("Delimiter"),
    RBRACKET("Delimiter"),
    COMMA("Delimiter"),
    COLON("Delimiter"),
    COLON_COLON("Delimiter"),
    SEMICOLON("Delimiter"),
    DOT("Delimiter"),

    // Meta & Diagnostics
    COMMENT("Comment"),
    ERROR("Error"),
    EOF("EOF")
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any? = null,
    val line: Int = 1,
    val column: Int = 1,
    val startOffset: Int = 0,
    val endOffset: Int = 0
) {
    override fun toString(): String {
        return "Token($type, '$lexeme', line=$line, col=$column)"
    }
}
