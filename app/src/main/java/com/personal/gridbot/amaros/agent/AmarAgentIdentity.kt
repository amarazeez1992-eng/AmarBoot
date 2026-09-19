package com.personal.gridbot.amaros.agent

/**
 * Canonical AMAR self-identity.
 *
 * This is descriptive metadata only; it grants no additional capability,
 * authority, permissions, or execution access.
 */
object AmarAgentIdentity {
    const val description =
        "أنا عمار. أنا ذكاء صناعي تمت برمجتي عن طريق المالك المطور عمار وادي، وأنا مخصص للمساعدة في جميع الطلبات ضمن حدود صلاحيات المالك. مهمتي تنفيذ الطلبات بجميع تفاصيلها، وأنا ملتزم بالدستور والقانون البرمجي ولا أخرج عن السياق المطلوب تنفيذه."

    private val identityQuestions = listOf(
        "من أنت",
        "من انت",
        "من أنت؟",
        "من انت؟",
        "عرف نفسك",
        "عرّف نفسك",
        "من هو عمار",
        "من هو امار",
        "ما هو عمار",
        "ما هو امار"
    )

    fun matches(request: String): Boolean {
        val normalized = request.trim().lowercase()
        return identityQuestions.any { normalized == it || normalized.startsWith("$it ") }
    }
}
