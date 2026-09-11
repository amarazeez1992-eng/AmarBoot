package com.personal.gridbot.amaros.bots

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.chart.AmarTimeframe
import kotlin.math.round

private val B0=Color(0xFF050817); private val B1=Color(0xFF0B1230); private val B2=Color(0xFF101A3D)
private val BT=Color(0xFFF3FAFF); private val BM=Color(0xFF9BAED0); private val BC=Color(0xFF24E8FF)
private val BG=Color(0xFF18F2A4); private val BR=Color(0xFFFF4F78); private val BY=Color(0xFFFFD166); private val BP=Color(0xFFFF6AD5)

@Composable
fun AmarBotLabInterfaceBScreen(onBackHome:()->Unit, selectedBot:Int, onBotSelected:(Int)->Unit){
    val context=androidx.compose.ui.platform.LocalContext.current
    val repo=remember(context){AmarBotVaultRepository(context)}
    var bots by remember{mutableStateOf(repo.load())}
    var selectedStrategy by remember{mutableStateOf(1)}
    var notice by remember{mutableStateOf("")}
    val strategy=bots.firstOrNull{it.botNumber==selectedBot}?.strategies?.firstOrNull{it.number==selectedStrategy}
    val tf=AmarTradingTimeframeContext.selected

    Column(Modifier.fillMaxSize().background(B0)){
        Row(Modifier.fillMaxWidth().background(B1).padding(9.dp),verticalAlignment=Alignment.CenterVertically){
            Button(onClick=onBackHome,colors=ButtonDefaults.buttonColors(containerColor=BP,contentColor=Color.White)){Text("⌂")}
            Column(Modifier.weight(1f).padding(horizontal=8.dp)){Text("AMAR • INTERFACE B",color=BC,fontSize=17.sp,fontWeight=FontWeight.Black);Text("V$selectedBot • مختبر الهاتف",color=BM,fontSize=9.sp)}
            Box(Modifier.size(11.dp).background(BG,RoundedCornerShape(50)))
        }
        LazyColumn(Modifier.fillMaxSize(),contentPadding=androidx.compose.foundation.layout.PaddingValues(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            item{Card(B1){Column(Modifier.padding(9.dp)){Text("البوتات",color=BT,fontWeight=FontWeight.Black,fontSize=12.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){(1..10).forEach{n->Chip("V$n",n==selectedBot,Modifier.weight(1f)){onBotSelected(n)}}}}}}
            item{Card(B1){Column(Modifier.padding(9.dp)){Text("الاستراتيجيات",color=BT,fontWeight=FontWeight.Black,fontSize=12.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){(1..10).forEach{n->Chip(n.toString().padStart(2,'0'),n==selectedStrategy,Modifier.weight(1f)){selectedStrategy=n}}};Text("الاستراتيجية ${selectedStrategy.toString().padStart(2,'0')}",color=BC,fontSize=10.sp)}}}
            item{TimeframeStatus(tf)}
            item{Editor(strategy,selectedStrategy,selectedBot,{saved->repo.saveStrategy(selectedBot,saved);bots=repo.load();notice="✓ تم حفظ الاستراتيجية $selectedStrategy"},{n->repo.deleteStrategy(selectedBot,n);bots=repo.load();notice="تم حذف الاستراتيجية $n"},{notice="✓ تم تطبيق الإعدادات على واجهة B"})}
            item{Commands{notice=it}}
            if(notice.isNotBlank())item{Card(B2){Text(notice,color=BT,fontWeight=FontWeight.Bold,modifier=Modifier.fillMaxWidth().padding(9.dp))}}
        }
    }
}

@Composable private fun TimeframeStatus(tf:AmarTimeframe){
    var remaining by remember(tf){mutableStateOf(tf.remainingMillis())}
    androidx.compose.runtime.LaunchedEffect(tf){while(true){remaining=tf.remainingMillis();kotlinx.coroutines.delay(1000)}}
    Card(B1){Column(Modifier.padding(9.dp)){Text("فريم الدخول المحدد",color=BT,fontWeight=FontWeight.Black,fontSize=12.sp);Text("${tf.shortLabel} • ${tf.arabicLabel}",color=BC,fontSize=17.sp,fontWeight=FontWeight.Black);Text("بداية الشمعة التالية خلال ${formatTimeframeRemaining(remaining)}",color=BG,fontSize=11.sp,fontWeight=FontWeight.Bold);Text("غيّر الفريم من الشريط العام أعلى المختبر — ويطبق على جميع الواجهات.",color=BM,fontSize=8.sp)}}
}

@Composable private fun Editor(s:AmarSavedStrategy?,n:Int,bot:Int,save:(AmarSavedStrategy)->Unit,delete:(Int)->Unit,apply:()->Unit){
    var lot by remember(s?.number,s?.profile?.lot){mutableStateOf((s?.profile?.lot?:.01).toFloat())}
    var mult by remember(s?.number,s?.profile?.multiplier){mutableStateOf((s?.profile?.multiplier?:2.0).toFloat())}
    var grid by remember(s?.number,s?.profile?.gridStep){mutableStateOf((s?.profile?.gridStep?:30.0).toFloat())}
    var max by remember(s?.number,s?.profile?.maxOrders){mutableStateOf((s?.profile?.maxOrders?:10).toFloat())}
    var tp by remember(s?.number,s?.profile?.basketTp){mutableStateOf((s?.profile?.basketTp?:50.0).toFloat())}
    var sl by remember(s?.number,s?.profile?.basketSl){mutableStateOf((s?.profile?.basketSl?:-30.0).toFloat())}
    var buy by remember(s?.number,s?.profile?.buyEnabled){mutableStateOf(s?.profile?.buyEnabled?:true)}
    var sell by remember(s?.number,s?.profile?.sellEnabled){mutableStateOf(s?.profile?.sellEnabled?:true)}
    Card(B1){Column(Modifier.padding(9.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text("الاستراتيجية ${n.toString().padStart(2,'0')} • سحب بالإصبع",color=BT,fontWeight=FontWeight.Black,fontSize=12.sp);Slide("اللوت",lot,.01f..5f,.01f){lot=it};Slide("مضاعف الشبكة",mult,.5f..5f,.05f){mult=it};Slide("مسافة الشبكة",grid,0f..500f,1f){grid=it};Slide("عدد الأوامر",max,1f..50f,1f){max=it};Slide("هدف السلة $",tp,0f..1000f,1f){tp=it};Slide("وقف السلة $",sl,-1000f..0f,1f){sl=it};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Toggle("BUY",buy,Modifier.weight(1f)){buy=!buy};Toggle("SELL",sell,Modifier.weight(1f)){sell=!sell}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Button(onClick=apply,modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=BC,contentColor=Color.Black)){Text("تطبيق")};Button(onClick={save(AmarSavedStrategy(n,"واجهة B • استراتيجية ${n.toString().padStart(2,'0')}",AmarBot1RuntimeConfig(lot.toDouble(),grid.toDouble(),max.toInt().coerceAtLeast(1),mult.toDouble(),tp.toDouble(),sl.toDouble(),0.0,buy,sell)))},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=BG,contentColor=Color.Black)){Text("حفظ")};Button(onClick={delete(n)},colors=ButtonDefaults.buttonColors(containerColor=BR,contentColor=Color.White)){Text("حذف")}}}}
}

