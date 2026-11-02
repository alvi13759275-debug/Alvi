package com.example.compiler.parser

import com.example.compiler.model.*

class OmniParser(private val tokens: List<Token>) {
    private var current = 0
    val errors = mutableListOf<String>()

    fun parse(): ProgramNode {
        errors.clear()
        current = 0
        val statements = mutableListOf<AstNode>()

        while (!isAtEnd()) {
            // Skip standalone comments or stray semicolons
            if (match(TokenType.COMMENT) || match(TokenType.SEMICOLON)) continue

            try {
                declaration()?.let { statements.add(it) }
            } catch (e: ParseException) {
                errors.add(e.message ?: "Unknown parse error")
                synchronize()
            }
        }

        return ProgramNode(statements, 1)
    }

    private fun declaration(): AstNode? {
        return when {
            match(TokenType.FN) -> functionDeclaration()
            match(TokenType.STRUCT) -> structDeclaration()
            match(TokenType.LET) -> varDeclaration()
            match(TokenType.REG_OPCODE) -> registerOpcodeDeclaration()
            match(TokenType.CONST) -> dexConstDeclaration()
            else -> statement()
        }
    }

    private fun functionDeclaration(): FunctionDefNode {
        val nameToken = consume(TokenType.IDENTIFIER, "Expect function name after 'fn'")
        consume(TokenType.LPAREN, "Expect '(' after function name")
        val parameters = mutableListOf<ParameterNode>()

        if (!check(TokenType.RPAREN)) {
            do {
                val paramName = consume(TokenType.IDENTIFIER, "Expect parameter name").lexeme
                var paramType = "Any"
                if (match(TokenType.COLON)) {
                    paramType = consume(TokenType.IDENTIFIER, "Expect type after ':'").lexeme
                }
                parameters.add(ParameterNode(paramName, paramType))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RPAREN, "Expect ')' after parameters")

        var returnType = "Any"
        if (match(TokenType.COLON)) {
            returnType = consume(TokenType.IDENTIFIER, "Expect return type after ':'").lexeme
        }

        val body = blockStatement()
        return FunctionDefNode(nameToken.lexeme, parameters, returnType, body, nameToken.line)
    }

    private fun structDeclaration(): StructDefNode {
        val nameToken = consume(TokenType.IDENTIFIER, "Expect struct name")
        consume(TokenType.LBRACE, "Expect '{' before struct fields")
        val fields = mutableListOf<Pair<String, String>>()

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (match(TokenType.COMMENT) || match(TokenType.SEMICOLON) || match(TokenType.COMMA)) continue
            val fieldName = consume(TokenType.IDENTIFIER, "Expect field name").lexeme
            var fieldType = "Any"
            if (match(TokenType.COLON)) {
                fieldType = consume(TokenType.IDENTIFIER, "Expect type").lexeme
            }
            fields.add(fieldName to fieldType)
            match(TokenType.COMMA)
        }
        consume(TokenType.RBRACE, "Expect '}' after struct fields")
        return StructDefNode(nameToken.lexeme, fields, nameToken.line)
    }

    private fun varDeclaration(): VariableDeclNode {
        val isMut = match(TokenType.MUT)
        val nameToken = consume(TokenType.IDENTIFIER, "Expect variable name")
        var type: String? = null
        if (match(TokenType.COLON)) {
            type = consume(TokenType.IDENTIFIER, "Expect type name").lexeme
        }

        consume(TokenType.ASSIGN, "Expect '=' after variable declaration")
        val initializer = expression()
        match(TokenType.SEMICOLON)
        return VariableDeclNode(nameToken.lexeme, type, isMut, initializer, nameToken.line)
    }

