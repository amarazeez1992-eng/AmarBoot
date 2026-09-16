package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.agent.file.AmarFileCodeIntelligence
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.chart.AmarTimeframe

/** Deterministic evidence tools exposed to the AI agent. No broker writes are performed here. */
object AmarAiDeterministicToolGateway {
    suspend fun execute(tool: String, args: String): String? {
        val spec = AmarAiToolRegistry.resolve(tool) ?: return "TOOL_REJECTED|unknown_tool=$tool"
        if (spec.authority != AmarAiToolRegistry.Authority.READ_ONLY) {
            return "TOOL_REJECTED|authority=${spec.authority}|tool=${spec.name}"
        }
        return when (tool) {
            "inspect_app" -> "APP_INSPECTION|${AmarAiAppInspector.describe()}"
            "engine_market" -> AmarAiEngineBinding.market()
            "tracking" -> AmarAiEngineBinding.tracking(args.trim().ifBlank { null })
            "candle" -> {
                val parts = args.split(',', ';', '|').map { it.trim() }.filter { it.isNotBlank() }
                val symbol = parts.firstOrNull() ?: AmarMarketStateStore.snapshot.symbol
                val requestedTf = parts.drop(1).firstOrNull()
                val timeframe = requestedTf?.let { value ->
                    AmarTimeframe.entries.firstOrNull { it.name.equals(value, true) || it.shortLabel.equals(value, true) }
                } ?: AmarMarketStateStore.snapshot.timeframe
                AmarAiEngineBinding.candle(symbol, timeframe)
            }
            "file_analyze", "code_analyze" -> {
                val input = parseFileInput(args) ?: return "TOOL_REJECTED|invalid_file_context"
                val analysis = AmarFileCodeIntelligence.analyze(input)
                "FILE_ANALYSIS|path=${analysis.path}|language=${analysis.language}|lines=${analysis.lineCount}|sha256=${analysis.sha256}|findings=${analysis.findings.size}|symbols=${analysis.symbols.size}|dependencies=${analysis.dependencies.size}"
            }
            "file_compare" -> {
                val separator = "\n---AMAR-BEFORE-AFTER---\n"
                val pieces = args.split(separator, limit = 2)
                if (pieces.size != 2) return "TOOL_REJECTED|invalid_file_compare_context"
                val before = parseFileInput(pieces[0]) ?: return "TOOL_REJECTED|invalid_before_context"
                val after = parseFileInput(pieces[1]) ?: return "TOOL_REJECTED|invalid_after_context"
                "FILE_COMPARE|changes=${AmarFileCodeIntelligence.compare(before, after).size}"
            }
            else -> null
        }
    }

    private fun parseFileInput(args: String): AmarFileCodeIntelligence.FileInput? {
        val marker = "\n---AMAR-CONTENT---\n"
        val split = args.split(marker, limit = 2)
        if (split.size != 2) return null
        val path = split[0].removePrefix("path=").trim()
        if (path.isBlank()) return null
        return AmarFileCodeIntelligence.FileInput(path, split[1])
    }
}