@Composable private fun Slide(label:String,value:Float,range:ClosedFloatingPointRange<Float>,step:Float,set:(Float)->Unit){Column(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth()){Text(label,color=BT,fontSize=8.sp,modifier=Modifier.weight(1f));Text(if(value<10)"%.2f".format(java.util.Locale.US,value)else"%.0f".format(java.util.Locale.US,value),color=BC,fontWeight=FontWeight.Black,fontSize=11.sp)};Slider(value=value,onValueChange={set((round(it/step)*step).coerceIn(range.start,range.endInclusive))},valueRange=range)}}
@Composable private fun Toggle(label:String,on:Boolean,m:Modifier,click:()->Unit){val c by animateColorAsState(if(on)BG else BR,tween(180),label="toggle");Button(onClick=click,modifier=m.height(38.dp),colors=ButtonDefaults.buttonColors(containerColor=c,contentColor=Color.Black)){Text(if(on)"● $label ON" else "○ $label OFF",fontSize=9.sp,fontWeight=FontWeight.Black)}}
@Composable private fun Commands(notice:(String)->Unit){Card(B1){Column(Modifier.padding(9.dp)){Text("أوامر سريعة",color=BT,fontWeight=FontWeight.Black,fontSize=12.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("إغلاق الشراء",BR,Modifier.weight(1f)){notice("طلب إغلاق الشراء — انتظار تأكيد Runtime")};Cmd("إغلاق البيع",BP,Modifier.weight(1f)){notice("طلب إغلاق البيع — انتظار تأكيد Runtime")}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("إغلاق الكل",BR,Modifier.weight(1f)){notice("⚠ طلب إغلاق الكل — انتظار التحقق")};Cmd("إعادة البناء",BC,Modifier.weight(1f)){notice("طلب إعادة البناء — انتظار Runtime")}}}}}
@Composable private fun Cmd(label:String,color:Color,m:Modifier,click:()->Unit){Button(onClick=click,modifier=m.height(42.dp),colors=ButtonDefaults.buttonColors(containerColor=color,contentColor=if(color==BC)Color.Black else Color.White),shape=RoundedCornerShape(18.dp,6.dp,18.dp,6.dp)){Text(label,fontSize=9.sp,fontWeight=FontWeight.Black)}}
@Composable private fun Chip(text:String,selected:Boolean,m:Modifier,click:()->Unit){Box(m.height(34.dp).background(if(selected)BG else B2,RoundedCornerShape(8.dp)).clickable{click()},contentAlignment=Alignment.Center){Text(text,color=if(selected)Color.Black else BT,fontSize=8.sp,fontWeight=FontWeight.Black)}}
