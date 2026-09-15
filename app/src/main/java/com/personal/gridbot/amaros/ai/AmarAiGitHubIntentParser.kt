package com.personal.gridbot.amaros.ai

import org.json.JSONObject

/** Converts explicit GitHub requests into safe structured intents; values are never guessed. */
object AmarAiGitHubIntentParser {
    private val repoUrl = Regex("(?:https?://)?github\\.com/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+)(?:/.*)?", RegexOption.IGNORE_CASE)
    private val repo = Regex("(?:مستودع|repo|repository)\\s+([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+)", RegexOption.IGNORE_CASE)
    private val path = Regex("(?:ملف|file|path)\\s+([^\\s]+)", RegexOption.IGNORE_CASE)

    fun parse(request: String): JSONObject? {
        val q = request.trim()
        val match = repoUrl.find(q) ?: repo.find(q) ?: return null
        val owner = match.groupValues[1]
        val name = match.groupValues[2]
        val operation = when {
            q.contains("احذف") || q.contains("delete", true) -> "file_delete"
            q.contains("حدّث") || q.contains("حدث") || q.contains("عدل") || q.contains("عدّل") || q.contains("update", true) -> "file_update"
            q.contains("أنشئ") || q.contains("انشئ") || q.contains("أنشاء") || q.contains("create", true) -> "file_create"
            q.contains("فرع") || q.contains("branch", true) -> "branch_create"
            q.contains("ادمج") || q.contains("merge", true) -> "pull_request_merge"
            q.contains("طلب سحب") || q.contains("pull request", true) -> "pull_request_create"
            q.contains("ترخيص") || q.contains("license", true) -> "license_inspect"
            q.contains("ابحث") || q.contains("search", true) -> "code_search"
            q.contains("اقرأ") || q.contains("read", true) || q.contains("افتح") || q.contains("open", true) -> "file_read"
            else -> "repo_inspect"
        }
        val out = JSONObject().put("operation", operation).put("owner", owner).put("repo", name)
        path.find(q)?.let { out.put("path", it.groupValues[1].trimStart('/')) }
        return out
    }
}
