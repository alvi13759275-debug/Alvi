package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.model.Token
import com.example.compiler.model.TokenType
import com.example.ui.theme.*

@Composable
fun Phase2LexerView(
    tokens: List<Token>,
    tokenTimeUs: Long,
    diagnostics: List<String>,
    modifier: Modifier = Modifier
) {
    var selectedToken by remember { mutableStateOf<Token?>(tokens.firstOrNull()) }
    var selectedFilter by remember { mutableStateOf<String?>("All") }

    val categories = listOf("All", "Keyword", "Identifier", "Number", "String", "Operator", "Delimiter")

    val filteredTokens = remember(tokens, selectedFilter) {
        if (selectedFilter == null || selectedFilter == "All") {
            tokens.filter { it.type != TokenType.EOF }
        } else {
            tokens.filter { it.type.category == selectedFilter && it.type != TokenType.EOF }
        }
    }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("phase2_lexer_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Metrics Bar with Workable Copy Stream Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                            ) {
                                Text(
                                    text = "ARCHITECT: ALVI",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = EmeraldSuccess
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Phase 2: Lexer",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val sb = StringBuilder()
                                sb.append("POS\tCATEGORY\tTYPE\tLEXEME\n")
                                tokens.forEach { t ->
                                    sb.append("${t.line}:${t.column}\t${t.type.category}\t${t.type.name}\t${t.lexeme}\n")
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("tokens-tsv", sb.toString()))
                                Toast.makeText(context, "Copied ${tokens.size} tokens as TSV!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Stream", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tokens Emitted", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                            Text(
                                text = "${tokens.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Scan Time", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                            Text(
                                text = "$tokenTimeUs µs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Diagnostics", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                            Text(
                                text = "${diagnostics.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (diagnostics.isEmpty()) EmeraldSuccess else RoseError
                            )
                        }
                    }
                }
            }
        }

        // Diagnostics Warning
        if (diagnostics.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseError)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RoseError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lexical Diagnostics", fontWeight = FontWeight.Bold, color = RoseError)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        diagnostics.forEach { diag ->
                            Text(diag, fontFamily = JetBrainsMono, fontSize = 12.sp, color = TextPrimaryDark)
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Text(
                text = "TOKEN STREAM FILTER",
                style = MaterialTheme.typography.labelSmall,
                color = CyanPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanPrimary
                        )
                    )
                }
            }
        }

        // Selected Token Detail Inspector
        item {
            selectedToken?.let { token ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Token Inspector",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Line ${token.line}, Col ${token.column}",
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (token.lexeme.isEmpty()) "<EOF>" else token.lexeme,
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = getTokenColor(token.type)
                                )
                                Text(
                                    text = "Type: ${token.type.name} (${token.type.category})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (token.literal != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkBackground
                                ) {
                                    Text(
                                        text = "Val: ${token.literal}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontFamily = JetBrainsMono,
                                        fontSize = 12.sp,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Token Stream Grid / List
        item {
            Text(
                text = "TOKEN STREAM (${filteredTokens.size})",
                style = MaterialTheme.typography.labelSmall,
                color = AmberAccent,
                fontWeight = FontWeight.Bold
            )
        }

        items(filteredTokens) { token ->
            val isSelected = token == selectedToken
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DarkSurfaceElevated else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedToken = token }
                    .testTag("token_item_${token.line}_${token.column}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = getTokenColor(token.type).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, getTokenColor(token.type).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = token.type.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = getTokenColor(token.type),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (token.lexeme.isEmpty()) "<EOF>" else token.lexeme,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.SemiBold,
                            color = getTokenColor(token.type),
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "${token.line}:${token.column}",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }
            }
        }
    }
}

private fun getTokenColor(type: TokenType): Color {
    return when (type) {
        TokenType.FN, TokenType.LET, TokenType.MUT, TokenType.CONST,
        TokenType.IF, TokenType.ELSE, TokenType.WHILE, TokenType.RETURN,
        TokenType.PRINT, TokenType.MATCH, TokenType.STRUCT -> SyntaxKeyword

        TokenType.IDENTIFIER -> SyntaxFunction
        TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL, TokenType.HEX_LITERAL -> SyntaxNumber
        TokenType.STRING_LITERAL -> SyntaxString
        TokenType.BOOLEAN_LITERAL -> AmberAccent
        TokenType.REGISTER -> SyntaxRegister
        TokenType.REG_OPCODE -> VioletSyntax
        TokenType.COMMENT -> SyntaxComment
        TokenType.ERROR -> RoseError

        TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH,
        TokenType.PERCENT, TokenType.ASSIGN, TokenType.EQUAL_EQUAL,
        TokenType.PIPE_FORWARD, TokenType.ARROW -> SyntaxOperator

        else -> TextSecondaryDark
    }
}
