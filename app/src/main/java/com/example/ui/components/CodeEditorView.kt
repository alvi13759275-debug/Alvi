package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.model.LanguageProfile
import com.example.ui.theme.*
import com.example.viewmodel.StudioPhase

@Composable
fun CodeEditorView(
    activeProfile: LanguageProfile,
    sourceCode: String,
    onSourceCodeChange: (String) -> Unit,
    onSelectProfile: (LanguageProfile) -> Unit,
    onNavigatePhase: (StudioPhase) -> Unit,
    onRunPipeline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSnippetsMenu by remember { mutableStateOf(false) }

    val snippets = listOf(
        "Factorial" to """// Recursive Factorial Calculation
fn factorial(n: Int): Int {
  if (n <= 1) {
    return 1;
  }
  return n * factorial(n - 1);
}

let res = factorial(5);
print(res);
""",
        "Fibonacci" to """// Iterative Fibonacci Series
let mut a = 0;
let mut b = 1;
let mut step = 0;

while (step < 7) {
  print(a);
  let temp = a + b;
  a = b;
  b = temp;
  step = step + 1;
}
""",
        "Pipe Pipeline" to """// High-Tech Functional Pipeline
fn triple(v: Int): Int {
  return v * 3;
}

fn addHundred(v: Int): Int {
  return v + 100;
}

let result = 10 |> triple |> addHundred;
print(result);
""",
        "Dalvik Regs" to """.method runDalvik(registers 5)
  const v0, 50
  const v1, 25
  add-int v2, v0, v1
  print v2
  const v3, 4
  mul-int v4, v2, v3
  print v4
  return v4
.end
"""
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("code_editor_view")
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hi-Tech HUD Telemetry Bar
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(EmeraldSuccess, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ARCHITECT: ALVI // OMNILANG v2.4",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CyanPrimary
                    )
                }

                Text(
                    text = "${activeProfile.paradigm.displayName.take(12).uppercase()} // DEX-ART",
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    color = AmberAccent
                )
            }
        }

        // Preset selector chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(LanguageProfile.Presets) { preset ->
                FilterChip(
                    selected = activeProfile.id == preset.id,
                    onClick = { onSelectProfile(preset) },
                    label = { Text("${preset.name} (${preset.fileExtension})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyanPrimary
                    )
                )
            }
        }

        // Active File Bar with Workable Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "main${activeProfile.fileExtension}",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            }

            // Workable Toolbar Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Snippet Loader Button
                Box {
                    OutlinedButton(
                        onClick = { showSnippetsMenu = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.SnippetFolder, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Snippets", fontSize = 11.sp, color = AmberAccent)
                    }

                    DropdownMenu(
                        expanded = showSnippetsMenu,
                        onDismissRequest = { showSnippetsMenu = false }
                    ) {
                        snippets.forEach { (name, code) ->
                            DropdownMenuItem(
                                text = { Text(name, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    onSourceCodeChange(code)
                                    showSnippetsMenu = false
                                    Toast.makeText(context, "Loaded $name snippet", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // Copy Button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("omnilang-source", sourceCode))
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Source", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                }

                // Clear Button
                IconButton(
                    onClick = {
                        onSourceCodeChange("// Enter custom code here\n")
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Editor", tint = RoseError, modifier = Modifier.size(16.dp))
                }

                // Run / Recompile Button
                Button(
                    onClick = {
                        onRunPipeline()
                        Toast.makeText(context, "Compiling pipeline across 4 phases...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("run_pipeline_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compile", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Code Editor Box with Line Numbers
        val lineCount = remember(sourceCode) {
            sourceCode.count { it == '\n' } + 1
        }
        val lineNumbers = remember(lineCount) {
            (1..lineCount).joinToString("\n")
        }

        Surface(
            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Line numbers gutter
                Text(
                    text = lineNumbers,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    color = TextMutedDark,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Editable code text field
                BasicTextField(
                    value = sourceCode,
                    onValueChange = onSourceCodeChange,
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(CyanPrimary),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp)
                        .testTag("source_code_input")
                )
            }
        }

        // Pipeline Phase Quick Navigation Buttons
        Text("DIRECT WORKBENCH PHASES", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { onNavigatePhase(StudioPhase.PHASE_1_GRAMMAR) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text("P1: Specs", fontSize = 10.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onNavigatePhase(StudioPhase.PHASE_2_LEXER) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text("P2: Lexer", fontSize = 10.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onNavigatePhase(StudioPhase.PHASE_3_PARSER) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text("P3: AST", fontSize = 10.sp, color = VioletSyntax, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onNavigatePhase(StudioPhase.PHASE_4_EXECUTION) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(2.dp)
            ) {
                Text("P4: VM/Hex", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            }
        }
    }
}
