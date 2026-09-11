package com.personal.gridbot.amaros.rooms.news

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val NBlue=Color(0xFF38BDF8); private val NPurple=Color(0xFF8B5CF6); private val NGreen=Color(0xFF22C55E); private val NGold=Color(0xFFFFB84D)
@Composable fun AmarMarketPulseModernScreen(){
 var tab by remember{mutableStateOf("A")}; var tick by remember{mutableIntStateOf(0)}; LaunchedEffect(Unit){while(true){delay(1000);tick++}}
 val time=SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(Date()); val pulse=rememberInfiniteTransition(label="market"); val s by pulse.animateFloat(.92f,1.08f,infiniteRepeatable(tween(1200),RepeatMode.Reverse),label="orb")
 Column(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("A" to "الأخبار","B" to "السوق","C" to "الجلسات").forEach{(k,v)->FilterChip(tab==k,{tab=k},Modifier.weight(1f),{Text("$k — $v")})}}
  Box(Modifier.fillMaxWidth().height(180.dp).background(Brush.linearGradient(listOf(NBlue.copy(.23f),NPurple.copy(.22f),NGold.copy(.15f))),RoundedCornerShape(28.dp))){Box(Modifier.align(Alignment.Center).size(118.dp).scale(s).background(Brush.radialGradient(listOf(NBlue,NPurple,Color.Transparent)),CircleShape));Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){Text(if(tab=="A")"NEWS" else if(tab=="B")"MARKET" else "EUROPE",fontWeight=FontWeight.Black,fontSize=22.sp);Text(time,color=Color.White,fontSize=11.sp)}}
  when(tab){"A"->PulseCard("الأخبار","موجز الأحداث عالي التأثير",NBlue,listOf("الأولوية: الأخبار المؤثرة","التنبيه مرتبط بقواعد مركز التنبيهات","المصدر الحي يضاف عبر موصل مستقل"));"B"->PulseCard("حالة السوق","مراقبة ديناميكية",NGreen,listOf("الاتجاه: —","التذبذب: —","السيولة: —"));"C"->PulseCard("السوق الأوروبي","جلسة أوروبا",NGold,listOf("الحالة: تُحسب من الوقت المحلي","فتح / نشاط / إغلاق","مؤشر مستقل للجلسات"))}
 }
}
@Composable private fun PulseCard(title:String,sub:String,c:Color,lines:List<String>){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(21.dp),colors=CardDefaults.cardColors(containerColor=c.copy(.09f))){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,fontWeight=FontWeight.Black,fontSize=19.sp);Text(sub,color=MaterialTheme.colorScheme.onSurfaceVariant);lines.forEach{Text("• $it",fontSize=12.sp)}}}}
