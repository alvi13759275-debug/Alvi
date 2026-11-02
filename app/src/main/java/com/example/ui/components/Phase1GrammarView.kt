package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.model.LanguageProfile
import com.example.ui.theme.*

@Composable
fun Phase1GrammarView(
    profile: LanguageProfile,
    onAskArchitect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("phase1_grammar_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner with ALVI Creator Badge & Workable Buttons
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                                    text = "Phase 1: Specs",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Formal Syntax Rules, Lexical Keywords & EBNF Grammar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                        ) {
                            Text(
                                text = profile.fileExtension,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Paradigm: ${profile.paradigm.displayName}") },
                                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("Target: ${profile.defaultExecution.badge}") },
                                leadingIcon = { Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        // Workable Copy EBNF Button
                        Button(
                            onClick = {
                                val ebnfText = StringBuilder()
                                ebnfText.append("(* Formal EBNF Grammar for ${profile.name} // Architect: ALVI *)\n\n")
                                profile.grammarSpecs.forEach { rule ->
                                    ebnfText.append("${rule.nonTerminal.padEnd(16)} ::= ${rule.production} ;\n")
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ebnf-spec", ebnfText.toString()))
                                Toast.makeText(context, "EBNF grammar specs copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy EBNF", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section: Keywords & Lexical Categories
        item {
            Text(
                text = "RESERVED KEYWORDS & IDENTIFIERS",
                style = MaterialTheme.typography.labelSmall,
                color = AmberAccent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Core Vocabulary (${profile.keywords.size} reserved tokens):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(profile.keywords) { kw ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SyntaxKeyword.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SyntaxKeyword.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = kw,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Medium,
                                    color = SyntaxKeyword,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Built-in Type Signatures:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(profile.typeNames) { typeName ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SyntaxType.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SyntaxType.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = typeName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Medium,
                                    color = SyntaxType,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Operator Precedence & Associativity
        item {
            Text(
                text = "OPERATOR PRECEDENCE & ASSOCIATIVITY",
                style = MaterialTheme.typography.labelSmall,
                color = VioletSyntax,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Operator", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                        Text("Precedence Level", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                        Text("Associativity", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    profile.operators.forEach { (op, prec) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = op,
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = SyntaxOperator,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Level $prec",
                                fontFamily = JetBrainsMono,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (op == "=") "Right-to-Left" else "Left-to-Right",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: EBNF Grammar Production Rules
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EBNF GRAMMAR PRODUCTION RULES",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${profile.grammarSpecs.size} Rules",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark
                )
            }
        }

        items(profile.grammarSpecs) { rule ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rule.nonTerminal,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimaryLight,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Non-Terminal",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "::= ${rule.production}",
                            modifier = Modifier.padding(8.dp),
                            fontFamily = JetBrainsMono,
                            color = EmeraldSuccess,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rule.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Consult Architect Button
        item {
            Button(
                onClick = { onAskArchitect("Analyze Phase 1 Grammar Specs for ${profile.name} and suggest optimization for ambiguous expressions.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phase1_consult_architect_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DarkBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Consult AI Compiler Architect on Phase 1", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
