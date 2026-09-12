package com.personal.gridbot.amaros.ai

import androidx.compose.runtime.mutableStateListOf

/** Lightweight in-session conversation state; no remote persistence and no broker authority. */
class AmarAiConversationState {
    data class Message(val role: Role, val text: String)
    enum class Role { USER, AI, SYSTEM }

    val messages = mutableStateListOf<Message>(
        Message(Role.SYSTEM, "AMAR AI جاهز للتحليل والبحث والمحاكاة — بدون تنفيذ تداول مباشر.")
    )

    fun addUser(text: String) {
        if (text.isNotBlank()) messages += Message(Role.USER, text.trim())
    }

    fun addAi(text: String) {
        if (text.isNotBlank()) messages += Message(Role.AI, text.trim())
    }

    fun addSystem(text: String) {
        if (text.isNotBlank()) messages += Message(Role.SYSTEM, text.trim())
    }

    fun clear() {
        messages.clear()
        addSystem("تم بدء جلسة AMAR AI جديدة.")
    }
}
