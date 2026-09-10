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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val B=Color(0xFF07121B); private val P=Color(0xFF0D202B); private val P2=Color(0xFF102A37); private val W=Color(0xFFE9FBFF); private val M=Color(0xFF8CA9B5)
private val C=Color(0xFF19E6FF); private val G=Color(0xFF00E6A0); private val BL=Color(0xFF4D7CFF); private val PU=Color(0xFFB14DFF); private val PK=Color(0xFFFF4FA3); private val Y=Color(0xFFFFC84D); private val R=Color(0xFFFF5364); private val L=Color(0xFF214452)

@Composable
fun Bot1PremiumScreenV2(){
    var bot by remember{mutableIntStateOf(1)}; var strategy by remember{mutableIntStateOf(1)}; var running by remember{mutableStateOf(false)}; var buy by remember{mutableStateOf(true)}; var sell by remember{mutableStateOf(true)}; var tab by remember{mutableIntStateOf(0)}
    var lot by remember{mutableFloatStateOf(.01f)}; var step by remember{mutableFloatStateOf(30f)}; var max by remember{mutableFloatStateOf(10f)}; var mult by remember{mutableFloatStateOf(2f)}; var tp by remember{mutableFloatStateOf(50f)}; var sl by remember{mutableFloatStateOf(-30f)}; var trail by remember{mutableFloatStateOf(0f)}
    SurfaceBot {
        LazyColumn(contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{Header(running)}; item{Bots(bot){bot=it;strategy=1}}; item{Core(bot,running)}; item{Controls(running){running=!running}}; item{Directions(buy,sell,{buy=!buy},{sell=!sell})}; item{Strategies(bot,strategy){strategy=it}}
            item{if(tab==0) Settings(lot,step,max,mult,tp,sl,trail,{lot=it},{step=it},{max=it},{mult=it},{tp=it},{sl=it},{trail=it}) else Bot1MarketStatus()}
            item{Tabs(tab){tab=it}}; item{Safety()}
        }
    }
}

