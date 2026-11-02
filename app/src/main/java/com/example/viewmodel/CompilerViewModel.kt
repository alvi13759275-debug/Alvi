package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.GeminiCompilerService
import com.example.compiler.binary.BinaryAnalysisReport
import com.example.compiler.binary.BinaryInspector
import com.example.compiler.interpreter.InterpreterResult
import com.example.compiler.interpreter.OmniInterpreter
import com.example.compiler.lexer.OmniLexer
import com.example.compiler.model.*
import com.example.compiler.parser.OmniParser
import com.example.compiler.transpiler.OmniTranspiler
import com.example.compiler.transpiler.TranspileTarget
import com.example.compiler.vm.BytecodeCompiler
import com.example.compiler.vm.VirtualMachine
import com.example.knowledge.FormatCatalog
import com.example.knowledge.FormatCategory
import com.example.knowledge.FormatItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StudioPhase(val number: Int, val title: String, val subtitle: String) {
    PHASE_1_GRAMMAR(1, "Phase 1: Specs", "Syntax Rules, Keywords & AST Grammar"),
    PHASE_2_LEXER(2, "Phase 2: Lexer", "Tokenizer & Visual Token Stream"),
    PHASE_3_PARSER(3, "Phase 3: Parser", "AST Builder & Syntax Tree Visualizer"),
    PHASE_4_EXECUTION(4, "Phase 4: Engine", "Interpreter, VM, Transpiler & Hex")
}

data class CompilerUiState(
    val currentPhase: StudioPhase = StudioPhase.PHASE_1_GRAMMAR,
    val activeProfile: LanguageProfile = LanguageProfile.OmniScript,
    val sourceCode: String = LanguageProfile.OmniScript.sampleCode,

    // Phase 2: Lexer
    val tokens: List<Token> = emptyList(),
    val lexerDiagnostics: List<String> = emptyList(),
    val tokenTimeUs: Long = 0,

    // Phase 3: Parser
    val ast: ProgramNode? = null,
    val parseErrors: List<String> = emptyList(),
    val parseTimeUs: Long = 0,

    // Phase 4: Execution
    val interpreterResult: InterpreterResult? = null,
    val bytecodeInstructions: List<Instruction> = emptyList(),
    val vmState: VmStepState? = null,
    val activeTranspileTarget: TranspileTarget = TranspileTarget.KOTLIN,
    val transpiledCode: String = "",
    val binaryReport: BinaryAnalysisReport? = null,

    // Knowledge Catalog
    val knowledgeQuery: String = "",
    val selectedCategory: FormatCategory? = null,
    val selectedParadigm: LanguageParadigm? = null,
    val catalogResults: List<FormatItem> = FormatCatalog.items,
    val selectedFormat: FormatItem? = null,

    // AI Chat
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = "architect",
            content = "Greetings. I am your Principal Compiler Architect & Binary Analyst.\nI have initialized Phase 1: Syntax Rules, Keywords, and AST Grammar Specs for your multi-paradigm language.\n\nUse the tabs below or ask me anything regarding grammar design, tokenization DFAs, Pratt parsing, or register VM allocation!"
        )
    ),
    val isAiThinking: Boolean = false,
    val useHighThinking: Boolean = false,
    val useGoogleSearch: Boolean = false
)

class CompilerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CompilerUiState())
    val uiState: StateFlow<CompilerUiState> = _uiState.asStateFlow()

    private val lexerCache = mutableMapOf<String, List<Token>>()
    private val bytecodeCompiler = BytecodeCompiler()
    private val transpiler = OmniTranspiler()
    private var activeVm: VirtualMachine? = null

    init {
        // Run initial compilation cycle
        runFullPipeline()
    }

    fun setPhase(phase: StudioPhase) {
        _uiState.value = _uiState.value.copy(currentPhase = phase)
    }

    fun updateSourceCode(newCode: String) {
        _uiState.value = _uiState.value.copy(sourceCode = newCode)
        runFullPipeline()
    }

    fun selectProfile(profile: LanguageProfile) {
        _uiState.value = _uiState.value.copy(
            activeProfile = profile,
            sourceCode = profile.sampleCode
        )
        runFullPipeline()
    }

    fun runFullPipeline() {
        val code = _uiState.value.sourceCode

        // Phase 2: Lexer
        val lexStart = System.nanoTime()
        val lexer = OmniLexer(code)
        val tokens = lexer.tokenize()
        val lexTimeUs = (System.nanoTime() - lexStart) / 1_000

        // Phase 3: Parser
        val parseStart = System.nanoTime()
        val parser = OmniParser(tokens)
        val ast = parser.parse()
        val parseTimeUs = (System.nanoTime() - parseStart) / 1_000

        // Phase 4: Execution - Interpreter
        val interpreter = OmniInterpreter()
        val interpResult = interpreter.execute(ast)

        // Phase 4: Execution - Bytecode VM
        val instructions = bytecodeCompiler.compile(ast)
        val vm = VirtualMachine(instructions)
        activeVm = vm
        vm.runAll()
        val vmState = vm.getCurrentState()

        // Phase 4: Execution - Transpiler
        val currentTarget = _uiState.value.activeTranspileTarget
        val transpiled = transpiler.transpile(ast, currentTarget)

        // Phase 4: Execution - Binary Report
        val binaryReport = BinaryInspector.analyzeDex(instructions)

        _uiState.value = _uiState.value.copy(
            tokens = tokens,
            lexerDiagnostics = lexer.diagnostics,
            tokenTimeUs = lexTimeUs,
            ast = ast,
            parseErrors = parser.errors,
            parseTimeUs = parseTimeUs,
            interpreterResult = interpResult,
            bytecodeInstructions = instructions,
            vmState = vmState,
            transpiledCode = transpiled,
            binaryReport = binaryReport
        )
    }

    fun stepVm() {
        activeVm?.let { vm ->
            val newState = vm.step()
            _uiState.value = _uiState.value.copy(vmState = newState)
        }
    }

    fun resetVm() {
        val instructions = _uiState.value.bytecodeInstructions
        val vm = VirtualMachine(instructions)
        activeVm = vm
        _uiState.value = _uiState.value.copy(vmState = vm.getCurrentState())
    }

    fun selectTranspileTarget(target: TranspileTarget) {
        val ast = _uiState.value.ast ?: return
        val code = transpiler.transpile(ast, target)
        _uiState.value = _uiState.value.copy(
            activeTranspileTarget = target,
            transpiledCode = code
        )
    }

    // Knowledge Catalog
    fun updateKnowledgeQuery(query: String) {
        val results = FormatCatalog.search(query, _uiState.value.selectedCategory, _uiState.value.selectedParadigm)
        _uiState.value = _uiState.value.copy(knowledgeQuery = query, catalogResults = results)
    }

    fun filterCategory(category: FormatCategory?) {
        val results = FormatCatalog.search(_uiState.value.knowledgeQuery, category, _uiState.value.selectedParadigm)
        _uiState.value = _uiState.value.copy(selectedCategory = category, catalogResults = results)
    }

    fun filterParadigm(paradigm: LanguageParadigm?) {
        val results = FormatCatalog.search(_uiState.value.knowledgeQuery, _uiState.value.selectedCategory, paradigm)
        _uiState.value = _uiState.value.copy(selectedParadigm = paradigm, catalogResults = results)
    }

    fun selectFormatDetail(format: FormatItem?) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    // AI Chat
    fun toggleHighThinking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(useHighThinking = enabled)
    }

    fun toggleGoogleSearch(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(useGoogleSearch = enabled)
    }

    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return

        val userMsg = ChatMessage(sender = "user", content = prompt)
        val updatedMessages = _uiState.value.chatMessages + userMsg

        _uiState.value = _uiState.value.copy(
            chatMessages = updatedMessages,
            isAiThinking = true
        )

        viewModelScope.launch {
            val response = GeminiCompilerService.askArchitect(
                prompt = prompt,
                history = updatedMessages,
                useThinking = _uiState.value.useHighThinking,
                useSearch = _uiState.value.useGoogleSearch
            )

            val architectMsg = ChatMessage(sender = "architect", content = response)
            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + architectMsg,
                isAiThinking = false
            )
        }
    }
}