    private fun registerOpcodeDeclaration(): AstNode {
        val opcodeToken = previous()
        val opcode = opcodeToken.lexeme

        if (opcode == ".method") {
            // Method declaration in assembly
            val name = if (check(TokenType.IDENTIFIER)) advance().lexeme else "main"
            while (!check(TokenType.LBRACE) && peek().line == opcodeToken.line && !isAtEnd()) {
                advance()
            }
            val stmts = mutableListOf<AstNode>()
            while (!check(TokenType.REG_OPCODE) && !isAtEnd()) {
                declaration()?.let { stmts.add(it) }
            }
            if (match(TokenType.REG_OPCODE) && previous().lexeme == ".end") {
                // Done
            }
            return FunctionDefNode(name, emptyList(), "Unit", BlockNode(stmts, opcodeToken.line), opcodeToken.line)
        }

        // Register instruction: e.g. add-int v2, v0, v1
        val dest = consume(TokenType.REGISTER, "Expect destination register").lexeme
        var src1: String? = null
        var src2: String? = null
        var imm: Long? = null

        if (match(TokenType.COMMA)) {
            if (check(TokenType.REGISTER)) {
                src1 = advance().lexeme
                if (match(TokenType.COMMA)) {
                    if (check(TokenType.REGISTER)) {
                        src2 = advance().lexeme
                    } else if (check(TokenType.INT_LITERAL) || check(TokenType.HEX_LITERAL)) {
                        imm = (advance().literal as? Long) ?: 0L
                    }
                }
            } else if (check(TokenType.INT_LITERAL) || check(TokenType.HEX_LITERAL)) {
                imm = (advance().literal as? Long) ?: 0L
            }
        }
        match(TokenType.SEMICOLON)
        return RegisterOpNode(opcode, dest, src1, src2, imm, opcodeToken.line)
    }

    private fun dexConstDeclaration(): AstNode {
        val line = previous().line
        val dest = consume(TokenType.REGISTER, "Expect register for const instruction").lexeme
        consume(TokenType.COMMA, "Expect ',' after register")
        val immToken = if (check(TokenType.INT_LITERAL) || check(TokenType.HEX_LITERAL)) advance()
        else throw error(peek(), "Expect immediate number for const")

        val imm = (immToken.literal as? Long) ?: 0L
        match(TokenType.SEMICOLON)
        return RegisterOpNode("const", dest, null, null, imm, line)
    }

