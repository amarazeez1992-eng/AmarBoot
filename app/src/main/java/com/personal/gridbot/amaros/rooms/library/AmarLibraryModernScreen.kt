package com.personal.gridbot.amaros.rooms.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LBlue=Color(0xFF38BDF8); private val LPurple=Color(0xFF8B5CF6); private val LGreen=Color(0xFF22C55E); private val LGold=Color(0xFFFFB84D)
@Composable fun AmarLibraryModernScreen(){
 var tab by remember{mutableStateOf("الكل")}; val tabs=listOf("الكل","البوتات","المؤشرات","الاستراتيجيات","الحزم")
 val items=listOf("BOT 1" to "بوت التداول","Indicator Desk" to "مؤشرات","Strategy Vault" to "استراتيجيات","Chart Pack" to "حزمة رسوم","Risk Pack" to "إدارة المخاطر")
 Column(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(LPurple.copy(.25f),LBlue.copy(.22f),LGold.copy(.16f))),RoundedCornerShape(25.dp)).padding(18.dp)){Text("AMAR LIBRARY",fontSize=22.sp,fontWeight=FontWeight.Black);Text("مكتبة موحدة للبوتات والمؤشرات والاستراتيجيات",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  LazyRow(horizontalArrangement=Arrangement.spacedBy(7.dp)){items(tabs){FilterChip(tab==it,{tab=it},{Text(it)})}}
  items.forEachIndexed{i,x->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(.55f))){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(x.first,fontWeight=FontWeight.Black);Text("${i+1}",color=LBlue,fontWeight=FontWeight.Black)};Text(x.second,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp);LinearProgressIndicator({(i+2)/6f},Modifier.fillMaxWidth(),color=if(i%2==0)LBlue else LGreen);Text("وحدة مستقلة • قابلة للتوسعة",fontSize=10.sp,color=LGold)}}}
 }
}