@Composable private fun SurfaceBot(content:@Composable()->Unit){Box(Modifier.fillMaxSize().background(B),contentAlignment=Alignment.TopCenter){content()}}
@Composable private fun Header(run:Boolean){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("عمار",color=C,fontSize=30.sp,fontWeight=FontWeight.Black);Text("استوديو البوتات المتقدم",color=M,fontSize=11.sp)}; Dot(if(run)G else R,14.dp);Spacer(Modifier.width(7.dp));Text(if(run)"يعمل" else "جاهز",color=if(run)G else R,fontWeight=FontWeight.Bold,fontSize=12.sp)}}
@Composable private fun Bots(selected:Int,select:(Int)->Unit){Column{Title("خزنة البوتات","٤ بوتات مستقلة • لكل بوت مجموعة استراتيجيات");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){(1..4).forEach{n->val a=listOf(C,PU,PK,Y)[n-1];Column(Modifier.width(92.dp).background(if(n==selected)P2 else P,RoundedCornerShape(18.dp)).border(1.dp,if(n==selected)a else L,RoundedCornerShape(18.dp)).clickable{select(n)}.padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally){Dot(a,25.dp);Text("بوت $n",color=W,fontWeight=FontWeight.Black,fontSize=12.sp);Text(if(n==1)"مجهز" else "جاهز",color=a,fontSize=9.sp)}}}}
@Composable private fun Core(bot:Int,run:Boolean){val pulse by rememberInfiniteTransition(label="core").animateFloat(.95f,1.05f,infiniteRepeatable(tween(1200,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="p");Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Box(Modifier.size(220.dp).graphicsLayer{scaleX=pulse;scaleY=pulse}.shadow(28.dp,CircleShape).background(Brush.radialGradient(listOf(Color(0xFF173E4B),B)),CircleShape).border(2.dp,Brush.sweepGradient(listOf(C,PU,PK,Y,C)),CircleShape),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Dot(if(run)G else R,12.dp);Text("بوت $bot",C,14.sp,FontWeight.Bold);Text("عمار",W,36.sp,FontWeight.Black);Text(if(run)"المحرك يعمل" else "المحرك جاهز",if(run)G else M,12.sp);Text("شبكة • مضاعفة • سلة",Y,10.sp,FontWeight.Bold)}}}}
@Composable private fun Controls(run:Boolean,toggle:()->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Btn(if(run)"إيقاف" else "تشغيل",if(run)R else G,Modifier.weight(1f),toggle);Btn("إعادة بناء",BL,Modifier.weight(1f)){};Btn("إغلاق الكل",R,Modifier.weight(1f)) {}}}
@Composable private fun Directions(b:Boolean,s:Boolean,bt:()->Unit,st:()->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Btn(if(b)"شراء ●" else "شراء ○",G,Modifier.weight(1f),bt);Btn(if(s)"بيع ●" else "بيع ○",PK,Modifier.weight(1f),st)}}
@Composable private fun Strategies(bot:Int,selected:Int,select:(Int)->Unit){Column{Title("استراتيجيات بوت $bot","١٠ خانات مستقلة • لكل بوت مجموعة خاصة");(1..10).chunked(2).forEach{pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){pair.forEach{n->val a=listOf(C,G,BL,PU,PK,Y)[(n-1)%6];Column(Modifier.weight(1f).background(if(n==selected)P2 else P,RoundedCornerShape(15.dp)).border(1.dp,if(n==selected)a else L,RoundedCornerShape(15.dp)).clickable{select(n)}.padding(9.dp)){Text("استراتيجية $n",color=W,fontSize=11.sp,fontWeight=FontWeight.Bold);Text(if(n==selected)"محددة • قابلة للتعديل" else "خانة حفظ",color=a,fontSize=8.sp)}}}}}}
@Composable private fun Settings(lot:Float,step:Float,max:Float,mult:Float,tp:Float,sl:Float,tr:Float,a:(Float)->Unit,b:(Float)->Unit,c:(Float)->Unit,d:(Float)->Unit,e:(Float)->Unit,f:(Float)->Unit,g:(Float)->Unit){Card(colors=CardDefaults.cardColors(P),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Title("إعدادات البوت","تحكم دقيق بالسحب");S("اللوت",lot,.01f,1f,"%.2f",a,G);S("مسافة الشبكة",step,1f,300f,"%.0f",b,C);S("الحد الأقصى للصفقات",max,1f,100f,"%.0f",c,BL);S("مضاعف اللوت",mult,1f,5f,"%.2f",d,PU);S("هدف السلة — دولار",tp,0f,500f,"%.2f",e,Y);S("خسارة السلة — دولار",sl,-500f,0f,"%.2f",f,R);S("التتبع المتحرك",tr,0f,300f,"%.0f",g,PK)}}}
@Composable private fun S(label:String,v:Float,min:Float,max:Float,fmt:String,set:(Float)->Unit,accent:Color){Column{Row(Modifier.fillMaxWidth()){Text(label,M,10.sp,modifier=Modifier.weight(1f));Text(String.format(java.util.Locale.US,fmt,v),accent,14.sp,FontWeight.Black)};Slider(v,set,min..max)}}
@Composable private fun Tabs(tab:Int,set:(Int)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Btn("الإعدادات",if(tab==0)C M,Modifier.weight(1f)){set(0)};Btn("حالة السوق",if(tab==1)G M,Modifier.weight(1f)){set(1)}}}
@Composable private fun Safety(){Box(Modifier.fillMaxWidth().background(Color(0xFF171A25),RoundedCornerShape(18.dp)).border(1.dp,Y.copy(alpha=.65f),RoundedCornerShape(18.dp)).padding(12.dp)){Column{Text("حاجز الأمان",Y,fontWeight=FontWeight.Black);Text("التنفيذ الحقيقي يبقى مقفولاً حتى تكتمل المصادقة والتصريح وقناة MT5.",W,10.sp)}}}
@Composable private fun Title(t:String,s:String){Column(Modifier.padding(bottom=5.dp)){Text(t,W,16.sp,FontWeight.Black);Text(s,M,9.sp)}}
@Composable private fun Dot(c:Color,size:androidx.compose.ui.unit.Dp){Spacer(Modifier.size(size).background(c,CircleShape).shadow(8.dp,CircleShape))}
@Composable private fun Btn(text:String,color:Color,modifier:Modifier,onClick:()->Unit){Box(modifier.height(50.dp).background(Brush.linearGradient(listOf(P2,P)),RoundedCornerShape(16.dp)).border(1.dp,color.copy(alpha=.75f),RoundedCornerShape(16.dp)).clickable(onClick=onClick),contentAlignment=Alignment.Center){Text(text,color=W,fontSize=10.sp,fontWeight=FontWeight.Black)}}
