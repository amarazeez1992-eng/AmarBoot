package com.personal.gridbot.amaros.rooms.positions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PBlue=Color(0xFF38BDF8); private val PGreen=Color(0xFF22C55E); private val PRed=Color(0xFFFF5C7A); private val PGold=Color(0xFFFFB84D)
@Composable fun AmarPositionsModernScreen(){
 Column(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text("📋 الصفقات والأوامر",fontSize=23.sp,fontWeight=FontWeight.Black);Text("لوحة تشغيل مستقلة — لا تنفذ أي أمر من هذه الطبقة وحدها",color=MaterialTheme.colorScheme.onSurfaceVariant)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){M("مفتوحة","—",PBlue,Modifier.weight(1f));M("معلقة","—",PGold,Modifier.weight(1f));M("الربح العائم","—",PGreen,Modifier.weight(1f))}
  Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant.copy(.5f))){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("مخطط الصفقات",fontWeight=FontWeight.Black);Text("الرمز   الاتجاه   الحجم   الدخول   SL   TP   P/L",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);HorizontalDivider();Text("لا توجد بيانات تنفيذ مؤكدة حاليًا. عند اتصال Runtime ستظهر البيانات هنا دون تغيير طبقة التنفيذ.",color=PGold,fontSize=12.sp)}}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=PBlue)){Text("تحديث")};OutlinedButton({},Modifier.weight(1f)){Text("السجل")};Button({},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=PRed)){Text("طوارئ")}}
 }
}
@Composable private fun M(a:String,b:String,c:Color,m:Modifier){Card(m,shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(c.copy(.1f))){Column(Modifier.padding(10.dp)){Text(a,fontSize=10.sp);Text(b,fontWeight=FontWeight.Black,fontSize=15.sp)}}}
