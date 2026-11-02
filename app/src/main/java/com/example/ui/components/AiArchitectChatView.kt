package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChatMessage
import com.example.ui.theme.*

@Composable
fun AiArchitectChatView(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    useHighThinking: Boolean,
    useGoogleSearch: Boolean,
    onToggleHighThinking: (Boolean) -> Unit,
    onToggleGoogleSearch: (Boolean) -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Phase 1: Define EBNF grammar with pipe operators",
        "Phase 2: Explain linear time DFA lexing algorithm",
        "Phase 3: Compare Pratt parsing vs Recursive Descent",
        "Phase 4: Register VM vs Stack VM trade-offs",
        "Binary: Explain Android Dalvik DEX magic bytes and headers"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_architect_chat_view")
    ) {
        // AI Controls & Model Status
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (useHighThinking) "Gemini 4 Argon (High Thinking)" else "Gemini 3.8 Flash (Compiler Intelligence)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (useHighThinking) AmberAccent else CyanPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Thinking", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                        Switch(
                            checked = useHighThinking,
                            onCheckedChange = onToggleHighThinking,
                            modifier = Modifier
                                .height(28.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) CyanPrimary.copy(alpha = 0.2f) else DarkSurface
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isUser) CyanPrimary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = if (isUser) CyanPrimary else AmberAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isUser) "Developer" else "Principal Compiler Architect",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) CyanPrimary else AmberAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.content,
                                fontFamily = if (msg.content.contains("::=") || msg.content.contains("fun ") || msg.content.contains("class ")) JetBrainsMono else InterFont,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = CyanPrimary
                                )
                                Text(
                                    text = if (useHighThinking) "Architect reasoning with high thinking..." else "Architect analyzing...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick prompts suggestions
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                AssistChip(
                    onClick = {
                        inputText = prompt
                    },
                    label = { Text(prompt, fontSize = 11.sp) }
                )
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask about grammar, ASTs, VM, or 700+ formats...") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        onSendMessage(text)
                    }
                },
                enabled = inputText.isNotBlank() && !isThinking,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = CyanPrimary,
                    contentColor = DarkBackground
                ),
                modifier = Modifier.testTag("ai_chat_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
