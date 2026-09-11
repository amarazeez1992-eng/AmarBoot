package com.personal.gridbot.amaros.rooms.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ABlue=Color(0xFF38BDF8); private val ARed=Color(0xFFFF5C7A); private val AGreen=Color(0xFF22C55E); private val AOrange=Color(0xFFFFB84D)
@Composable fun AmarAlertsModernScreen(){
 val rules=listOf("صفقات جديدة" to "TRADE","خطر الحساب" to "RISK","اتصال MT5" to "CONNECTION","الشبكة" to "GRID","Trailing" to "TRAILING","الأخبار" to "NEWS")
 var enabled by remember{mutableStateOf(setOf("TRADE","RISK","CONNECTION","GRID","TRAILING","NEWS"))}
 Column(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  Text("🔔 مركز التنبيهات",fontSize=23.sp,fontWeight=FontWeight.Black);Text("نظام مستقل للقواعد والتنبيهات وسجل الأحداث",color=MaterialTheme.colorScheme.onSurfaceVariant)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Stat("القواعد",rules.size.toString(),ABlue,Modifier.weight(1f));Stat("مفعّل",enabled.size.toString(),AGreen,Modifier.weight(1f));Stat("الوضع","READY",AOrange,Modifier.weight(1f))}
  rules.forEach{r->val on=r.second in enabled;Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=if(on)AGreen.copy(.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(.45f))){Row(Modifier.fillMaxWidth().padding(13.dp),horizontalArrangement=Arrangement.SpaceBetween){Column{Text(r.first,fontWeight=FontWeight.Black);Text(r.second,fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Switch(on,{enabled=if(on)enabled-r.second else enabled+r.second})}}}
 }
}
@Composable private fun Stat(a:String,b:String,c:Color,m:Modifier){Card(m,shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(c.copy(.11f))){Column(Modifier.padding(10.dp)){Text(a,fontSize=10.sp);Text(b,fontWeight=FontWeight.Black,fontSize=15.sp)}}}
