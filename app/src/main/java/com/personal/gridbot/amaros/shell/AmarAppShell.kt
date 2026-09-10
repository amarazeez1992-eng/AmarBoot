package com.personal.gridbot.amaros.shell

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.core.AmarEvent
import com.personal.gridbot.amaros.core.AmarEventBus
import com.personal.gridbot.amaros.core.AmarRuntimeController
import com.personal.gridbot.amaros.core.AmarRuntimeTicker
import com.personal.gridbot.amaros.design.AmarLivingButton
import com.personal.gridbot.amaros.design.AmarLivingGlass
import com.personal.gridbot.amaros.design.AmarLivingMetric
import com.personal.gridbot.amaros.design.AmarLivingVisualEngine
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.navigation.AmarRoomWorkspace
import com.personal.gridbot.amaros.rooms.commandcenter.CommandCenterScreen

/** AMAR shell with B9-B12 demo runtime connected as a read-only data source. */
@Composable
fun AmarAppShell(initialState: AmarAppState = AmarAppState(), onOpenLegacyGrid: () -> Unit = {}) {
    var state by remember { mutableStateOf(initialState) }
    val runtimeController = remember { AmarRuntimeController() }
    val runtimeState by runtimeController.state.collectAsState()
    val runtimeTicker = remember { AmarRuntimeTicker(runtimeController) }
    val runtimeScope = rememberCoroutineScope()

    DisposableEffect(runtimeTicker, runtimeScope) {
        runtimeTicker.start(runtimeScope)
        onDispose { runtimeTicker.stop() }
    }

    val transition = rememberInfiniteTransition(label = "shell-depth")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(9000), RepeatMode.Reverse), label = "shell-phase")
    val telemetry = runtimeState.telemetry
    val health = runtimeState.health

    AmarLivingVisualEngine {
        Row(Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AmarLivingGlass(
                Modifier.width(116.dp).fillMaxSize().graphicsLayer {
                    rotationY = (phase - 0.5f) * 1.4f
                    cameraDistance = 18f
                    shadowElevation = 18f
                    scaleX = 1f + phase * 0.006f
                    scaleY = 1f + phase * 0.006f
                }, Color(0xFF35D6FF)
            ) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("AMAR", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text("TRADING OS", color = Color(0xFF35D6FF), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(7.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(AmarRoom.entries) { room ->
                            AmarLivingButton(room.titleAr, state.selectedRoom == room, when (room) {
                                AmarRoom.COMMAND_CENTER -> Color(0xFF35D6FF)
                                AmarRoom.BOT_LAB -> Color(0xFFFFC857)
                                AmarRoom.MARKET -> Color(0xFF39E58C)
                                AmarRoom.CHART -> Color(0xFF8B5CFF)
                                else -> Color(0xFFFF5DA2)
                            }, Modifier.fillMaxWidth()) {
                                state = state.copy(selectedRoom = room)
                                AmarEventBus.publish(AmarEvent.RoomSelected(room.name))
                            }
                        }
                    }
                }
            }
            Column(Modifier.fillMaxSize().graphicsLayer {
                rotationX = (0.5f - phase) * 0.5f
                cameraDistance = 24f
                shadowElevation = 22f
                translationY = (phase - 0.5f) * 3f
            }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AmarLivingGlass(Modifier.fillMaxWidth(), Color(0xFF8B5CFF)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("AMAR TRADING OS", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("${state.selectedRoom.emoji} ${state.selectedRoom.titleAr}  •  نظام حي", color = Color.White.copy(alpha = 0.64f))
                        }
                        AmarLivingButton("تجريبي", true, Color(0xFF39E58C)) {}
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AmarLivingMetric("السوق", telemetry.symbol, "${telemetry.timeframe} • ${telemetry.bid}", Color(0xFF35D6FF), Modifier.weight(1f))
                    AmarLivingMetric("النظام", "حي #${runtimeState.cycleNumber}", "DEMO • ${telemetry.spread}", Color(0xFF39E58C), Modifier.weight(1f))
                    AmarLivingMetric("الصحة", health.status.name, "أخطاء متتالية: ${health.consecutiveFailures}", Color(0xFFFFC857), Modifier.weight(1f))
                }
                Box(Modifier.fillMaxSize()) { AmarRoomContent(state.selectedRoom, onOpenLegacyGrid) }
            }
        }
    }
}

@Composable private fun AmarRoomContent(room: AmarRoom, onOpenLegacyGrid: () -> Unit) = when (room) {
    AmarRoom.COMMAND_CENTER -> CommandCenterScreen()
    AmarRoom.BOT_LAB -> LegacyGridEntry(onOpenLegacyGrid)
    else -> AmarRoomWorkspace(room)
}

@Composable private fun LegacyGridEntry(onOpenLegacyGrid: () -> Unit) {
    AmarLivingGlass(Modifier.fillMaxSize(), Color(0xFFFFC857)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("مختبر البوتات", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text("البوت الحالي محفوظ كما هو. لا تتم إضافة بوتات جديدة في هذه المرحلة.", color = Color.White.copy(alpha = 0.70f))
            AmarLivingButton("فتح واجهة Grid الحالية", true, Color(0xFFFFC857), Modifier.fillMaxWidth(), onOpenLegacyGrid)
        }
    }
}
