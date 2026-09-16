package com.personal.gridbot.amaros.ai

/** Immutable, Android-free GitHub request intent. Safe to unit-test on the JVM. */
data class AmarAiGitHubIntent(
    val operation: Operation,
    val owner: String,
    val repo: String,
    val path: String? = null
) {
    enum class Operation {
        REPO_INSPECT,
        CODE_SEARCH,
        FILE_READ,
        LICENSE_INSPECT,
        FILE_CREATE,
        FILE_UPDATE,
        FILE_DELETE,
        BRANCH_CREATE,
        PULL_REQUEST_CREATE,
        PULL_REQUEST_MERGE
    }

    /** Compatibility accessor for the supervisor; no Android/JSON dependency is introduced. */
    fun optString(key: String): String = when (key) {
        "operation" -> operation.wireName
        "owner" -> owner
        "repo" -> repo
        "path" -> path.orEmpty()
        else -> ""
    }

    private val Operation.wireName: String
        get() = when (this) {
            Operation.REPO_INSPECT -> "repo_inspect"
            Operation.CODE_SEARCH -> "code_search"
            Operation.FILE_READ -> "file_read"
            Operation.LICENSE_INSPECT -> "license_inspect"
            Operation.FILE_CREATE -> "file_create"
            Operation.FILE_UPDATE -> "file_update"
            Operation.FILE_DELETE -> "file_delete"
            Operation.BRANCH_CREATE -> "branch_create"
            Operation.PULL_REQUEST_CREATE -> "pull_request_create"
            Operation.PULL_REQUEST_MERGE -> "pull_request_merge"
        }
}

/** Parses only explicit GitHub repository requests; it never guesses missing repository values. */
object AmarAiGitHubIntentParser {
    private val repoUrl = Regex("(?:https?://)?github\\.com/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+)(?:/.*)?", RegexOption.IGNORE_CASE)
    private val repoRef = Regex("(?:مستودع|repo|repository)\\s+([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+)", RegexOption.IGNORE_CASE)
    private val pathRef = Regex("(?:ملف|file|path)\\s+([^\\s]+)", RegexOption.IGNORE_CASE)

    fun parse(request: String): AmarAiGitHubIntent? {
        val query = request.trim().replace(Regex("\\s+"), " ")
        if (query.isBlank()) return null
        val match = repoUrl.find(query) ?: repoRef.find(query) ?: return null
        val owner = match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
        val repo = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() } ?: return null
        val path = pathRef.find(query)?.groupValues?.getOrNull(1)?.trimStart('/')
        val operation = when {
            query.contains("احذف") || query.contains("delete", true) -> AmarAiGitHubIntent.Operation.FILE_DELETE
            query.contains("حدّث") || query.contains("حدث") || query.contains("عدل") || query.contains("عدّل") || query.contains("update", true) -> AmarAiGitHubIntent.Operation.FILE_UPDATE
            query.contains("أنشئ") || query.contains("انشئ") || query.contains("أنشاء") || query.contains("create", true) -> AmarAiGitHubIntent.Operation.FILE_CREATE
            query.contains("فرع") || query.contains("branch", true) -> AmarAiGitHubIntent.Operation.BRANCH_CREATE
            query.contains("ادمج") || query.contains("merge", true) -> AmarAiGitHubIntent.Operation.PULL_REQUEST_MERGE
            query.contains("طلب سحب") || query.contains("pull request", true) -> AmarAiGitHubIntent.Operation.PULL_REQUEST_CREATE
            query.contains("ترخيص") || query.contains("license", true) -> AmarAiGitHubIntent.Operation.LICENSE_INSPECT
            query.contains("ابحث") || query.contains("search", true) -> AmarAiGitHubIntent.Operation.CODE_SEARCH
            query.contains("اقرأ") || query.contains("read", true) -> AmarAiGitHubIntent.Operation.FILE_READ
            (query.contains("افتح") || query.contains("open", true)) && path != null -> AmarAiGitHubIntent.Operation.FILE_READ
            else -> AmarAiGitHubIntent.Operation.REPO_INSPECT
        }
        return AmarAiGitHubIntent(operation, owner, repo, path)
    }
}