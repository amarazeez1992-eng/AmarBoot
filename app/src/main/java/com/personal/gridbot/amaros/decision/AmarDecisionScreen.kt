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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.data.DemoDataProvider

/** Read-only decision room wired to the existing analysis/fusion/confidence path. */
@Composable
fun AmarDecisionScreen() {
    val confluence = remember { AmarDecisionConfluence(DemoDataProvider()) }
    val state = remember { confluence.evaluate() }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("القرار", style = MaterialTheme.typography.headlineMedium)
        Text("تجميع التحليل والتوافق والثقة — وضع DEMO للعرض فقط")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("القرار الحالي", style = MaterialTheme.typography.titleLarge)
                Text(state.proposal.direction.name)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("شراء: \${if (state.proposal.direction.name == "LONG_BIAS") "نشط" else "—"}")
                    Text("بيع: \${if (state.proposal.direction.name == "SHORT_BIAS") "نشط" else "—"}")
                    Text("ثقة: \${"%.0f".format(state.confidence.score * 100.0)}%")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("مصادر القرار", style = MaterialTheme.typography.titleMedium)
                Text("Fusion: \${"%.3f".format(state.context.directionalScore)}")
                Text("Analyzer evidence: \${state.context.evidence.size}")
                Text("Confidence: \${state.confidence.label}")
                Text(state.proposal.rationale)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("حالة المحركات", style = MaterialTheme.typography.titleMedium)
                Text("MarketAnalyzer → EvidenceFusion → DecisionEngine → ConfidenceEngine")
                Text("التنفيذ الحقيقي مغلق: \${!state.proposal.executable}")
            }
        }
    }
}
