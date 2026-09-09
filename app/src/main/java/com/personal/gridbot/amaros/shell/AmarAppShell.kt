package com.personal.gridbot.amaros.shell

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.core.AmarEvent
import com.personal.gridbot.amaros.core.AmarEventBus
import com.personal.gridbot.amaros.design.AmarLivingButton
import com.personal.gridbot.amaros.design.AmarLivingGlass
import com.personal.gridbot.amaros.design.AmarLivingMetric
import com.personal.gridbot.amaros.design.AmarLivingVisualEngine
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.navigation.AmarRoomWorkspace
import com.personal.gridbot.amaros.rooms.commandcenter.CommandCenterScreen
import androidx.compose.material3.Text

/**
 * AMAR B4: living command shell.
 * Navigation and visual motion remain independent from trading logic.
 */
@Composable
fun AmarAppShell(
    initialState: AmarAppState = AmarAppState(),
    onOpenLegacyGrid: () -> Unit = {}
) {
    var state by remember { mutableStateOf(initialState) }
    val transition = rememberInfiniteTransition(label = "shell-depth")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
        label = "shell-phase"
    )

    AmarLivingVisualEngine {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AmarLivingGlass(
                modifier = Modifier
                    .width(116.dp)
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = (phase - 0.5f) * 1.4f
                        translationZ = 8f
                    },
                accent = Color(0xFF35D6FF)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text("AMAR", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    Text("TRADING OS", color = Color(0xFF35D6FF), style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(7.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(AmarRoom.entries) { room ->
                            AmarLivingButton(
                                label = room.titleAr,
                                active = state.selectedRoom == room,
                                accent = when (room) {
                                    AmarRoom.COMMAND_CENTER -> Color(0xFF35D6FF)
                                    AmarRoom.BOT_LAB -> Color(0xFFFFC857)
                                    AmarRoom.MARKET -> Color(0xFF39E58C)
                                    AmarRoom.CHART -> Color(0xFF8B5CFF)
                                    else -> Color(0xFFFF5DA2)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                state = state.copy(selectedRoom = room)
                                AmarEventBus.publish(AmarEvent.RoomSelected(room.name))
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = (0.5f - phase) * 0.5f
                        translationZ = 16f
                    },
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AmarLivingGlass(
                    modifier = Modifier.fillMaxWidth(),
                    accent = Color(0xFF8B5CFF)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("AMAR TRADING OS", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                            Text(
                                "${state.selectedRoom.emoji} ${state.selectedRoom.titleAr}  •  نظام حي",
                                color = Color.White.copy(alpha = 0.64f)
                            )
                        }
                        AmarLivingButton("تجريبي", true, Color(0xFF39E58C)) {}
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AmarLivingMetric("السوق", "XAUUSD", "حالة مباشرة", Color(0xFF35D6FF), Modifier.weight(1f))
                    AmarLivingMetric("النظام", "نشط", "المحرك البصري يعمل", Color(0xFF39E58C), Modifier.weight(1f))
                    AmarLivingMetric("الوضع", "DEMO", "بدون تداول حقيقي", Color(0xFFFFC857), Modifier.weight(1f))
                }

                Box(Modifier.fillMaxSize()) {
                    AmarRoomContent(state.selectedRoom, onOpenLegacyGrid)
                }
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
    AmarLivingGlass(modifier = Modifier.fillMaxSize(), accent = Color(0xFFFFC857)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("مختبر البوتات", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text("البوت الحالي محفوظ كما هو. لا تتم إضافة بوتات جديدة في هذه المرحلة.", color = Color.White.copy(alpha = 0.70f))
            AmarLivingButton("فتح واجهة Grid الحالية", true, Color(0xFFFFC857), Modifier.fillMaxWidth(), onOpenLegacyGrid)
        }
    }
}
