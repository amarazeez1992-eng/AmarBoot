package com.personal.gridbot.amaros.agent.claim

data class StructuredClaim(
    val id: String,
    val text: String,
    val subject: String,
    val predicate: String,
    val objectValue: String?
)
