package com.personal.gridbot.amaros.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.personal.gridbot.amaros.data.AmarDataSnapshot
import com.personal.gridbot.amaros.intelligence.MarketAnalyzer
import com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Decision room shell. It is intentionally read-only until the confluence engines are connected. */
@Composable
fun AmarDecisionScreen() {
    val analyzer = remember { MarketAnalyzer() }
    val snapshot = remember { AmarDataSnapshot(generatedAtEpochMs = System.currentTimeMillis()) }
    val context = remember(snapshot.generatedAtEpochMs) { analyzer.analyze(snapshot) }
    val confidence = remember(context) {
        AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(
                quality = context.evidence.map { it.confidence }.average().coerceIn(0.0, 1.0),
                completeness = if (context.evidence.isNotEmpty()) 1.0 else 0.0,
                freshness = 1.0,
                agreement = (1.0 - context.evidence.map { kotlin.math.abs(it.score - context.directionalScore) }.average()).coerceIn(0.0, 1.0),
                sourceReliability = context.confidence
            )
        )
    }
    val decisionState = when {
        context.directionalScore > 0.18 -> "BULLISH"
        context.directionalScore < -0.18 -> "BEARISH"
        else -> "NEUTRAL"
    }
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("القرار", style = MaterialTheme.typography.headlineMedium)
        Text("محرك تجميع الأدلة والتوافق — واجهة القرار المستقبلية")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("القرار الحالي", style = MaterialTheme.typography.titleLarge)
                Text("حالة القرار: $decisionState")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("اتجاه: ${context.directionalScore.formatDecisionScore()}")
                    Text("نظام: ${context.regime}")
                    Text("ثقة: ${(confidence.score * 100).toInt()}%")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("مصادر القرار", style = MaterialTheme.typography.titleMedium)
                Text("Evidence → Fusion → Analyzer → Confidence → Decision")
                Text(context.explanation)
                Text("عدد الأدلة: ${context.evidence.size} • تصنيف الثقة: ${confidence.label}")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("حالة القرار", style = MaterialTheme.typography.titleMedium)
                Text(confidence.reasons.joinToString(" • "))
            }
        }
    }
}


private fun Double.formatDecisionScore(): String = "%.3f".format(this)
