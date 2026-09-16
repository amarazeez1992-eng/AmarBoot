package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.ai.core.AmarAiApprovalLedger
import org.json.JSONObject

/** Natural-language-facing GitHub tool gateway. Write/delete/merge operations require an approved proposal. */
class AmarAiGitHubToolGateway(private val context: Context) {
    private val github = AmarGitHubWorkspaceEngine(context)
    private val approvals = AmarAiApprovalLedger(context)

    suspend fun inspect(args: AmarAiGitHubIntent): AmarGitHubWorkspaceEngine.GitHubResult = inspect(args.toJson())

    suspend fun inspect(args: JSONObject): AmarGitHubWorkspaceEngine.GitHubResult = when (args.getString("operation")) {
        "repo_search" -> github.searchRepositories(args.getString("query"), args.optInt("limit", 10))
        "repo_inspect" -> github.inspectRepository(args.getString("owner"), args.getString("repo"))
        "code_search" -> github.searchCode(args.optString("owner").ifBlank { null }, args.optString("repo").ifBlank { null }, args.getString("query"), args.optInt("limit", 20))
        "file_read" -> github.readFile(args.getString("owner"), args.getString("repo"), args.getString("path"), args.optString("ref").ifBlank { null })
        "license_inspect" -> github.inspectLicense(args.getString("owner"), args.getString("repo"))
        else -> AmarGitHubWorkspaceEngine.GitHubResult(false, "github_inspect", 400, "عملية GitHub للقراءة غير معروفة")
    }

    suspend fun proposeWrite(args: AmarAiGitHubIntent): AmarGitHubWorkspaceEngine.GitHubResult = proposeWrite(args.toJson())

    suspend fun proposeWrite(args: JSONObject): AmarGitHubWorkspaceEngine.GitHubResult {
        val subject = "GITHUB|${args.optString("operation")}|${args.optString("owner")}/${args.optString("repo")}|${args.optString("path")}|${args.optString("branch")}|${args.optString("message")}".trimEnd('|')
        val proposal = approvals.propose(subject)
        return AmarGitHubWorkspaceEngine.GitHubResult(
            false,
            "approval_required",
            202,
            "تحتاج هذه العملية موافقة المستخدم قبل تنفيذها",
            JSONObject().put("proposalId", proposal.id).put("status", proposal.status.name).put("subject", proposal.subject).toString()
        )
    }

    suspend fun executeApproved(proposalId: String, args: JSONObject): AmarGitHubWorkspaceEngine.GitHubResult {
        if (!approvals.isApproved(proposalId)) {
            return AmarGitHubWorkspaceEngine.GitHubResult(false, "approval_required", 403, "الموافقة غير موجودة أو غير صالحة")
        }
        return when (args.getString("operation")) {
            "branch_create" -> github.createBranch(args.getString("owner"), args.getString("repo"), args.getString("branch"), args.getString("fromRef"))
            "file_create", "file_update" -> github.writeFile(args.getString("owner"), args.getString("repo"), args.getString("path"), args.getString("content"), args.getString("message"), args.optString("branch").ifBlank { null })
            "file_delete" -> github.deleteFile(args.getString("owner"), args.getString("repo"), args.getString("path"), args.getString("message"), args.optString("branch").ifBlank { null })
            "pull_request_create" -> github.createPullRequest(args.getString("owner"), args.getString("repo"), args.getString("title"), args.getString("head"), args.getString("base"), args.optString("body"))
            "pull_request_merge" -> github.mergePullRequest(args.getString("owner"), args.getString("repo"), args.getInt("number"), args.optString("method", "squash"))
            else -> AmarGitHubWorkspaceEngine.GitHubResult(false, "github_write", 400, "عملية الكتابة غير معروفة")
        }
    }
}
