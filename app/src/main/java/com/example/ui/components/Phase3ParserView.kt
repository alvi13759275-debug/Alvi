package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.model.*
import com.example.ui.theme.*

@Composable
fun Phase3ParserView(
    ast: ProgramNode?,
    parseErrors: List<String>,
    parseTimeUs: Long,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Tree View, 1: Canvas Graph, 2: JSON AST

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("phase3_parser_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Metrics Bar with ALVI Badge and Workable Copy Button
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
                                text = "Phase 3: AST Parser",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val jsonStr = ast?.toJsonString() ?: "{}"
                                val formatted = try {
                                    org.json.JSONObject(jsonStr).toString(2)
                                } catch (e: Exception) {
                                    jsonStr
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ast-json", formatted))
                                Toast.makeText(context, "AST JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy AST JSON", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Root Statements", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                            Text(
                                text = "${ast?.statements?.size ?: 0}",
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
                            Text("Parse Duration", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                            Text(
                                text = "$parseTimeUs µs",
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
                            Text("Syntax Errors", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                            Text(
                                text = "${parseErrors.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (parseErrors.isEmpty()) EmeraldSuccess else RoseError
                            )
                        }
                    }
                }
            }
        }

        // Errors alert
        if (parseErrors.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseError)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parser Diagnostic Errors", fontWeight = FontWeight.Bold, color = RoseError)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        parseErrors.forEach { err ->
                            Text(err, fontFamily = JetBrainsMono, fontSize = 12.sp, color = TextPrimaryDark)
                        }
                    }
                }
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CyanPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Hierarchical Tree") },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Visual Node Graph") },
                    icon = { Icon(Icons.Default.BubbleChart, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("JSON AST") },
                    icon = { Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Tab Content
        item {
            when (selectedTab) {
                0 -> HierarchicalAstView(ast)
                1 -> CanvasAstGraphView(ast)
                2 -> JsonAstView(ast)
            }
        }
    }
}

@Composable
private fun HierarchicalAstView(ast: ProgramNode?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "ProgramNode (Root)",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary,
                fontSize = 15.sp
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            if (ast == null || ast.statements.isEmpty()) {
                Text("No statements parsed in AST", color = TextMutedDark, fontFamily = JetBrainsMono)
            } else {
                ast.statements.forEachIndexed { index, stmt ->
                    AstNodeTreeItem(stmt, depth = 1, isLast = index == ast.statements.size - 1)
                }
            }
        }
    }
}

@Composable
private fun AstNodeTreeItem(node: AstNode, depth: Int, isLast: Boolean) {
    val indent = "    ".repeat(depth)
    val branch = if (isLast) "└── " else "├── "

    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = indent + branch,
                fontFamily = JetBrainsMono,
                color = TextMutedDark,
                fontSize = 12.sp
            )

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = getAstNodeColor(node).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, getAstNodeColor(node).copy(alpha = 0.5f))
            ) {
                Text(
                    text = node.nodeType,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = getAstNodeColor(node)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            val details = when (node) {
                is FunctionDefNode -> "fn ${node.name}(${node.parameters.size} params): ${node.returnType}"
                is VariableDeclNode -> "${if (node.isMutable) "mut " else ""}${node.name}: ${node.type ?: "auto"}"
                is AssignmentNode -> "${node.target} = ..."
                is BinaryOpNode -> "operator '${node.operator}'"
                is PipeNode -> "pipe operator '|>'"
                is CallNode -> "call ${node.callee}(${node.arguments.size} args)"
                is PrintNode -> "print(...)"
                is IfNode -> "if (cond)"
                is WhileNode -> "while (cond)"
                is ReturnNode -> "return"
                is RegisterOpNode -> "${node.opcode} ${node.destReg}"
                is LiteralNode -> "value = ${node.value}"
                is IdentifierNode -> "id = ${node.name}"
                is StructDefNode -> "struct ${node.name}"
                else -> ""
            }

            Text(
                text = details,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Render sub-children if applicable
        when (node) {
            is FunctionDefNode -> {
                AstNodeTreeItem(node.body, depth + 1, isLast = true)
            }
            is BlockNode -> {
                node.statements.forEachIndexed { i, s ->
                    AstNodeTreeItem(s, depth + 1, isLast = i == node.statements.size - 1)
                }
            }
            is IfNode -> {
                AstNodeTreeItem(node.thenBranch, depth + 1, isLast = node.elseBranch == null)
                node.elseBranch?.let { AstNodeTreeItem(it, depth + 1, isLast = true) }
            }
            is WhileNode -> {
                AstNodeTreeItem(node.body, depth + 1, isLast = true)
            }
            else -> {}
        }
    }
}

@Composable
private fun CanvasAstGraphView(ast: ProgramNode?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanDark),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val stmts = ast?.statements ?: emptyList()
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rootX = size.width / 2
            val rootY = 40f
            val nodeRadius = 24f

            // Draw root node
            drawCircle(color = CyanDark, radius = nodeRadius, center = Offset(rootX, rootY))
            drawCircle(color = CyanPrimary, radius = nodeRadius, center = Offset(rootX, rootY), style = Stroke(width = 2f))

            if (stmts.isNotEmpty()) {
                val stepX = size.width / (stmts.size + 1)
                stmts.forEachIndexed { i, stmt ->
                    val childX = stepX * (i + 1)
                    val childY = 160f

                    // Connecting branch line
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(rootX, rootY + nodeRadius),
                        end = Offset(childX, childY - nodeRadius),
                        strokeWidth = 2f
                    )

                    // Child bubble
                    val color = getAstNodeColor(stmt)
                    drawCircle(color = color.copy(alpha = 0.3f), radius = nodeRadius, center = Offset(childX, childY))
                    drawCircle(color = color, radius = nodeRadius, center = Offset(childX, childY), style = Stroke(width = 2f))
                }
            }
        }
    }
}

@Composable
private fun JsonAstView(ast: ProgramNode?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        val scrollState = rememberScrollState()
        val jsonStr = ast?.toJsonString() ?: "{}"
        // Formatted indented JSON
        val formattedJson = try {
            org.json.JSONObject(jsonStr).toString(2)
        } catch (e: Exception) {
            jsonStr
        }

        Box(
            modifier = Modifier
                .padding(14.dp)
                .horizontalScroll(scrollState)
        ) {
            Text(
                text = formattedJson,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = EmeraldSuccess
            )
        }
    }
}

private fun getAstNodeColor(node: AstNode): Color {
    return when (node) {
        is FunctionDefNode -> SyntaxFunction
        is VariableDeclNode, is AssignmentNode -> CyanPrimary
        is BinaryOpNode, is UnaryOpNode -> SyntaxOperator
        is PipeNode -> VioletSyntax
        is IfNode, is WhileNode -> AmberAccent
        is PrintNode -> EmeraldSuccess
        is ReturnNode -> RoseError
        is RegisterOpNode -> SyntaxRegister
        is StructDefNode -> SyntaxType
        is LiteralNode -> SyntaxNumber
        is IdentifierNode -> SyntaxFunction
        else -> TextSecondaryDark
    }
}
