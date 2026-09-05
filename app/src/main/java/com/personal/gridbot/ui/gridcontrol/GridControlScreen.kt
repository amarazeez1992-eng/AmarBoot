package com.personal.gridbot.ui.gridcontrol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import com.personal.gridbot.ui.components.AmarButton
import com.personal.gridbot.ui.components.AmarButtonType
import com.personal.gridbot.ui.theme.LocalAmarPalette
import kotlin.math.abs

@Composable
fun GridControlScreen(
    state: GridControlState,
    onTradingToggle: () -> Unit,
    onBuyToggle: () -> Unit,
    onSellToggle: () -> Unit,
    onGridToggle: () -> Unit,
    onCloseAll: () -> Unit,
    onCloseBuy: () -> Unit,
    onCloseSell: () -> Unit,
    onDeletePending: () -> Unit,
    onRebuild: () -> Unit
) {
    val colors = LocalAmarPalette.current

    var selectedTimeframe by remember {
        mutableStateOf(state.selectedTimeframe)
    }

    var selectedSection by remember {
        mutableStateOf("الرئيسية")
    }

    var accountExpanded by remember {
        mutableStateOf(state.accountAdded)
    }

    var analysisExpanded by remember {
        mutableStateOf(true)
    }

    var controlExpanded by remember {
        mutableStateOf(true)
    }

    val pulseTransition = rememberInfiniteTransition(
        label = "amar_main_pulse"
    )

    val pulse by pulseTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1300,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "main_pulse"
    )

    val profitColor =
        if (state.floatingProfit >= 0.0) {
            colors.profit
        } else {
            colors.loss
        }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            AmarDynamicBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 18.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(500)
                    ) + slideInVertically(
                        animationSpec = tween(500),
                        initialOffsetY = { -30 }
                    )
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
                                    text = "مرحباً بك يا عمار 👋",
                                    color = colors.textPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(
                                    modifier = Modifier.height(3.dp)
                                )

                                Text(
                                    text = "AMAR TRADING SYSTEM",
                                    color = colors.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (state.isTrading) {
                                            colors.buy.copy(alpha = 0.12f)
                                        } else {
                                            colors.warning.copy(alpha = 0.12f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (state.isTrading) {
                                            colors.buy.copy(
                                                alpha = pulse * 0.65f
                                            )
                                        } else {
                                            colors.warning.copy(
                                                alpha = pulse * 0.65f
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 9.dp
                                    )
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (state.isTrading) {
                                                    colors.buy
                                                } else {
                                                    colors.warning
                                                },
                                                shape = CircleShape
                                            )
                                    )

                                    Text(
                                        text = if (state.isTrading) {
                                            "يعمل"
                                        } else {
                                            "متوقف"
                                        },
                                        color = if (state.isTrading) {
                                            colors.buy
                                        } else {
                                            colors.warning
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                AmarGlassCard {

                    Column {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "حساب التداول",
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = if (state.accountAdded) {
                                        "${state.accountBroker} • ${state.accountName}"
                                    } else {
                                        "لم تتم إضافة حساب"
                                    },
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = if (state.isDemoMode) {
                                    "DEMO"
                                } else {
                                    "LIVE"
                                },
                                color = if (state.isDemoMode) {
                                    colors.warning
                                } else {
                                    colors.buy
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        if (state.accountAdded) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                colors.surfaceElevated,
                                                colors.panel,
                                                colors.surface
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = colors.accent.copy(
                                            alpha = 0.30f
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        accountExpanded = !accountExpanded
                                    }
                                    .padding(14.dp)
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
                                                text = state.accountBroker,
                                                color = colors.accent,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Text(
                                                text = state.accountName,
                                                color = colors.textPrimary,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )

                                            Text(
                                                text = "رقم الحساب: ${state.accountNumber}",
                                                color = colors.textMuted,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Text(
                                            text = if (accountExpanded) {
                                                "▲"
                                            } else {
                                                "▼"
                                            },
                                            color = colors.accent,
                                            fontSize = 16.sp
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = accountExpanded,
                                        enter = fadeIn(
                                            tween(250)
                                        )
                                    ) {

                                        Column {

                                            Spacer(
                                                modifier = Modifier.height(14.dp)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {

                                                AmarMiniMetric(
                                                    title = "الرصيد",
                                                    value = "$%.2f".format(
                                                        state.balance
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                AmarMiniMetric(
                                                    title = "Equity",
                                                    value = "$%.2f".format(
                                                        state.equity
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    valueColor = profitColor
                                                )
                                            }

                                            Spacer(
                                                modifier = Modifier.height(8.dp)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {

                                                AmarMiniMetric(
                                                    title = "الهامش",
                                                    value = "$%.2f".format(
                                                        state.margin
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                AmarMiniMetric(
                                                    title = "الهامش الحر",
                                                    value = "$%.2f".format(
                                                        state.freeMargin
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        } else {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = colors.surfaceElevated.copy(
                                            alpha = 0.75f
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = colors.border,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Text(
                                        text = "💳",
                                        fontSize = 28.sp
                                    )

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = "أضف حساب التداول",
                                        color = colors.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    Text(
                                        text = "يمكنك استخدام التطبيق بدون حساب",
                                        color = colors.textMuted,
                                        fontSize = 10.sp
                                    )

                                    Spacer(
                                        modifier = Modifier.height(12.dp)
                                    )

                                    AmarButton(
                                        text = "+ إضافة حساب تداول",
                                        type = AmarButtonType.DEFAULT,
                                        glowing = true,
                                        onClick = {
                                            accountExpanded = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "لوحة الحساب",
                    color = colors.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    AmarDashboardMetric(
                        title = "الرصيد",
                        value = "$%.2f".format(state.balance),
                        modifier = Modifier.weight(1f),
                        accent = colors.accent
                    )

                    AmarDashboardMetric(
                        title = "Equity",
                        value = "$%.2f".format(state.equity),
                        modifier = Modifier.weight(1f),
                        accent = colors.buy
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    AmarDashboardMetric(
                        title = "ربح اليوم",
                        value = "%+.2f".format(
                            state.dailyProfit
                        ),
                        modifier = Modifier.weight(1f),
                        accent = if (state.dailyProfit >= 0) {
                            colors.profit
                        } else {
                            colors.loss
                        }
                    )

                    AmarDashboardMetric(
                        title = "الربح الكلي",
                        value = "%+.2f".format(
                            state.totalProfit
                        ),
                        modifier = Modifier.weight(1f),
                        accent = if (state.totalProfit >= 0) {
                            colors.profit
                        } else {
                            colors.loss
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    AmarDashboardMetric(
                        title = "الصفقات",
                        value = state.positions.toString(),
                        modifier = Modifier.weight(1f),
                        accent = colors.buy
                    )

                    AmarDashboardMetric(
                        title = "الرابحة",
                        value = state.winningTrades.toString(),
                        modifier = Modifier.weight(1f),
                        accent = colors.profit
                    )

                    AmarDashboardMetric(
                        title = "الخاسرة",
                        value = state.losingTrades.toString(),
                        modifier = Modifier.weight(1f),
                        accent = colors.loss
                    )
                }

                AmarGlassCard {

                    Column {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "حالة السوق",
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Text(
                                    text = "${state.symbol} • تحليل متعدد الفريمات",
                                    color = colors.textMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (
                                            state.marketDirection == "BUY"
                                        ) {
                                            colors.buy.copy(alpha = 0.13f)
                                        } else {
                                            colors.sell.copy(alpha = 0.13f)
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    )
                            ) {

                                Text(
                                    text = if (
                                        state.marketDirection == "BUY"
                                    ) {
                                        "🟢 شرائي"
                                    } else {
                                        "🔴 بيعي"
                                    },
                                    color = if (
                                        state.marketDirection == "BUY"
                                    ) {
                                        colors.buy
                                    } else {
                                        colors.sell
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "قوة الاتجاه ${state.marketStrength}%",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    color = colors.surfaceElevated,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        state.marketStrength
                                            .coerceIn(0, 100) / 100f
                                    )
                                    .height(8.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                colors.accent,
                                                colors.buy
                                            )
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "الفريمات",
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        val timeframes = listOf(
                            "M1",
                            "M5",
                            "M15",
                            "M30",
                            "H1",
                            "H2",
                            "H4"
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {

                            timeframes.chunked(4).forEach { rowItems ->

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {

                                    rowItems.forEach { timeframe ->

                                        val selected =
                                            selectedTimeframe == timeframe

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    color = if (selected) {
                                                        colors.accent.copy(
                                                            alpha = 0.15f
                                                        )
                                                    } else {
                                                        colors.surfaceElevated
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (selected) {
                                                        colors.accent
                                                    } else {
                                                        colors.border
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    selectedTimeframe =
                                                        timeframe
                                                }
                                                .padding(
                                                    vertical = 9.dp
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {

                                            Column(
                                                horizontalAlignment =
                                                    Alignment.CenterHorizontally
                                            ) {

                                                Text(
                                                    text = timeframe,
                                                    color = if (selected) {
                                                        colors.accent
                                                    } else {
                                                        colors.textPrimary
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(2.dp)
                                                )

                                                Text(
                                                    text = if (
                                                        timeframe == "M30"
                                                    ) {
                                                        "SELL"
                                                    } else {
                                                        "BUY"
                                                    },
                                                    color = if (
                                                        timeframe == "M30"
                                                    ) {
                                                        colors.sell
                                                    } else {
                                                        colors.buy
                                                    },
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    if (rowItems.size < 4) {
                                        Spacer(
                                            modifier = Modifier.weight(
                                                (4 - rowItems.size).toFloat()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AmarGlassCard {

                    Column {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    analysisExpanded =
                                        !analysisExpanded
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "📊 تحليل السوق",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = if (analysisExpanded) {
                                    "▲"
                                } else {
                                    "▼"
                                },
                                color = colors.accent
                            )
                        }

                        AnimatedVisibility(
                            visible = analysisExpanded,
                            enter = fadeIn(tween(250))
                        ) {

                            Column {

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                AmarAnalysisRow(
                                    title = "الاتجاه العام",
                                    value = if (
                                        state.marketDirection == "BUY"
                                    ) {
                                        "شرائي"
                                    } else {
                                        "بيعي"
                                    },
                                    valueColor = if (
                                        state.marketDirection == "BUY"
                                    ) {
                                        colors.buy
                                    } else {
                                        colors.sell
                                    }
                                )

                                AmarAnalysisRow(
                                    title = "الفريم الحالي",
                                    value = selectedTimeframe,
                                    valueColor = colors.accent
                                )

                                AmarAnalysisRow(
                                    title = "حالة السوق",
                                    value = "TRENDING",
                                    valueColor = colors.buy
                                )

                                AmarAnalysisRow(
                                    title = "التذبذب",
                                    value = "HIGH",
                                    valueColor = colors.warning
                                )

                                AmarAnalysisRow(
                                    title = "قوة الإشارة",
                                    value = "${state.marketStrength}%",
                                    valueColor = colors.accent
                                )
                            }
                        }
                    }
                }

                AmarGlassCard {

                    Column {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    controlExpanded =
                                        !controlExpanded
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "🎛️ التحكم المباشر",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Text(
                                    text = "تحكم وهمي Demo في هذه المرحلة",
                                    color = colors.textMuted,
                                    fontSize = 9.sp
                                )
                            }

                            Text(
                                text = if (controlExpanded) {
                                    "▲"
                                } else {
                                    "▼"
                                },
                                color = colors.accent
                            )
                        }

                        AnimatedVisibility(
                            visible = controlExpanded,
                            enter = fadeIn(tween(250))
                        ) {

                            Column {

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {

                                    AmarButton(
                                        text = "🟢 BUY",
                                        modifier = Modifier.weight(1f),
                                        type = AmarButtonType.BUY,
                                        enabled = state.buyEnabled,
                                        glowing = state.buyEnabled,
                                        onClick = onBuyToggle
                                    )

                                    AmarButton(
                                        text = "🔴 SELL",
                                        modifier = Modifier.weight(1f),
                                        type = AmarButtonType.SELL,
                                        enabled = state.sellEnabled,
                                        glowing = state.sellEnabled,
                                        onClick = onSellToggle
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {

                                    AmarButton(
                                        text = if (state.isTrading) {
                                            "⏸ إيقاف البوت"
                                        } else {
                                            "▶ تشغيل البوت"
                                        },
                                        modifier = Modifier.weight(1f),
                                        type = if (state.isTrading) {
                                            AmarButtonType.WARNING
                                        } else {
                                            AmarButtonType.SUCCESS
                                        },
                                        glowing = state.isTrading,
                                        onClick = onTradingToggle
                                    )

                                    AmarButton(
                                        text = if (state.gridEnabled) {
                                            "GRID ON"
                                        } else {
                                            "GRID OFF"
                                        },
                                        modifier = Modifier.weight(1f),
                                        type = AmarButtonType.DEFAULT,
                                        glowing = state.gridEnabled,
                                        onClick = onGridToggle
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {

                                    AmarButton(
                                        text = "إغلاق BUY",
                                        modifier = Modifier.weight(1f),
                                        type = AmarButtonType.BUY,
                                        onClick = onCloseBuy
                                    )

                                    AmarButton(
                                        text = "إغلاق SELL",
                                        modifier = Modifier.weight(1f),
                                        type = AmarButtonType.SELL,
                                        onClick = onCloseSell
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                AmarButton(
                                    text = "⚡ إغلاق جميع الصفقات",
                                    type = AmarButtonType.DANGER,
                                    glowing = true,
                                    onClick = onCloseAll
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {

                                    AmarButton(
                                        text = "🗑 حذف المعلقات",
                                        modifier = Modifier.weight(1f),
                                        type = AmarButtonType.WARNING,
                                        onClick = onDeletePending
                                    )

                                    AmarButton(
                                        text = "🔄 إعادة بناء",
                                        modifier = Modifier.weight(1f),
                                        type = AmarButtonType.DEFAULT,
                                        glowing = true,
                                        onClick = onRebuild
                                    )
                                }
                            }
                        }
                    }
                }

                AmarGlassCard {

                    Column {

                        Text(
                            text = "⚡ التتبع الديناميكي",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = "Trailing / Tracking",
                            color = colors.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            AmarMiniMetric(
                                title = "الحالة",
                                value = if (state.trailingEnabled) {
                                    "ACTIVE"
                                } else {
                                    "OFF"
                                },
                                modifier = Modifier.weight(1f),
                                valueColor = if (state.trailingEnabled) {
                                    colors.buy
                                } else {
                                    colors.textMuted
                                }
                            )

                            AmarMiniMetric(
                                title = "Grid Step",
                                value = "${state.gridStep}",
                                modifier = Modifier.weight(1f),
                                valueColor = colors.accent
                            )

                            AmarMiniMetric(
                                title = "Floating",
                                value = "%+.2f".format(
                                    state.floatingProfit
                                ),
                                modifier = Modifier.weight(1f),
                                valueColor = profitColor
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "السعر يتحرك → التتبع يتحرك معه",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .background(
                                    color = colors.surfaceElevated,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (state.trailingEnabled) {
                                        colors.buy.copy(alpha = 0.45f)
                                    } else {
                                        colors.border
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(10.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "ENTRY",
                                    color = colors.textMuted,
                                    fontSize = 8.sp
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .height(1.dp)
                                        .weight(1f)
                                        .background(
                                            colors.border
                                        )
                                )

                                Text(
                                    text = "SL",
                                    color = colors.sell,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = if (
                                                state.trailingEnabled
                                            ) {
                                                colors.buy
                                            } else {
                                                colors.neutral
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                AmarGlassCard {

                    Column {

                        Text(
                            text = "⚙️ إعدادات الشبكة",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        AmarSettingRow(
                            title = "حجم اللوت",
                            value = "%.2f".format(
                                state.lotStart
                            )
                        )

                        AmarSettingRow(
                            title = "Grid Step",
                            value = "${state.gridStep} points"
                        )

                        AmarSettingRow(
                            title = "الحد الأقصى للصفقات",
                            value = state.maxOrders.toString()
                        )

                        AmarSettingRow(
                            title = "Martingale",
                            value = "%.2f".format(
                                state.martingale
                            )
                        )

                        AmarSettingRow(
                            title = "Basket TP",
                            value = "$%.2f".format(
                                state.basketTp
                            ),
                            valueColor = colors.profit
                        )

                        AmarSettingRow(
                            title = "Basket SL",
                            value = "$%.2f".format(
                                abs(state.basketSl)
                            ),
                            valueColor = colors.loss
                        )

                        AmarSettingRow(
                            title = "Individual TP",
                            value = "$%.2f".format(
                                state.individualTp
                            ),
                            valueColor = colors.profit
                        )

                        AmarSettingRow(
                            title = "Individual SL",
                            value = "$%.2f".format(
                                abs(state.individualSl)
                            ),
                            valueColor = colors.loss
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {

                    listOf(
                        "الرئيسية",
                        "التحليل",
                        "البوت",
                        "الإعدادات"
                    ).forEach { item ->

                        val selected = selectedSection == item

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (selected) {
                                        colors.accent.copy(alpha = 0.15f)
                                    } else {
                                        colors.surface.copy(alpha = 0.85f)
                                    },
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) {
                                        colors.accent.copy(alpha = 0.65f)
                                    } else {
                                        colors.border
                                    },
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .clickable {
                                    selectedSection = item
                                }
                                .padding(
                                    vertical = 10.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = item,
                                color = if (selected) {
                                    colors.accent
                                } else {
                                    colors.textSecondary
                                },
                                fontSize = 9.sp,
                                fontWeight = if (selected) {
                                    FontWeight.ExtraBold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = "AMAR TRADING SYSTEM • DEMO MODE",
                    color = colors.textMuted,
                    fontSize = 8.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AmarGlassCard(
    content: @Composable () -> Unit
) {
    val colors = LocalAmarPalette.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.surface.copy(alpha = 0.96f),
                        colors.panel.copy(alpha = 0.91f),
                        colors.surfaceElevated.copy(alpha = 0.90f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = colors.border.copy(alpha = 0.90f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun AmarDashboardMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color
) {
    val colors = LocalAmarPalette.current

    Column(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = colors.surface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.32f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(13.dp)
    ) {

        Text(
            text = title,
            color = colors.textMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = value,
            color = accent,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AmarMiniMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    val colors = LocalAmarPalette.current

    Column(
        modifier = modifier
            .background(
                color = colors.surface.copy(alpha = 0.72f),
                shape = RoundedCornerShape(13.dp)
            )
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(13.dp)
            )
            .padding(10.dp)
    ) {

        Text(
            text = title,
            color = colors.textMuted,
            fontSize = 8.sp
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = value,
            color = valueColor ?: colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AmarAnalysisRow(
    title: String,
    value: String,
    valueColor: Color
) {
    val colors = LocalAmarPalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = colors.textSecondary,
            fontSize = 10.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = valueColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AmarSettingRow(
    title: String,
    value: String,
    valueColor: Color? = null
) {
    val colors = LocalAmarPalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = colors.textSecondary,
            fontSize = 10.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = valueColor ?: colors.textPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
