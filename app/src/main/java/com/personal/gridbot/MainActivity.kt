package com.personal.gridbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.shell.AmarAppShell
import com.personal.gridbot.ui.gridcontrol.GridControlScreen
import com.personal.gridbot.ui.gridcontrol.PreviewGridControlState
import com.personal.gridbot.ui.theme.AmarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showLegacyGrid by remember { mutableStateOf(false) }
            AmarTheme {
                Surface {
                    if (showLegacyGrid) {
                        var gridState by remember { mutableStateOf(PreviewGridControlState) }
                        GridControlScreen(
                            state = gridState,
                            onTradingToggle = { gridState = gridState.copy(isTrading = !gridState.isTrading) },
                            onBuyToggle = { gridState = gridState.copy(buyEnabled = !gridState.buyEnabled) },
                            onSellToggle = { gridState = gridState.copy(sellEnabled = !gridState.sellEnabled) },
                            onGridToggle = { gridState = gridState.copy(gridEnabled = !gridState.gridEnabled) },
                            onCloseAll = {
                                gridState = gridState.copy(positions = 0, pendingOrders = 0, floatingProfit = 0.0)
                            },
                            onCloseBuy = {
                                gridState = gridState.copy(positions = (gridState.positions - 1).coerceAtLeast(0))
                            },
                            onCloseSell = {
                                gridState = gridState.copy(positions = (gridState.positions - 1).coerceAtLeast(0))
                            },
                            onDeletePending = { gridState = gridState.copy(pendingOrders = 0) },
                            onRebuild = {
                                gridState = gridState.copy(gridEnabled = true, pendingOrders = gridState.maxOrders)
                            }
                        )
                    } else {
                        AmarAppShell(
                            initialState = AmarAppState(),
                            onOpenLegacyGrid = { showLegacyGrid = true }
                        )
                    }
                }
            }
        }
    }
}