    private fun statement(): AstNode {
        return when {
            match(TokenType.IF) -> ifStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.RETURN) -> returnStatement()
            match(TokenType.PRINT) -> printStatement()
            match(TokenType.LBRACE) -> blockStatement()
            else -> expressionStatement()
        }
    }

    private fun ifStatement(): IfNode {
        val line = previous().line
        consume(TokenType.LPAREN, "Expect '(' after 'if'")
        val condition = expression()
        consume(TokenType.RPAREN, "Expect ')' after if condition")
        val thenBranch = blockStatement()
        var elseBranch: BlockNode? = null
        if (match(TokenType.ELSE)) {
            elseBranch = if (match(TokenType.IF)) {
                BlockNode(listOf(ifStatement()), line)
            } else {
                blockStatement()
            }
        }
        return IfNode(condition, thenBranch, elseBranch, line)
    }

    private fun whileStatement(): WhileNode {
        val line = previous().line
        consume(TokenType.LPAREN, "Expect '(' after 'while'")
        val condition = expression()
        consume(TokenType.RPAREN, "Expect ')' after condition")
        val body = blockStatement()
        return WhileNode(condition, body, line)
    }

    private fun returnStatement(): ReturnNode {
        val line = previous().line
        var expr: AstNode? = null
        if (!check(TokenType.SEMICOLON) && peek().line == line) {
            expr = expression()
        }
        match(TokenType.SEMICOLON)
        return ReturnNode(expr, line)
    }

    private fun printStatement(): AstNode {
        val line = previous().line
        val hasParen = match(TokenType.LPAREN)
        val expr = expression()
        if (hasParen) {
            consume(TokenType.RPAREN, "Expect ')' after print argument")
        }
        match(TokenType.SEMICOLON)
        return PrintNode(expr, line)
    }

    private fun blockStatement(): BlockNode {
        val line = previous().line
        consume(TokenType.LBRACE, "Expect '{' before block")
        val statements = mutableListOf<AstNode>()
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (match(TokenType.COMMENT) || match(TokenType.SEMICOLON)) continue
            declaration()?.let { statements.add(it) }
        }
        consume(TokenType.RBRACE, "Expect '}' after block")
        return BlockNode(statements, line)
    }

    private fun expressionStatement(): AstNode {
        // Check if assignment: IDENT = expr
        if (check(TokenType.IDENTIFIER) && peekNext().type == TokenType.ASSIGN) {
            val name = advance().lexeme
            advance() // skip =
            val value = expression()
            match(TokenType.SEMICOLON)
            return AssignmentNode(name, value, previous().line)
        }

        val expr = expression()
        match(TokenType.SEMICOLON)
        return expr
    }

    // Precedence climbing
    private fun expression(): AstNode = pipeExpression()

    private fun pipeExpression(): AstNode {
        var expr = logicalOr()
        while (match(TokenType.PIPE_FORWARD)) {
            val right = logicalOr()
            expr = PipeNode(expr, right, previous().line)
        }
        return expr
    }

    private fun logicalOr(): AstNode {
        var expr = logicalAnd()
        while (match(TokenType.OR)) {
            val op = previous().lexeme
            val right = logicalAnd()
            expr = BinaryOpNode(expr, op, right, previous().line)
        }
        return expr
    }

    private fun logicalAnd(): AstNode {
        var expr = equality()
        while (match(TokenType.AND)) {
            val op = previous().lexeme
            val right = equality()
            expr = BinaryOpNode(expr, op, right, previous().line)
        }
        return expr
    }

    private fun equality(): AstNode {
        var expr = comparison()
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            val op = previous().lexeme
            val right = comparison()
            expr = BinaryOpNode(expr, op, right, previous().line)
        }
        return expr
    }

    private fun comparison(): AstNode {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val op = previous().lexeme
            val right = term()
            expr = BinaryOpNode(expr, op, right, previous().line)
        }
        return expr
    }

    private fun term(): AstNode {
        var expr = factor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous().lexeme
            val right = factor()
            expr = BinaryOpNode(expr, op, right, previous().line)
        }
        return expr
    }

    private fun factor(): AstNode {
        var expr = unary()
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            val op = previous().lexeme
            val right = unary()
            expr = BinaryOpNode(expr, op, right, previous().line)
        }
        return expr
    }

    private fun unary(): AstNode {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous().lexeme
            val right = unary()
            return UnaryOpNode(op, right, previous().line)
        }
        return primary()
    }

    private fun primary(): AstNode {
        val token = advance()
        return when (token.type) {
            TokenType.BOOLEAN_LITERAL -> LiteralNode(token.literal, "Bool", token.line)
            TokenType.INT_LITERAL -> LiteralNode(token.literal, "Int", token.line)
            TokenType.FLOAT_LITERAL -> LiteralNode(token.literal, "Float", token.line)
            TokenType.HEX_LITERAL -> LiteralNode(token.literal, "Hex", token.line)
            TokenType.STRING_LITERAL -> LiteralNode(token.literal, "String", token.line)
            TokenType.REGISTER -> IdentifierNode(token.lexeme, token.line)

            TokenType.IDENTIFIER -> {
                // Check if function call
                if (match(TokenType.LPAREN)) {
                    val args = mutableListOf<AstNode>()
                    if (!check(TokenType.RPAREN)) {
                        do {
                            args.add(expression())
                        } while (match(TokenType.COMMA))
                    }
                    consume(TokenType.RPAREN, "Expect ')' after arguments")
                    CallNode(token.lexeme, args, token.line)
                } else {
                    IdentifierNode(token.lexeme, token.line)
                }
            }

            TokenType.LPAREN -> {
                val expr = expression()
                consume(TokenType.RPAREN, "Expect ')' after expression")
                expr
            }

            else -> throw error(token, "Unexpected token '${token.lexeme}' in expression")
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF
    private fun peek(): Token = tokens[current]
    private fun peekNext(): Token = if (current + 1 < tokens.size) tokens[current + 1] else tokens[tokens.size - 1]
    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String): ParseException {
        val msg = "Parser Error [Line ${token.line}, Col ${token.column}]: $message (at '${token.lexeme}')"
        return ParseException(msg)
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return
            when (peek().type) {
                TokenType.FN, TokenType.LET, TokenType.IF, TokenType.WHILE,
                TokenType.RETURN, TokenType.PRINT, TokenType.STRUCT -> return
                else -> advance()
            }
        }
    }
}

class ParseException(message: String) : RuntimeException(message)
