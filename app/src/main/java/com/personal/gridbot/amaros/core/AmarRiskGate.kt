package com.personal.gridbot.amaros.core

import com.personal.gridbot.amaros.data.AmarDataSnapshot
import com.personal.gridbot.amaros.intelligence.DecisionEngine

/** B8 risk gate. It validates execution eligibility without placing trades. */
object AmarRiskGate {
    data class Result(
        val allowed: Boolean,
        val reason: String
    )

    fun validate(data: AmarDataSnapshot, proposal: DecisionEngine.DecisionProposal): Result {
        if (data.demoOnly) return Result(false, "وضع DEMO: التنفيذ الحقيقي مغلق")
        if (data.risk.emergencyStop) return Result(false, "تم تفعيل الإيقاف الطارئ")
        if (proposal.direction == DecisionEngine.Direction.NEUTRAL) {
            return Result(false, "لا يوجد اتجاه قرار قابل للتنفيذ")
        }
        if (proposal.confidence <= 0.0) return Result(false, "الثقة غير صالحة للتنفيذ")
        val maxDrawdown = data.risk.maxDrawdownPercent
        if (maxDrawdown > 0.0 && data.account.drawdownPercent >= maxDrawdown) {
            return Result(false, "تجاوز حد السحب المسموح")
        }
        return Result(true, "اجتاز القرار بوابة المخاطر")
    }
}
