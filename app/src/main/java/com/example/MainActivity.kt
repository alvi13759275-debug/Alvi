package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compiler.model.LanguageProfile
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.CompilerViewModel
import com.example.viewmodel.StudioPhase

enum class NavigationTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PHASES("Pipeline", Icons.Default.Layers),
    EDITOR("Editor", Icons.Default.Code),
    TERMUX("Termux", Icons.Default.Terminal),
    KNOWLEDGE("700+ Formats", Icons.Default.MenuBook),
    ARCHITECT("AI Architect", Icons.Default.AutoAwesome)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                OmniLangApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniLangApp(viewModel: CompilerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentNavTab by remember { mutableStateOf(NavigationTab.PHASES) }
    var showProfileDropdown by remember { mutableStateOf(false) }
    var showCreatorModal by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showCreatorModal) {
        CreatorArchitectModal(
            onDismiss = { showCreatorModal = false },
            onOpenTermux = {
                showCreatorModal = false
                currentNavTab = NavigationTab.TERMUX
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("omnilang_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_super_compiler_icon_1793640584973),
                                contentDescription = "OmniLang Super Emblem",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, CyanPrimary, RoundedCornerShape(8.dp))
                            )

                            Column {
                                Text(
                                    text = "OmniLang",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanPrimary,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "BY ALVI",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    fontSize = 9.sp,
                                    fontFamily = JetBrainsMono
                                )
                            }

                            // Active Profile Selector Dropdown
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { showProfileDropdown = true }
                                        .testTag("profile_badge_selector")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = uiState.activeProfile.fileExtension,
                                            fontFamily = JetBrainsMono,
                                            fontSize = 11.sp,
                                            color = CyanPrimaryLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Change Language",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showProfileDropdown,
                                    onDismissRequest = { showProfileDropdown = false }
                                ) {
                                    LanguageProfile.Presets.forEach { preset ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(preset.name, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        "${preset.fileExtension} • ${preset.paradigm.displayName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextMutedDark
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectProfile(preset)
                                                showProfileDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        // Workable Creator ALVI HUD Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { showCreatorModal = true }
                                .testTag("creator_alvi_badge_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "ALVI",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldSuccess
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.runFullPipeline() },
                            modifier = Modifier.testTag("top_recompile_action")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recompile Pipeline", tint = CyanPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground,
                        titleContentColor = CyanPrimary
                    )
                )

                // Hi-Tech Cyberpunk Telemetry Status Bar
                Surface(
                    color = Color(0xFF080D1A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Text(
                                text = "SYS: QUANTUM-ACTIVE",
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "•",
                                color = TextMutedDark,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "ARCHITECT: ALVI",
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showCreatorModal = true }
                            )
                        }

                        // Workable Quick Termux Tool Switcher Button
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { currentNavTab = NavigationTab.TERMUX }
                                .testTag("telemetry_termux_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "TERMUX CLI",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.sp,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentNavTab == tab,
                        onClick = { currentNavTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pipeline Phase Selector Header when in PHASES tab
            if (currentNavTab == NavigationTab.PHASES) {
                ScrollableTabRow(
                    selectedTabIndex = uiState.currentPhase.ordinal,
                    containerColor = DarkSurfaceVariant,
                    contentColor = CyanPrimary,
                    edgePadding = 8.dp
                ) {
                    StudioPhase.values().forEach { phase ->
                        Tab(
                            selected = uiState.currentPhase == phase,
                            onClick = { viewModel.setPhase(phase) },
                            text = {
                                Text(
                                    phase.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (uiState.currentPhase == phase) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Screen Switcher
            when (currentNavTab) {
                NavigationTab.PHASES -> {
                    when (uiState.currentPhase) {
                        StudioPhase.PHASE_1_GRAMMAR -> {
                            Phase1GrammarView(
                                profile = uiState.activeProfile,
                                onAskArchitect = { prompt ->
                                    currentNavTab = NavigationTab.ARCHITECT
                                    viewModel.sendAiPrompt(prompt)
                                }
                            )
                        }
                        StudioPhase.PHASE_2_LEXER -> {
                            Phase2LexerView(
                                tokens = uiState.tokens,
                                tokenTimeUs = uiState.tokenTimeUs,
                                diagnostics = uiState.lexerDiagnostics
                            )
                        }
                        StudioPhase.PHASE_3_PARSER -> {
                            Phase3ParserView(
                                ast = uiState.ast,
                                parseErrors = uiState.parseErrors,
                                parseTimeUs = uiState.parseTimeUs
                            )
                        }
                        StudioPhase.PHASE_4_EXECUTION -> {
                            Phase4ExecutionView(
                                interpreterResult = uiState.interpreterResult,
                                bytecodeInstructions = uiState.bytecodeInstructions,
                                vmState = uiState.vmState,
                                activeTarget = uiState.activeTranspileTarget,
                                transpiledCode = uiState.transpiledCode,
                                binaryReport = uiState.binaryReport,
                                onStepVm = { viewModel.stepVm() },
                                onResetVm = { viewModel.resetVm() },
                                onSelectTarget = { target -> viewModel.selectTranspileTarget(target) }
                            )
                        }
                    }
                }

                NavigationTab.EDITOR -> {
                    CodeEditorView(
                        activeProfile = uiState.activeProfile,
                        sourceCode = uiState.sourceCode,
                        onSourceCodeChange = { viewModel.updateSourceCode(it) },
                        onSelectProfile = { viewModel.selectProfile(it) },
                        onNavigatePhase = { phase ->
                            viewModel.setPhase(phase)
                            currentNavTab = NavigationTab.PHASES
                        },
                        onRunPipeline = { viewModel.runFullPipeline() }
                    )
                }

                NavigationTab.TERMUX -> {
                    TermuxTerminalView(sourceCode = uiState.sourceCode)
                }

                NavigationTab.KNOWLEDGE -> {
                    KnowledgeCatalogView(
                        query = uiState.knowledgeQuery,
                        selectedCategory = uiState.selectedCategory,
                        selectedParadigm = uiState.selectedParadigm,
                        items = uiState.catalogResults,
                        selectedDetail = uiState.selectedFormat,
                        onQueryChange = { viewModel.updateKnowledgeQuery(it) },
                        onCategorySelect = { viewModel.filterCategory(it) },
                        onParadigmSelect = { viewModel.filterParadigm(it) },
                        onSelectDetail = { viewModel.selectFormatDetail(it) }
                    )
                }

                NavigationTab.ARCHITECT -> {
                    AiArchitectChatView(
                        messages = uiState.chatMessages,
                        isThinking = uiState.isAiThinking,
                        useHighThinking = uiState.useHighThinking,
                        useGoogleSearch = uiState.useGoogleSearch,
                        onToggleHighThinking = { viewModel.toggleHighThinking(it) },
                        onToggleGoogleSearch = { viewModel.toggleGoogleSearch(it) },
                        onSendMessage = { viewModel.sendAiPrompt(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun CreatorArchitectModal(
    onDismiss: () -> Unit,
    onOpenTermux: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0B1120),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("creator_architect_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Surface(
                    shape = CircleShape,
                    color = CyanPrimary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CREATOR: ALVI",
                    fontFamily = JetBrainsMono,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanPrimary
                )

                Text(
                    text = "Principal Compiler Architect & Binary Analyst",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Specs Container
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D31),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "OMNILANG ECOSYSTEM SPECIFICATIONS",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SpecItem(label = "Creator", value = "ALVI")
                        SpecItem(label = "Paradigm", value = "Multi-Paradigm (Imperative, Functional, OOP, Declarative, Assembly)")
                        SpecItem(label = "Formats & Grammars", value = "700+ Languages, Extensions, & Binary Formats")
                        SpecItem(label = "Termux Toolchain", value = "omnic CLI (Native aarch64 Android Shell Tool)")
                        SpecItem(label = "Lexer Engine", value = "Linear O(N) Streaming DFA Tokenizer")
                        SpecItem(label = "Parser Engine", value = "Pratt Precedence & Recursive Descent AST")
                        SpecItem(label = "Bytecode VM", value = "Dual Stack & Register Bank (v0..v7)")
                        SpecItem(label = "Transpile Targets", value = "Kotlin, TypeScript, JavaScript, Python 3, ANSI C, Smali")
                        SpecItem(label = "Binary Synthesis", value = "Dalvik DEX (dex\\n039) container generation & hex inspector")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Workable Action Buttons
                Button(
                    onClick = onOpenTermux,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_open_termux_btn")
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LAUNCH TERMUX SHELL",
                        color = DarkBackground,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = JetBrainsMono
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val text = """
                            ============================================================
                            OMNILANG COMPILER ECOSYSTEM & TERMUX TOOLCHAIN
                            CREATOR & PRINCIPAL COMPILER ARCHITECT: ALVI
                            - Supports 700+ programming languages, file extensions, and binary specs
                            - Termux CLI: 'omnic' tool suite with run, build, lex, ast, hex, transpile
                            - 4-Phase Compiler Pipeline: Grammar EBNF, O(N) Lexer, Pratt Parser, Dual VM
                            - Transpiles to: Kotlin, TypeScript, JavaScript, Python 3, ANSI C, Dalvik Smali
                            - Binary: DEX container synthesis (magic: 64 65 78 0A) with 16-byte hex dump
                            ============================================================
                        """.trimIndent()
                        clipboard.setPrimaryClip(ClipData.newPlainText("alvi-compiler-specs", text))
                        Toast.makeText(context, "ALVI Architect Specifications copied!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_copy_specs_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COPY ARCHITECT SPECS",
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("modal_close_btn")
                ) {
                    Text("CLOSE", color = TextMutedDark, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            color = TextMutedDark,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE2E8F0),
            modifier = Modifier.weight(1f)
        )
    }
}
