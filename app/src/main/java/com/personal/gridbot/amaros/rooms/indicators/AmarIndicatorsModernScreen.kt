package com.personal.gridbot.amaros.rooms.indicators

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

private val IBlue=Color(0xFF38BDF8); private val IPurple=Color(0xFF8B5CF6); private val IGreen=Color(0xFF22C55E); private val IOrange=Color(0xFFFFB84D)

@Composable
fun AmarIndicatorsModernScreen(){
 val t=rememberInfiniteTransition(label="indicator-live"); val pulse by t.animateFloat(.96f,1.04f,infiniteRepeatable(tween(1100),RepeatMode.Reverse),label="pulse")
 var selected by remember{mutableStateOf("الاتجاه")}; val groups=listOf("الاتجاه","الزخم","التذبذب","الحجم","البنية")
 val indicators=listOf("EMA" to "اتجاه السعر","SMA" to "متوسط الحركة","VWAP" to "السعر المرجعي","RSI" to "زخم","ATR" to "التذبذب","Volume" to "الحجم")
 Column(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(IBlue.copy(.28f),IPurple.copy(.24f),IOrange.copy(.18f))),RoundedCornerShape(26.dp)).padding(18.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(68.dp).scale(pulse).background(Brush.radialGradient(listOf(IBlue,IPurple,Color.Transparent)),CircleShape));Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text("INDICATOR DESK",fontSize=21.sp,fontWeight=FontWeight.Black);Text("مركز المؤشرات الحي — مستقل وقابل للتوسع",color=MaterialTheme.colorScheme.onSurfaceVariant)};Text("LIVE",color=IGreen,fontWeight=FontWeight.Black)}
  }
  LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){items(groups){g->FilterChip(selected==g,{selected=g},{Text(g)})}}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("المجموعة",selected,IBlue,Modifier.weight(1f));Metric("العناصر",indicators.size.toString(),IPurple,Modifier.weight(1f));Metric("الحالة","جاهز",IGreen,Modifier.weight(1f))}
  indicators.forEachIndexed{i,p->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(19.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(.55f))){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(IBlue.copy(.75f),IPurple.copy(.65f))),RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Text("${i+1}",color=Color.White,fontWeight=FontWeight.Black)};Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(p.first,fontWeight=FontWeight.Black);Text(p.second,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=11.sp)};AssistChip({}, {Text(if(selected=="الاتجاه")"ACTIVE" else "READY",fontSize=9.sp)})}}}
 }
}
@Composable private fun Metric(a:String,b:String,c:Color,m:Modifier){Card(m,shape=RoundedCornerShape(17.dp),colors=CardDefaults.cardColors(c.copy(.12f))){Column(Modifier.padding(11.dp)){Text(a,fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(b,fontWeight=FontWeight.Black,fontSize=14.sp)}}}
