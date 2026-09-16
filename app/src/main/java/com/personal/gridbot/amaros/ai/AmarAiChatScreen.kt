package com.personal.gridbot.amaros.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class AmarChatMessage(val text: String, val fromUser: Boolean)

@Composable
fun AmarAiChatScreen(onBackHome: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    var attachmentsOpen by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<AmarChatMessage>() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("AMAR AI", style = MaterialTheme.typography.headlineSmall)
                    Text("Agent • واجهة المحادثة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                }
                OutlinedButton(onClick = onBackHome) { Text("رجوع") }
            }
            Spacer(Modifier.height(8.dp))

            if (messages.isEmpty()) {
                Column(Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("كيف أستطيع مساعدتك؟", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("هذه واجهة جاهزة للربط لاحقاً مع AMAR AI Agent والمحركات.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), reverseLayout = false) {
                    items(messages) { message ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.Start else Arrangement.End) {
                            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (message.fromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                                Text(message.text, Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }

            if (attachmentsOpen) {
                Card(Modifier.fillMaxWidth().padding(bottom = 6.dp), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("الإضافات", style = MaterialTheme.typography.titleMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("📷 صورة", "📎 ملف", "📄 مستند").forEach { label ->
                                TextButton(onClick = { }) { Text(label) }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("🔎 تحليل", "🖥 مشاركة الشاشة", "📹 الكاميرا").forEach { label ->
                                TextButton(onClick = { }) { Text(label) }
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = { attachmentsOpen = !attachmentsOpen }, Modifier.size(48.dp).clip(CircleShape)) { Text("+") }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اكتب رسالتك إلى AMAR AI...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )
                IconButton(onClick = { }, Modifier.size(48.dp).clip(CircleShape)) { Text("🎙") }
                Button(onClick = { val text = draft.trim(); if (text.isNotEmpty()) { messages.add(AmarChatMessage(text, true)); draft = "" } }) { Text("إرسال") }
            }
        }
    }
}
