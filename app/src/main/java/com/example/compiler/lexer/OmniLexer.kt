package com.example.compiler.lexer

import com.example.compiler.model.Token
import com.example.compiler.model.TokenType

class OmniLexer(private val source: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    private var column = 1
    private var lineStart = 0

    private val tokens = mutableListOf<Token>()
    val diagnostics = mutableListOf<String>()

    fun tokenize(): List<Token> {
        tokens.clear()
        diagnostics.clear()
        start = 0
        current = 0
        line = 1
        column = 1
        lineStart = 0

        while (!isAtEnd()) {
            start = current
            val col = current - lineStart + 1
            scanToken(col)
        }

        tokens.add(Token(TokenType.EOF, "", null, line, current - lineStart + 1, current, current))
        return tokens
    }

    private fun scanToken(tokenCol: Int) {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LPAREN, tokenCol)
            ')' -> addToken(TokenType.RPAREN, tokenCol)
            '{' -> addToken(TokenType.LBRACE, tokenCol)
            '}' -> addToken(TokenType.RBRACE, tokenCol)
            '[' -> addToken(TokenType.LBRACKET, tokenCol)
            ']' -> addToken(TokenType.RBRACKET, tokenCol)
            ',' -> addToken(TokenType.COMMA, tokenCol)
            ';' -> addToken(TokenType.SEMICOLON, tokenCol)
            '.' -> addToken(TokenType.DOT, tokenCol)
            '+' -> addToken(TokenType.PLUS, tokenCol)
            '*' -> addToken(TokenType.STAR, tokenCol)
            '%' -> addToken(TokenType.PERCENT, tokenCol)

            ':' -> {
                if (match(':')) addToken(TokenType.COLON_COLON, tokenCol)
                else addToken(TokenType.COLON, tokenCol)
            }

            '-' -> {
                if (match('>')) addToken(TokenType.ARROW, tokenCol)
                else addToken(TokenType.MINUS, tokenCol)
            }

            '=' -> {
                if (match('=')) addToken(TokenType.EQUAL_EQUAL, tokenCol)
                else if (match('>')) addToken(TokenType.FAT_ARROW, tokenCol)
                else addToken(TokenType.ASSIGN, tokenCol)
            }

            '!' -> {
                if (match('=')) addToken(TokenType.BANG_EQUAL, tokenCol)
                else addToken(TokenType.BANG, tokenCol)
            }

            '<' -> {
                if (match('=')) addToken(TokenType.LESS_EQUAL, tokenCol)
                else addToken(TokenType.LESS, tokenCol)
            }

            '>' -> {
                if (match('=')) addToken(TokenType.GREATER_EQUAL, tokenCol)
                else addToken(TokenType.GREATER, tokenCol)
            }

            '|' -> {
                if (match('>')) addToken(TokenType.PIPE_FORWARD, tokenCol)
                else if (match('|')) addToken(TokenType.OR, tokenCol)
                else addError("Unexpected standalone '|', did you mean '|>' or '||'?", tokenCol)
            }

            '&' -> {
                if (match('&')) addToken(TokenType.AND, tokenCol)
                else addError("Unexpected '&', did you mean '&&'?", tokenCol)
            }

            '/' -> {
                if (match('/')) {
                    // Line comment
                    while (peek() != '\n' && !isAtEnd()) advance()
                    val commentText = source.substring(start, current)
                    tokens.add(Token(TokenType.COMMENT, commentText, commentText, line, tokenCol, start, current))
                } else if (match('*')) {
                    // Block comment
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                        if (peek() == '\n') {
                            line++
                            lineStart = current + 1
                        }
                        advance()
                    }
                    if (isAtEnd()) {
                        addError("Unterminated block comment", tokenCol)
                    } else {
                        advance() // *
                        advance() // /
                        val commentText = source.substring(start, current)
                        tokens.add(Token(TokenType.COMMENT, commentText, commentText, line, tokenCol, start, current))
                    }
                } else {
                    addToken(TokenType.SLASH, tokenCol)
                }
            }

            '#' -> {
                // Shell / Python style comment or immediate prefix
                if (peek().isDigit() || (peek() == '0' && peekNext() == 'x')) {
                    advance()
                    number(tokenCol)
                } else {
                    while (peek() != '\n' && !isAtEnd()) advance()
                    val commentText = source.substring(start, current)
                    tokens.add(Token(TokenType.COMMENT, commentText, commentText, line, tokenCol, start, current))
                }
            }

            ' ', '\r', '\t' -> {
                // Ignore whitespace
            }

            '\n' -> {
                line++
                lineStart = current
            }

            '"' -> string(tokenCol)

            else -> {
                if (c.isDigit()) {
                    number(tokenCol)
                } else if (c.isLetter() || c == '_' || c == '.') {
                    identifier(tokenCol)
                } else {
                    addError("Unexpected character '$c'", tokenCol)
                }
            }
        }
    }

    private fun identifier(tokenCol: Int) {
        while (peek().isLetterOrDigit() || peek() == '_' || peek() == '-') {
            advance()
        }

        val text = source.substring(start, current)

        // Check if register: v0..v15 or r0..r15
        if (Regex("^[vr][0-9]{1,2}$").matches(text)) {
            tokens.add(Token(TokenType.REGISTER, text, text, line, tokenCol, start, current))
            return
        }

        // Keywords mapping
        val type = when (text) {
            "fn" -> TokenType.FN
            "let" -> TokenType.LET
            "mut" -> TokenType.MUT
            "const" -> TokenType.CONST
            "if" -> TokenType.IF
            "else" -> TokenType.ELSE
            "while" -> TokenType.WHILE
            "return" -> TokenType.RETURN
            "print" -> TokenType.PRINT
            "match" -> TokenType.MATCH
            "struct" -> TokenType.STRUCT
            "type" -> TokenType.TYPE
            "async" -> TokenType.ASYNC
            "await" -> TokenType.AWAIT
            "emit" -> TokenType.EMIT
            "true" -> TokenType.BOOLEAN_LITERAL
            "false" -> TokenType.BOOLEAN_LITERAL
            ".method", ".end", "add-int", "sub-int", "mul-int", "div-int", "move", "registers" -> TokenType.REG_OPCODE
            else -> TokenType.IDENTIFIER
        }

        val literal = when (type) {
            TokenType.BOOLEAN_LITERAL -> text.toBoolean()
            else -> null
        }

        tokens.add(Token(type, text, literal, line, tokenCol, start, current))
    }

    private fun number(tokenCol: Int) {
        if (source[start] == '0' && (peek() == 'x' || peek() == 'X')) {
            advance() // 'x'
            while (isHexDigit(peek())) advance()
            val text = source.substring(start, current)
            val cleanHex = text.replace("#", "").removePrefix("0x").removePrefix("0X")
            val value = cleanHex.toLongOrNull(16) ?: 0L
            tokens.add(Token(TokenType.HEX_LITERAL, text, value, line, tokenCol, start, current))
            return
        }

        while (peek().isDigit()) advance()

        if (peek() == '.' && peekNext().isDigit()) {
            advance() // '.'
            while (peek().isDigit()) advance()
            val text = source.substring(start, current).replace("#", "")
            val value = text.toDoubleOrNull() ?: 0.0
            tokens.add(Token(TokenType.FLOAT_LITERAL, text, value, line, tokenCol, start, current))
        } else {
            val text = source.substring(start, current).replace("#", "")
            val value = text.toLongOrNull() ?: 0L
            tokens.add(Token(TokenType.INT_LITERAL, text, value, line, tokenCol, start, current))
        }
    }

    private fun string(tokenCol: Int) {
        val sb = java.lang.StringBuilder()
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
                lineStart = current + 1
            }
            if (peek() == '\\') {
                advance() // skip \
                when (peek()) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    'r' -> sb.append('\r')
                    '\\' -> sb.append('\\')
                    '"' -> sb.append('"')
                    else -> sb.append(peek())
                }
            } else {
                sb.append(peek())
            }
            advance()
        }

        if (isAtEnd()) {
            addError("Unterminated string literal", tokenCol)
            return
        }

        advance() // The closing "
        val fullLexeme = source.substring(start, current)
        tokens.add(Token(TokenType.STRING_LITERAL, fullLexeme, sb.toString(), line, tokenCol, start, current))
    }

    private fun addToken(type: TokenType, tokenCol: Int) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, null, line, tokenCol, start, current))
    }

    private fun addError(message: String, tokenCol: Int) {
        val errorMsg = "Lexer Error [Line $line, Col $tokenCol]: $message"
        diagnostics.add(errorMsg)
        val text = if (start < current) source.substring(start, current) else ""
        tokens.add(Token(TokenType.ERROR, text, message, line, tokenCol, start, current))
    }

    private fun advance(): Char = source[current++]

    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]
    private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]
    private fun isAtEnd(): Boolean = current >= source.length
    private fun isHexDigit(c: Char): Boolean = c.isDigit() || (c in 'a'..'f') || (c in 'A'..'F')
}
