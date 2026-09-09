package com.personal.gridbot.amaros.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.core.AmarEvent
import com.personal.gridbot.amaros.core.AmarEventBus
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.navigation.AmarRoomWorkspace
import com.personal.gridbot.amaros.rooms.commandcenter.CommandCenterScreen

/**
 * الغلاف الرئيسي لـ AMAR.
 * التنقل منفصل عن الوحدات، والغرف لا تحتوي على منطق تداول.
 * يمكن استبدال واجهة أي غرفة لاحقاً دون هدم بقية النظام.
 */
@Composable
fun AmarAppShell(
    initialState: AmarAppState = AmarAppState(),
    onOpenLegacyGrid: () -> Unit = {}
) {
    var state by remember { mutableStateOf(initialState) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(112.dp)
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("AMAR", style = MaterialTheme.typography.titleLarge)
                Text("نظام التداول", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(AmarRoom.entries) { room ->
                        val selected = state.selectedRoom == room
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    state = state.copy(selectedRoom = room)
                                    AmarEventBus.publish(AmarEvent.RoomSelected(room.name))
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(room.emoji)
                                Text(room.titleAr, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AMAR TRADING OS", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${state.selectedRoom.emoji} ${state.selectedRoom.titleAr}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text("تجريبي", color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                Spacer(Modifier.height(12.dp))
                AmarRoomContent(
                    room = state.selectedRoom,
                    onOpenLegacyGrid = onOpenLegacyGrid
                )
            }
        }
    }
}

@Composable
private fun AmarRoomContent(room: AmarRoom, onOpenLegacyGrid: () -> Unit) {
    when (room) {
        AmarRoom.COMMAND_CENTER -> CommandCenterScreen()
        AmarRoom.BOT_LAB -> LegacyGridEntry(onOpenLegacyGrid)
        else -> AmarRoomWorkspace(room)
    }
}

@Composable
private fun LegacyGridEntry(onOpenLegacyGrid: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onOpenLegacyGrid() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("مختبر البوتات", style = MaterialTheme.typography.headlineSmall)
            Text("البوت الحالي محفوظ كما هو. لا تتم إضافة بوتات جديدة في هذه المرحلة.")
            Text("اضغط هنا لفتح واجهة Grid الحالية.")
        }
    }
}
