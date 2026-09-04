package com.personal.gridbot.ui.gridcontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.ui.components.AmarButton
import com.personal.gridbot.ui.components.AmarButtonType
import com.personal.gridbot.ui.components.AmarPanel
import com.personal.gridbot.ui.components.AmarSection
import com.personal.gridbot.ui.components.AmarStatus
import com.personal.gridbot.ui.components.AmarStatusType
import com.personal.gridbot.ui.theme.LocalAmarPalette

@Composable
fun GridControlScreen(
    state: GridControlState,
    onTradingToggle: () -> Unit = {},
    onBuyToggle: () -> Unit = {},
    onSellToggle: () -> Unit = {},
    onGridToggle: () -> Unit = {},
    onCloseAll: () -> Unit = {},
    onCloseBuy: () -> Unit = {},
    onCloseSell: () -> Unit = {},
    onDeletePending: () -> Unit = {},
    onRebuild: () -> Unit = {}
) {
    val colors = LocalAmarPalette.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {

        // ============================================================
        // HEADER
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "AMAR",
                            color = colors.accent,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "| GRID CONTROL",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AmarStatus(
                        text = if (state.isTrading) "RUNNING" else "PAUSED",
                        type = if (state.isTrading) {
                            AmarStatusType.RUNNING
                        } else {
                            AmarStatusType.PAUSED
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.symbol,
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )

                    Text(
                        text = state.botName,
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ============================================================
        // LIVE STATUS
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmarSection(
                title = "الحالة المباشرة"
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    StatusCard(
                        title = "POSITIONS",
                        value = state.positions.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    StatusCard(
                        title = "PENDING",
                        value = state.pendingOrders.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    StatusCard(
                        title = "PROFIT",
                        value = String.format(
                            "%.2f",
                            state.floatingProfit
                        ),
                        modifier = Modifier.weight(1f),
                        positive = state.floatingProfit >= 0
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ============================================================
        // GRID SETTINGS
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmarSection(
                title = "إعدادات الشبكة"
            ) {

                InfoRow(
                    label = "Lot Start",
                    value = String.format("%.2f", state.lotStart)
                )

                InfoRow(
                    label = "Grid Step",
                    value = "${state.gridStep} points"
                )

                InfoRow(
                    label = "Max Orders",
                    value = state.maxOrders.toString()
                )

                InfoRow(
                    label = "Martingale",
                    value = String.format("%.2f", state.martingale)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                AmarButton(
                    text = if (state.gridEnabled) {
                        "● GRID ENABLED"
                    } else {
                        "○ GRID DISABLED"
                    },
                    type = if (state.gridEnabled) {
                        AmarButtonType.SUCCESS
                    } else {
                        AmarButtonType.DEFAULT
                    },
                    glowing = state.gridEnabled,
                    onClick = onGridToggle
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ============================================================
        // BASKET MANAGEMENT
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmarSection(
                title = "إدارة السلة"
            ) {

                InfoRow(
                    label = "Basket TP",
                    value = "$${String.format("%.2f", state.basketTp)}"
                )

                InfoRow(
                    label = "Basket SL",
                    value = "$${String.format("%.2f", state.basketSl)}"
                )

                InfoRow(
                    label = "Equity",
                    value = "$${String.format("%.2f", state.equity)}"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ============================================================
        // INDIVIDUAL TRADES
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmarSection(
                title = "الصفقة الفردية"
            ) {

                InfoRow(
                    label = "Individual TP",
                    value = "$${String.format("%.2f", state.individualTp)}"
                )

                InfoRow(
                    label = "Individual SL",
                    value = "$${String.format("%.2f", state.individualSl)}"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ============================================================
        // ENHANCEMENT ENGINE
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmarSection(
                title = "محرك التعزيز"
            ) {

                AmarButton(
                    text = if (state.enhancementEnabled) {
                        "● ENHANCEMENT ON"
                    } else {
                        "○ ENHANCEMENT OFF"
                    },
                    type = if (state.enhancementEnabled) {
                        AmarButtonType.SUCCESS
                    } else {
                        AmarButtonType.DEFAULT
                    },
                    glowing = state.enhancementEnabled,
                    onClick = {}
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ============================================================
        // TRADING CONTROL
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmarSection(
                title = "التحكم بالتداول"
            ) {

                AmarButton(
                    text = if (state.isTrading) {
                        "● TRADING ON"
                    } else {
                        "○ TRADING OFF"
                    },
                    type = if (state.isTrading) {
                        AmarButtonType.SUCCESS
                    } else {
                        AmarButtonType.DEFAULT
                    },
                    glowing = state.isTrading,
                    onClick = onTradingToggle
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    AmarButton(
                        text = if (state.buyEnabled) {
                            "BUY ON"
                        } else {
                            "BUY OFF"
                        },
                        modifier = Modifier.weight(1f),
                        type = if (state.buyEnabled) {
                            AmarButtonType.BUY
                        } else {
                            AmarButtonType.DEFAULT
                        },
                        glowing = state.buyEnabled,
                        onClick = onBuyToggle
                    )

                    AmarButton(
                        text = if (state.sellEnabled) {
                            "SELL ON"
                        } else {
                            "SELL OFF"
                        },
                        modifier = Modifier.weight(1f),
                        type = if (state.sellEnabled) {
                            AmarButtonType.SELL
                        } else {
                            AmarButtonType.DEFAULT
                        },
                        glowing = state.sellEnabled,
                        onClick = onSellToggle
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // ============================================================
        // ACTIONS
        // ============================================================

        AmarPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmarSection(
                title = "التحكم والإجراءات"
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    AmarButton(
                        text = "CLOSE BUY",
                        modifier = Modifier.weight(1f),
                        type = AmarButtonType.BUY,
                        onClick = onCloseBuy
                    )

                    AmarButton(
                        text = "CLOSE SELL",
                        modifier = Modifier.weight(1f),
                        type = AmarButtonType.SELL,
                        onClick = onCloseSell
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                AmarButton(
                    text = "DELETE PENDING",
                    type = AmarButtonType.WARNING,
                    onClick = onDeletePending
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                AmarButton(
                    text = "REBUILD GRID",
                    type = AmarButtonType.SUCCESS,
                    glowing = true,
                    onClick = onRebuild
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                AmarButton(
                    text = "CLOSE ALL",
                    type = AmarButtonType.DANGER,
                    onClick = onCloseAll
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "AMAR GRID CONTROL • UI BUILD",
            modifier = Modifier.fillMaxWidth(),
            color = colors.textMuted,
            fontSize = 10.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    positive: Boolean = true
) {
    val colors = LocalAmarPalette.current

    AmarPanel(
        modifier = modifier
    ) {
        Column {
            Text(
                text = title,
                color = colors.textMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = value,
                color = if (positive) {
                    colors.textPrimary
                } else {
                    colors.loss
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    val colors = LocalAmarPalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
