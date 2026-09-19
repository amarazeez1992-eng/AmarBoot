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
import com.personal.gridbot.amaros.intelligence.DecisionPipeline

@Composable
fun AmarDecisionScreen() {
    val pipeline = remember { DecisionPipeline(DemoDataProvider()) }
    val result = remember { pipeline.evaluate() }
    val proposal = result.proposal

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("القرار", style = MaterialTheme.typography.headlineMedium)
        Text("حالة القرار محسوبة من مسار AMAR الحالي")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("القرار الحالي", style = MaterialTheme.typography.titleLarge)
                Text("الحالة: ${proposal.direction}")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("النتيجة: ${"%.3f".format(java.util.Locale.US, proposal.score)}")
                    Text("الثقة: ${(proposal.confidence * 100).toInt()}%")
                    Text("تنفيذ: ${if (proposal.executable) "مسموح" else "مغلق"}")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("مصادر القرار", style = MaterialTheme.typography.titleMedium)
                Text(result.intelligence.context.explanation)
                Text("الأدلة: ${result.intelligence.context.evidence.size}")
                Text(proposal.rationale)
            }
        }
    }
}