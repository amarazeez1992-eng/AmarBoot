package com.personal.gridbot.amaros.bots

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg=Color(0xFFF7FBFA); private val Ink=Color(0xFF163E45); private val Muted=Color(0xFF718C90); private val Teal=Color(0xFF13B98A); private val Blue=Color(0xFF3E78FF); private val Red=Color(0xFFE7525B); private val Gold=Color(0xFFD8A52A); private val Line=Color(0xFFDDEBE8)

@Composable
fun Bot1PremiumScreen(){
 var running by remember{mutableStateOf(false)}; var buy by remember{mutableStateOf(true)}; var sell by remember{mutableStateOf(true)}; var tab by remember{mutableStateOf(0)}
 var lot by remember{mutableStateOf("0.01")}; var step by remember{mutableStateOf("30")}; var max by remember{mutableStateOf("10")}; var multi by remember{mutableStateOf("2.00")}; var tp by remember{mutableStateOf("50")}; var sl by remember{mutableStateOf("-30")}; var trail by remember{mutableStateOf("0")}
 Surface(color=Bg,modifier=Modifier.fillMaxSize()){LazyColumn(contentPadding=androidx.compose.foundation.layout.PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Header(running)};item{Hero(running)};item{ControlRow(running,{running=!running})};item{DirectionRow(buy,sell,{buy=!buy},{sell=!sell})};item{Tabs(tab,{tab=it})};if(tab==0)item{Settings(lot,step,max,multi,tp,sl,trail,{lot=it},{step=it},{max=it},{multi=it},{tp=it},{sl=it},{trail=it})}else item{MarketPanel()};item{SafetyPanel()}}}
}
@Composable private fun Header(running:Boolean){Row(Modifier.fillMaxWidth().padding(horizontal=4.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("بوت 1",fontSize=27.sp,fontWeight=FontWeight.Black,color=Ink);Text("لوحة التداول الذكية",fontSize=10.sp,color=Muted,fontWeight=FontWeight.Bold)};Box(Modifier.size(12.dp).background(if(running)Teal else Red,CircleShape))}}
@Composable private fun Hero(running:Boolean){val pulse by rememberInfiniteTransition(label="botPulse").animateFloat(.97f,1.03f,infiniteRepeatable(tween(1800,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="p");Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Box(Modifier.size(205.dp).graphicsLayer{scaleX=pulse;scaleY=pulse}.background(Color.White,CircleShape).border(2.dp,Line,CircleShape),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("عمار",fontSize=31.sp,fontWeight=FontWeight.Black,color=Ink);Text(if(running)"يعمل" else "متوقف",fontSize=16.sp,fontWeight=FontWeight.Bold,color=if(running)Teal else Red);Text("شبكة + مضاعفة + سلة",fontSize=10.sp,color=Muted);Spacer(Modifier.height(9.dp));Box(Modifier.size(10.dp).background(if(running)Teal else Red,CircleShape))}}}}
@Composable private fun ControlRow(running:Boolean,toggle:()->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){PremiumButton(if(running)"إيقاف البوت" else "تشغيل البوت",if(running)Red else Teal,Modifier.weight(1f),toggle);PremiumButton("إعادة بناء",Blue,Modifier.weight(1f),{});PremiumButton("إغلاق الكل",Red,Modifier.weight(1f),{})}}
@Composable private fun DirectionRow(b:Boolean,s:Boolean,tb:()->Unit,ts:()->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){PremiumButton(if(b)"شراء • مفعّل" else "شراء • متوقف",Teal,Modifier.weight(1f),tb);PremiumButton(if(s)"بيع • مفعّل" else "بيع • متوقف",Gold,Modifier.weight(1f),ts)}}
@Composable private fun Tabs(tab:Int,set:(Int)->Unit){Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(18.dp)).border(1.dp,Line,RoundedCornerShape(18.dp)).padding(5.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)){PremiumButton("الإعدادات",Blue,Modifier.weight(1f)){set(0)};PremiumButton("حالة السوق",Teal,Modifier.weight(1f)){set(1)}}}
@Composable private fun Settings(l:String,s:String,m:String,x:String,tp:String,sl:String,tr:String,ol:(String)->Unit,os:(String)->Unit,om:(String)->Unit,ox:(String)->Unit,otp:(String)->Unit,osl:(String)->Unit,otr:(String)->Unit){Card(colors=CardDefaults.cardColors(Color.White),shape=RoundedCornerShape(22.dp),elevation=CardDefaults.cardElevation(4.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(13.dp)){Text("إعدادات بوت 1",fontSize=18.sp,fontWeight=FontWeight.Black,color=Ink);Setting("اللوت الابتدائي",l,ol);Setting("مسافة الشبكة",s,os);Setting("الحد الأقصى للصفقات",m,om);Setting("مضاعف اللوت",x,ox);Setting("هدف السلة — دولار",tp,otp);Setting("خسارة السلة — دولار",sl,osl);Setting("التتبع المتحرك",tr,otr)}}}
@Composable private fun Setting(label:String,value:String,on:(String)->Unit){OutlinedTextField(value,onValueChange=on,label={Text(label,fontSize=11.sp)},singleLine=true,modifier=Modifier.fillMaxWidth().padding(vertical=3.dp),shape=RoundedCornerShape(14.dp))}
@Composable private fun MarketPanel(){Card(colors=CardDefaults.cardColors(Color.White),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("حالة السوق",fontSize=18.sp,fontWeight=FontWeight.Black,color=Ink);Spacer(Modifier.height(8.dp));Text("البيانات الحقيقية تظهر عند اتصال MT5",color=Muted,fontSize=12.sp);Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){Metric("الاتجاه","—");Metric("السعر","—");Metric("الصفقات","—")}}}}
@Composable private fun Metric(a:String,b:String){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(a,fontSize=10.sp,color=Muted);Text(b,fontSize=19.sp,fontWeight=FontWeight.Bold,color=Ink)}}
@Composable private fun SafetyPanel(){Box(Modifier.fillMaxWidth().background(Color(0xFFFFFBF0),RoundedCornerShape(17.dp)).border(1.dp,Color(0xFFEBD9A7),RoundedCornerShape(17.dp)).padding(12.dp)){Column{Text("حاجز الأمان",fontWeight=FontWeight.Black,color=Gold);Text("التنفيذ الحقيقي لا يُفعّل من الواجهة وحدها؛ يجب اجتياز المصادقة والتصريح وقناة MT5.",fontSize=10.sp,color=Ink)}}}
@Composable private fun PremiumButton(text:String,color:Color,modifier:Modifier,on:()->Unit){Box(modifier.height(52.dp).background(Color.White,RoundedCornerShape(16.dp)).border(1.5.dp,color.copy(alpha=.55f),RoundedCornerShape(16.dp)).clickable(onClick=on).padding(horizontal=6.dp),contentAlignment=Alignment.Center){Text(text,fontSize=10.sp,fontWeight=FontWeight.Black,color=color)}}
