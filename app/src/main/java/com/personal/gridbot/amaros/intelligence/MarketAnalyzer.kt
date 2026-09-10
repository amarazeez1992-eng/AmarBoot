package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataSnapshot

class MarketAnalyzer {
    fun analyze(data: AmarDataSnapshot): MarketContext {
        val mid = data.market.mid
        val spreadRatio = if (mid > 0.0) data.market.spread / mid else 0.0
        val drawdown = data.account.drawdownPercent
        val trendScore = kotlin.math.sin(data.generatedAtEpochMs.toDouble() / 9000.0).coerceIn(-1.0, 1.0)
        val momentumScore = kotlin.math.sin(data.generatedAtEpochMs.toDouble() / 4300.0).coerceIn(-1.0, 1.0)
        val volatilityScore = (spreadRatio * 1200.0).coerceIn(0.0, 1.0)
        val riskScore = (-drawdown / 5.0).coerceIn(-1.0, 0.0)
        return EvidenceFusion.fuse(
            listOf(
                Evidence(EvidenceType.TREND, trendScore, 0.70, "اتجاه تجريبي محسوب من تدفق البيانات"),
                Evidence(EvidenceType.MOMENTUM, momentumScore, 0.60, "زخم تجريبي متدرج"),
                Evidence(EvidenceType.VOLATILITY, volatilityScore, 0.75, "تقييم تقلب مبني على السبريد"),
                Evidence(EvidenceType.REGIME, riskScore, 0.80, "سياق المخاطر لا يسمح بتحويل التحليل إلى تنفيذ")
            )
        )
    }
}
