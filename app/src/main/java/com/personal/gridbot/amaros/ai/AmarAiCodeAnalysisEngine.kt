package com.personal.gridbot.amaros.ai

/**
 * Language-agnostic code-analysis planning layer.
 * It identifies the analysis dimensions; actual parsing/compilation is delegated
 * to language-specific adapters when available, otherwise the agent fails closed.
 */
object AmarAiCodeAnalysisEngine {
    data class CodeAnalysisPlan(
        val language: String,
        val dimensions: List<String>,
        val checks: List<String>,
        val recommendations: List<String>
    )

    private val languageHints = linkedMapOf(
        "kotlin" to listOf(".kt", "fun ", "val ", "var ", "data class"),
        "java" to listOf(".java", "public class", "interface ", "System.out"),
        "html" to listOf("<html", "<!doctype", "<div", "</body>"),
        "css" to listOf("{", "}", "@media", "color:"),
        "javascript/typescript" to listOf(".js", ".ts", "const ", "function ", "=>"),
        "mql5" to listOf(".mq5", "OnTick(", "OnInit(", "#include <Trade/"),
        "pine script" to listOf("//@version=", "strategy(", "indicator(", "plot(") ,
        "c/c++" to listOf("#include <", "int main(", "std::", "namespace ") ,
        "python" to listOf(".py", "def ", "import ", "if __name__"),
        "c#" to listOf(".cs", "using System", "public class", "namespace ")
    )

    fun plan(code: String, fileName: String = ""): CodeAnalysisPlan {
        val source = "$fileName\n$code".lowercase()
        val language = languageHints.entries.firstOrNull { (_, hints) -> hints.any(source::contains) }?.key ?: "unknown"
        return CodeAnalysisPlan(
            language = language,
            dimensions = listOf(
                "purpose_and_behavior", "syntax_and_structure", "logic_and_edge_cases",
                "errors_and_root_causes", "security", "performance", "maintainability",
                "dependencies_and_compatibility", "tests_and_coverage", "missing_requirements",
                "architecture", "research_and_stronger_alternatives"
            ),
            checks = listOf(
                "parse_or_compile_when_tooling_is_available",
                "trace control/data flow",
                "detect defects and explain root cause",
                "identify omissions and risky assumptions",
                "separate confirmed findings from hypotheses",
                "propose minimal fixes and stronger redesign options"
            ),
            recommendations = listOf(
                "preserve source language and identifiers in code patches",
                "show exact reason and impact for every finding",
                "validate proposed changes with tests/builds when possible",
                "fail closed instead of inventing results when source/tooling is insufficient"
            )
        )
    }
}
