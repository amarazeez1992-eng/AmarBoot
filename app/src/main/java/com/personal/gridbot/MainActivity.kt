package com.personal.gridbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.personal.gridbot.ui.gridcontrol.GridControlScreen
import com.personal.gridbot.ui.gridcontrol.PreviewGridControlState
import com.personal.gridbot.ui.gridcontrol.GridControlState
import com.personal.gridbot.ui.theme.AmarTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            AmarTheme {

                var state by remember {
                    mutableStateOf(
                        PreviewGridControlState
                    )
                }

                Surface {

                    GridControlScreen(
                        state = state,

                        onTradingToggle = {
                            state = state.copy(
                                isTrading = !state.isTrading
                            )
                        },

                        onBuyToggle = {
                            state = state.copy(
                                buyEnabled = !state.buyEnabled
                            )
                        },

                        onSellToggle = {
                            state = state.copy(
                                sellEnabled = !state.sellEnabled
                            )
                        },

                        onGridToggle = {
                            state = state.copy(
                                gridEnabled = !state.gridEnabled
                            )
                        },

                        onCloseAll = {
                            state = state.copy(
                                positions = 0,
                                pendingOrders = 0,
                                floatingProfit = 0.0
                            )
                        },

                        onCloseBuy = {
                            state = state.copy(
                                positions = (
                                    state.positions - 1
                                ).coerceAtLeast(0)
                            )
                        },

                        onCloseSell = {
                            state = state.copy(
                                positions = (
                                    state.positions - 1
                                ).coerceAtLeast(0)
                            )
                        },

                        onDeletePending = {
                            state = state.copy(
                                pendingOrders = 0
                            )
                        },

                        onRebuild = {
                            state = state.copy(
                                gridEnabled = true,
                                pendingOrders = state.maxOrders
                            )
                        }
                    )
                }
            }
        }
    }
}
