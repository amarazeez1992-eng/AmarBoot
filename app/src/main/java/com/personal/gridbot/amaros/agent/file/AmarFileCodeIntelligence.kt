package com.personal.gridbot.amaros.agent.file

import java.security.MessageDigest

/** Stage 2: deterministic file/code intelligence. No writes are performed by this engine. */
object AmarFileCodeIntelligence {
    enum class Language { KOTLIN, JAVA, PYTHON, JAVASCRIPT, TYPESCRIPT, HTML, CSS, C, CPP, CSHARP, MQL5, PINE, MARKDOWN, JSON, CSV, TEXT, UNKNOWN }
    enum class Confidence { CONFIRMED, POSSIBLE, UNKNOWN }

    data class FileInput(val path: String, val content: String, val versionTag: String? = null)
    data class Finding(val category: String, val message: String, val line: Int? = null, val confidence: Confidence = Confidence.CONFIRMED)
    data class Analysis(
        val path: String,
        val language: Language,
        val lineCount: Int,
        val sha256: String,
        val findings: List<Finding>,
        val symbols: List<String>,
        val dependencies: List<String>,
        val sections: List<String>
    )

    fun analyze(input: FileInput): Analysis {
        require(input.path.isNotBlank()) { "path is required" }
        val language = detectLanguage(input.path, input.content)
        val lines = input.content.lines()
        val findings = mutableListOf<Finding>()
        val symbols = mutableListOf<String>()
        val dependencies = mutableListOf<String>()
        val sections = mutableListOf<String>()

        lines.forEachIndexed { index, raw ->
            val line = raw.trim()
            if (line.startsWith("#") || line.startsWith("##") || line.startsWith("fun ") || line.startsWith("class ") || line.startsWith("object ") || line.startsWith("interface ") || line.startsWith("def ") || line.startsWith("function ")) {
                if (line.isNotEmpty()) symbols += line.take(160)
            }
            if (line.startsWith("#") && (language == Language.MARKDOWN || language == Language.PYTHON)) sections += line.take(160)
            dependencyToken(line)?.let { dependencies += it }
            if (line.contains("TODO", ignoreCase = true) || line.contains("FIXME", ignoreCase = true)) {
                findings += Finding("maintenance", "Unresolved TODO/FIXME marker", index + 1, Confidence.CONFIRMED)
            }
            if (line.contains("TODO", true) || line.contains("FIXME", true)) return@forEachIndexed
            if (looksLikeHardcodedSecret(line)) {
                findings += Finding("security", "Possible hardcoded secret/token; manual verification required", index + 1, Confidence.POSSIBLE)
            }
        }

        if (language == Language.JSON && input.content.isNotBlank()) {
            val trimmed = input.content.trim()
            if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) findings += Finding("syntax", "JSON does not start with an object or array", 1)
        }
        if (language == Language.CSV && lines.any { it.count { c -> c == ',' } != lines.firstOrNull()?.count { c -> c == ',' } }) {
            findings += Finding("structure", "CSV rows have inconsistent column counts", null, Confidence.POSSIBLE)
        }
        if (input.content.length > 1_000_000) findings += Finding("resource", "Large input; bounded processing is recommended", null, Confidence.CONFIRMED)

        return Analysis(input.path, language, lines.size, sha256(input.content), findings.distinct(), symbols.distinct(), dependencies.distinct(), sections.distinct())
    }

    fun compare(before: FileInput, after: FileInput): List<Finding> {
        val b = before.content.lines()
        val a = after.content.lines()
        val result = mutableListOf<Finding>()
        if (before.path != after.path) result += Finding("identity", "Compared files have different paths", confidence = Confidence.CONFIRMED)
        if (before.content == after.content) return result
        result += Finding("change", "Content changed", confidence = Confidence.CONFIRMED)
        val max = maxOf(b.size, a.size)
        for (i in 0 until max) {
            if (b.getOrNull(i) != a.getOrNull(i)) {
                result += Finding("diff", "Line changed", i + 1, Confidence.CONFIRMED)
                if (result.count { it.category == "diff" } >= 50) break
            }
        }
        return result
    }

    fun safeRepairProposal(analysis: Analysis): List<String> = analysis.findings.mapNotNull { finding ->
        when (finding.category) {
            "security" -> "Review and remove the suspected secret; do not auto-edit."
            "maintenance" -> "Resolve or document the TODO/FIXME; do not auto-edit."
            "syntax" -> "Validate the file with a real parser/compiler before proposing a concrete fix."
            "structure" -> "Normalize the file structure after confirming the intended schema."
            else -> null
        }
    }.distinct()

    private fun detectLanguage(path: String, content: String): Language {
        return when (path.substringAfterLast('.', "").lowercase()) {
            "kt" -> Language.KOTLIN; "java" -> Language.JAVA; "py" -> Language.PYTHON
            "js" -> Language.JAVASCRIPT; "ts", "tsx" -> Language.TYPESCRIPT; "html", "htm" -> Language.HTML
            "css" -> Language.CSS; "c" -> Language.C; "cpp", "cc", "h", "hpp" -> Language.CPP
            "cs" -> Language.CSHARP; "mq5", "mqh" -> Language.MQL5; "pine" -> Language.PINE
            "md" -> Language.MARKDOWN; "json" -> Language.JSON; "csv" -> Language.CSV; "txt" -> Language.TEXT
            else -> when { content.trimStart().startsWith("{") -> Language.JSON; else -> Language.UNKNOWN }
        }
    }

    private fun dependencyToken(line: String): String? {
        val t = line.trim()
        return when {
            t.startsWith("import ") -> t.removePrefix("import ").trim().take(200)
            t.startsWith("from ") && " import " in t -> t.take(200)
            t.startsWith("#include") -> t.take(200)
            t.startsWith("require(") -> t.take(200)
            t.startsWith("implementation(") -> t.take(200)
            else -> null
        }
    }

    private fun looksLikeHardcodedSecret(line: String): Boolean {
        val lower = line.lowercase()
        return (lower.contains("api_key") || lower.contains("apikey") || lower.contains("secret") || lower.contains("access_token")) &&
            (line.contains("=") || line.contains(":")) && line.length > 12
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
