package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val B=Color(0xFF050C14);private val P=Color(0xFF0A1722);private val I=Color(0xFF0E2230);private val C=Color(0xFF1DE5FF);private val G=Color(0xFFFFC84A);private val T=Color(0xFFE9FBFF)

@Composable fun AmarBotLabCompactInterface1Screen(onBackHome:()->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current;var selected by remember{mutableIntStateOf(AmarBotLabSelectionContext.selectedBot.coerceIn(1,10))};var strategy by remember{mutableIntStateOf(1)}
 Surface(Modifier.fillMaxSize(),color=B){Column(Modifier.fillMaxSize()){Row(Modifier.fillMaxWidth().background(P).padding(9.dp),verticalAlignment=Alignment.CenterVertically){Button(onClick=onBackHome,colors=ButtonDefaults.buttonColors(containerColor=G,contentColor=Color.Black)){Text("⌂")};Spacer(Modifier.width(8.dp));Column{Text("AMAR BOT LAB",color=C,fontSize=18.sp,fontWeight=FontWeight.Black);Text("البوت V$selected • الاستراتيجية V$strategy",color=Color(0xFF7896A5),fontSize=9.sp)}};LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(9.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{CompactStrip("البوتات",selected){selected=it;AmarBotLabSelectionContext.selectedBot=it}};item{CompactStrip("الاستراتيجيات",strategy){strategy=it}};item{Card(colors=CardDefaults.cardColors(P),shape=RoundedCornerShape(15.dp)){Column(Modifier.padding(10.dp)){Text("V$selected  /  Strategy V$strategy",color=C,fontSize=15.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(8.dp));Text("واجهة 1 — تحكم البوت والاستراتيجية",color=T,fontSize=11.sp);Text("الإعدادات الرقمية والسحب والإفلات تبقى داخل محرر الاستراتيجية الحالي دون تكبير بطاقات البوتات.",color=Color(0xFF7896A5),fontSize=9.sp)}}}}}}
}
@Composable private fun CompactStrip(title:String,selected:Int,onSelect:(Int)->Unit){Card(colors=CardDefaults.cardColors(P),shape=RoundedCornerShape(15.dp)){Column(Modifier.padding(8.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(title,color=T,fontSize=12.sp,fontWeight=FontWeight.Black);Text("V1–V10",color=Color(0xFF7896A5),fontSize=8.sp)};Spacer(Modifier.height(6.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){(1..10).forEach{n->Box(Modifier.weight(1f).height(32.dp).background(if(n==selected)C else I,RoundedCornerShape(7.dp)).clickable{onSelect(n)},contentAlignment=Alignment.Center){Text("V$n",color=if(n==selected)Color.Black else T,fontSize=8.sp,fontWeight=FontWeight.Black)}}}}}}
