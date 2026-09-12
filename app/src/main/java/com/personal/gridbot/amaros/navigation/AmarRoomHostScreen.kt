package com.personal.gridbot.amaros.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.accounts.AmarAccountsScreen
import com.personal.gridbot.amaros.ai.AmarAiExperienceScreenV2
import com.personal.gridbot.amaros.bots.AmarBotLabInterfaceHost
import com.personal.gridbot.amaros.broker.AmarMt5RuntimeRegistry
import com.personal.gridbot.amaros.chart.AmarLiveTradingChartHost
import com.personal.gridbot.amaros.chart.AmarTradingChartScreen
import com.personal.gridbot.amaros.design.AmarAppearanceScreen
import com.personal.gridbot.amaros.rooms.alerts.AmarAlertsModernScreen
import com.personal.gridbot.amaros.rooms.commandcenter.CommandCenterScreen
import com.personal.gridbot.amaros.rooms.indicators.AmarIndicatorsModernScreen
import com.personal.gridbot.amaros.rooms.library.AmarLibraryModernScreen
import com.personal.gridbot.amaros.rooms.news.AmarMarketPulse3DScreen
import com.personal.gridbot.amaros.rooms.positions.AmarPositionsModernScreen
import com.personal.gridbot.amaros.security.AmarProtectionCenterScreen
import com.personal.gridbot.amaros.settings.AmarAppAccountCenterScreen
import com.personal.gridbot.amaros.settings.AmarDeveloperOptionsScreen
import com.personal.gridbot.amaros.sync.AmarSyncCenterScreen
import com.personal.gridbot.ui.theme.AmarThemeMode

@Composable
fun AmarRoomHostScreen(room:AmarRoom,onBackHome:()->Unit,themeMode:AmarThemeMode,onThemeModeChange:(AmarThemeMode)->Unit,homeLayout:Int=1,onHomeLayoutChange:(Int)->Unit={}){
 if(room==AmarRoom.BOT_LAB){AmarBotLabInterfaceHost(onBackHome);return}
 val context=LocalContext.current;var settingsMode by remember{mutableIntStateOf(0)};var accountSyncMode by remember{mutableIntStateOf(0)}
 Surface(color=MaterialTheme.colorScheme.background,modifier=Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text("عمار",style=MaterialTheme.typography.headlineSmall);Text("${room.emoji} ${room.titleAr}",style=MaterialTheme.typography.titleMedium)};Button(onClick=onBackHome){Text("الرئيسية")}};when(room){AmarRoom.COMMAND_CENTER->CommandCenterScreen();AmarRoom.CHART->{val runtime=AmarMt5RuntimeRegistry.current();if(runtime==null)AmarTradingChartScreen(symbol="الذهب")else AmarLiveTradingChartHost(symbol="XAUUSD",provider=runtime.marketData)};AmarRoom.ACCOUNTS->AmarAccountsScreen();AmarRoom.INDICATORS->AmarIndicatorsModernScreen();AmarRoom.LIBRARY->AmarLibraryModernScreen();AmarRoom.ALERTS->AmarAlertsModernScreen();AmarRoom.POSITIONS->AmarPositionsModernScreen();AmarRoom.NEWS_SESSIONS->AmarMarketPulse3DScreen();AmarRoom.ANALYSIS->AmarAiExperienceScreenV2();AmarRoom.SETTINGS->Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(10.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Button({settingsMode=0},Modifier.weight(1f)){Text("الواجهات")};Button({settingsMode=1},Modifier.weight(1f)){Text("الحماية")};Button({settingsMode=2},Modifier.weight(1f)){Text("متقدم")};Button({settingsMode=3},Modifier.weight(1f)){Text("الحساب/المزامنة")}};when(settingsMode){0->AmarAppearanceScreen(themeMode,onThemeModeChange,homeLayout,onHomeLayoutChange);1->AmarProtectionCenterScreen(context);2->AmarDeveloperOptionsScreen();else->Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){Button({accountSyncMode=0},Modifier.weight(1f)){Text("حساب AMAR")};Button({accountSyncMode=1},Modifier.weight(1f)){Text("المزامنة واللابتوب")}};Box(Modifier.fillMaxSize()){if(accountSyncMode==0)AmarAppAccountCenterScreen()else AmarSyncCenterScreen()}}}};else->AmarRoomWorkspace(room)}}}}}
