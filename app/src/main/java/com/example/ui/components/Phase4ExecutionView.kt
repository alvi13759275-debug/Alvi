package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.compiler.binary.BinaryAnalysisReport
import com.example.compiler.interpreter.InterpreterResult
import com.example.compiler.model.Instruction
import com.example.compiler.model.VmStepState
import com.example.compiler.transpiler.TranspileTarget
import com.example.ui.theme.*

@Composable
fun Phase4ExecutionView(
    interpreterResult: InterpreterResult?,
    bytecodeInstructions: List<Instruction>,
    vmState: VmStepState?,
    activeTarget: TranspileTarget,
    transpiledCode: String,
    binaryReport: BinaryAnalysisReport?,
    onStepVm: () -> Unit,
    onResetVm: () -> Unit,
    onSelectTarget: (TranspileTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    var execTab by remember { mutableStateOf(0) } // 0: Live Interpreter, 1: Bytecode VM, 2: Transpiler, 3: Binary Hex
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("phase4_execution_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector
        item {
            ScrollableTabRow(
                selectedTabIndex = execTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CyanPrimary,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = execTab == 0,
                    onClick = { execTab = 0 },
                    text = { Text("AST Interpreter") },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = execTab == 1,
                    onClick = { execTab = 1 },
                    text = { Text("Bytecode VM") },
                    icon = { Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = execTab == 2,
                    onClick = { execTab = 2 },
                    text = { Text("Multi Transpiler") },
                    icon = { Icon(Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = execTab == 3,
                    onClick = { execTab = 3 },
                    text = { Text("Binary Hex") },
                    icon = { Icon(Icons.Default.Hexagon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Sub-Tab 0: Live AST Interpreter
        if (execTab == 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
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
                                Text("AST Runtime Terminal", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.2f", interpreterResult?.executionTimeMs ?: 0.0)} ms",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    color = TextMutedDark
                                )

                                IconButton(
                                    onClick = {
                                        val outText = interpreterResult?.output?.joinToString("\n") ?: ""
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("terminal-output", outText))
                                        Toast.makeText(context, "Terminal output copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Output", tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        val output = interpreterResult?.output ?: emptyList()
                        if (output.isEmpty()) {
                            Text("> (No output printed)", fontFamily = JetBrainsMono, color = TextMutedDark, fontSize = 13.sp)
                        } else {
                            output.forEach { line ->
                                Text("> $line", fontFamily = JetBrainsMono, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        interpreterResult?.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Runtime Error: $err", fontFamily = JetBrainsMono, color = RoseError, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Environment Scope Variables
            item {
                Text(
                    text = "ACTIVE ENVIRONMENT SCOPE (VARIABLES)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val vars = interpreterResult?.variables ?: emptyMap()
                    if (vars.isEmpty()) {
                        Text(
                            text = "No variables in scope",
                            modifier = Modifier.padding(14.dp),
                            color = TextMutedDark,
                            fontFamily = JetBrainsMono
                        )
                    } else {
                        Column(modifier = Modifier.padding(14.dp)) {
                            vars.forEach { (name, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(name, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                    Text(
                                        value?.toString() ?: "null",
                                        fontFamily = JetBrainsMono,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sub-Tab 1: Bytecode Virtual Machine & Stepper
        if (execTab == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("VM Controls", fontWeight = FontWeight.Bold, color = CyanPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onResetVm,
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset")
                                }
                                Button(
                                    onClick = onStepVm,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    enabled = vmState?.isHalted != true
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkBackground)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Step", color = DarkBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Program Counter (PC): 0x${String.format("%04X", vmState?.programCounter ?: 0)}", fontFamily = JetBrainsMono, fontSize = 12.sp, color = AmberAccent)
                            Text(if (vmState?.isHalted == true) "STATUS: HALTED" else "STATUS: RUNNING", fontFamily = JetBrainsMono, fontSize = 12.sp, color = if (vmState?.isHalted == true) RoseError else EmeraldSuccess)
                        }
                    }
                }
            }

            // Virtual Register Bank (v0..v7)
            item {
                Text("VIRTUAL REGISTERS (v0..v7)", style = MaterialTheme.typography.labelSmall, color = SyntaxRegister, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val regs = vmState?.registers ?: emptyMap()
                    items(regs.toList()) { (reg, value) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SyntaxRegister.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(reg, fontFamily = JetBrainsMono, color = SyntaxRegister, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$value", fontFamily = JetBrainsMono, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Operand Stack
            item {
                Text("OPERAND STACK", style = MaterialTheme.typography.labelSmall, color = CyanPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val stack = vmState?.operandStack ?: emptyList()
                if (stack.isEmpty()) {
                    Text("[Stack is empty]", fontFamily = JetBrainsMono, color = TextMutedDark, fontSize = 12.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(stack.reversed()) { item ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyanPrimary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                            ) {
                                Text(
                                    text = "$item",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontFamily = JetBrainsMono,
                                    color = CyanPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bytecode Disassembly Instructions List
            item {
                Text("DISASSEMBLED INSTRUCTIONS (${bytecodeInstructions.size})", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }

            items(bytecodeInstructions) { instr ->
                val isCurrent = instr.address == vmState?.programCounter
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) CyanPrimary.copy(alpha = 0.15f) else DarkBackground
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) CyanPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = instr.toString(),
                            fontFamily = JetBrainsMono,
                            color = if (isCurrent) CyanPrimary else TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isCurrent) {
                            Text("◀ CURRENT PC", fontFamily = JetBrainsMono, fontSize = 10.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sub-Tab 2: Multi-Target Transpiler
        if (execTab == 2) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TranspileTarget.values()) { target ->
                        FilterChip(
                            selected = activeTarget == target,
                            onClick = { onSelectTarget(target) },
                            label = { Text("${target.displayName} (${target.extension})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
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
                                text = "Output: ${activeTarget.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("transpiled", transpiledCode))
                                Toast.makeText(context, "Copied ${activeTarget.displayName} code", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = CyanPrimary)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        val scrollState = rememberScrollState()
                        Box(modifier = Modifier.horizontalScroll(scrollState)) {
                            Text(
                                text = transpiledCode,
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }
        }

        // Sub-Tab 3: Binary Hex & Dalvik/ART Inspector
        if (execTab == 3) {
            item {
                binaryReport?.let { report ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(report.formatName, fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Magic: ${report.magicBytesHex}", fontFamily = JetBrainsMono, fontSize = 12.sp, color = AmberAccent)
                                }

                                Button(
                                    onClick = {
                                        val sb = StringBuilder()
                                        sb.append("DEX Binary Hex Dump (Architect: ALVI)\n")
                                        report.hexDump.forEach { r ->
                                            sb.append("${r.offsetHex}: ${r.hexBytes.joinToString(" ")} |${r.ascii}|\n")
                                        }
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("dex-hex", sb.toString()))
                                        Toast.makeText(context, "DEX Hex dump copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Hex", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Total Size: ${report.totalBytes} bytes • Alignment: 16-byte", fontFamily = JetBrainsMono, fontSize = 12.sp, color = TextMutedDark)
                        }
                    }
                }
            }

            item {
                Text("HEX DUMP (16-BYTE ALIGNED)", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }

            binaryReport?.hexDump?.let { rows ->
                items(rows) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = row.offsetHex,
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )
                        Text(
                            text = row.hexBytes.joinToString(" "),
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = when (row.section) {
                                "Header" -> CyanPrimary
                                "Strings" -> AmberAccent
                                else -> EmeraldSuccess
                            }
                        )
                        Text(
                            text = "|${row.ascii}|",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        }
    }
}
