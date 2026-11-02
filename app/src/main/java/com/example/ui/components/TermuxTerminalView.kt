package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termux.CommandOutput
import com.example.termux.TermuxCliEngine
import com.example.ui.theme.*

@Composable
fun TermuxTerminalView(
    sourceCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cliEngine = remember { TermuxCliEngine() }

    LaunchedEffect(sourceCode) {
        cliEngine.setSourceCode(sourceCode)
    }

    val history = remember {
        mutableStateListOf(
            CommandOutput(
                command = "boot",
                text = """
============================================================
 TERMUX COMPILER TOOLCHAIN (ANDROID AARCH64)
 CORE ARCHITECT & CREATOR: ALVI
 OMNILANG ECOSYSTEM READY // TYPE 'help' OR USE SHORTCUTS
============================================================
"""
            )
        )
    }

    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    val runCommand: (String) -> Unit = { cmdText ->
        val trimmed = cmdText.trim()
        if (trimmed == "clear") {
            history.clear()
        } else if (trimmed.isNotEmpty()) {
            val output = cliEngine.execute(trimmed)
            history.add(output)
        }
        inputCommand = ""
    }

    val quickCommands = listOf(
        "omnic run",
        "omnic build",
        "omnic lex",
        "omnic ast",
        "omnic hex",
        "omnic transpile --to=kt",
        "omnic termux-install",
        "whoami",
        "clear"
    )

    val termuxKeys = listOf("TAB", "CTRL", "ALT", "ESC", "|", "-", ">", ";", "/", ":", "omnic", "clear")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .testTag("termux_terminal_view")
    ) {
        // Termux Top HUD Bar
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "TERMUX CLI TOOL // ALVI LABS",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                        Text(
                            text = "CREATOR: ALVI • aarch64 • ART/DEX v039",
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            color = EmeraldSuccess
                        )
                    }
                }

                Button(
                    onClick = {
                        val installScript = cliEngine.execute("omnic termux-install").text
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("termux-script", installScript))
                        Toast.makeText(context, "Termux setup script copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Script", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Quick Command Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0F1D))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickCommands) { qc ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clickable { runCommand(qc) }
                        .testTag("quick_cmd_${qc.replace(" ", "_")}")
                ) {
                    Text(
                        text = qc,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = CyanPrimaryLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Terminal Output Console
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(history) { item ->
                Column {
                    if (item.command.isNotEmpty() && item.command != "boot") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "alvi@omnilang:~$ ",
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = item.command,
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = item.text,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = if (item.isError) RoseError else Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Termux Virtual Special Keys Toolbar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(termuxKeys) { key ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable {
                        inputCommand += if (key == "TAB" || key == "CTRL" || key == "ALT" || key == "ESC") " " else key
                    }
                ) {
                    Text(
                        text = key,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Terminal Prompt Input Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050811))
                .border(1.dp, Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "alvi@termux:~$ ",
                fontFamily = JetBrainsMono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldSuccess
            )

            TextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                placeholder = { Text("omnic run / help...", color = TextMutedDark, fontFamily = JetBrainsMono, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { runCommand(inputCommand) }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontFamily = JetBrainsMono, fontSize = 13.sp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("termux_input_field")
            )

            IconButton(
                onClick = { runCommand(inputCommand) },
                enabled = inputCommand.isNotBlank(),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Execute", tint = CyanPrimary)
            }
        }
    }
}
