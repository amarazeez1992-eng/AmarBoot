package com.personal.gridbot.amaros.bots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.broker.AmarMt5RuntimeRegistry
import com.personal.gridbot.amaros.chart.AmarTimeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.round

private val B0=Color(0xFF050817); private val B1=Color(0xFF0B1230); private val B2=Color(0xFF101A3D); private val BT=Color(0xFFF3FAFF); private val BM=Color(0xFF9BAED0); private val BC=Color(0xFF24E8FF); private val BG=Color(0xFF18F2A4); private val BR=Color(0xFFFF4F78); private val BY=Color(0xFFFFD166); private val BP=Color(0xFFFF6AD5)
private enum class BMode{BOT,MARKET}
private data class FrameState(val bias:Int?,val pct:Double?,val remain:Long,val available:Boolean)
private data class LiveState(val connected:Boolean,val balance:Double?,val equity:Double?,val positions:Int?,val pending:Int?,val floating:Double?)

@Composable
fun AmarBotLabInterfaceBScreen(onBackHome:()->Unit,selectedBot:Int,onBotSelected:(Int)->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current; val repo=remember(context){AmarBotVaultRepository(context)}; var bots by remember{mutableStateOf(repo.load())}; var mode by remember{mutableStateOf(BMode.BOT)}; var live by remember{mutableStateOf(LiveState(false,null,null,null,null,null))}; var frames by remember{mutableStateOf<Map<AmarTimeframe,FrameState>>(emptyMap())}; var tf by remember{mutableStateOf(AmarTimeframe.M1)}; var notice by remember{mutableStateOf("")}; val runtime=AmarMt5RuntimeRegistry.current(); val magic=if(selectedBot==1)20260908L else null; val strategy=bots.firstOrNull{it.botNumber==selectedBot}?.strategies?.firstOrNull{it.number==1}
 LaunchedEffect(runtime,selectedBot){while(true){val c=runtime?.client;if(c==null)live=LiveState(false,null,null,null,null,null)else live=withContext(Dispatchers.IO){runCatching{val a=c.account();val b=c.botStatus("XAUUSD",magic);LiveState(a.connected,a.balance,a.equity,b.positions,b.pendingOrders,b.floatingProfit)}.getOrDefault(LiveState(false,null,null,null,null,null))};delay(1500)}}
 LaunchedEffect(runtime){while(true){if(runtime==null){frames=emptyMap();delay(2500);continue};frames=buildMap{AmarTimeframe.entries.forEach{x->val cs=withContext(Dispatchers.IO){runCatching{runtime.marketData.refresh("XAUUSD",x)}.getOrNull()};val a=cs?.dropLast(1)?.lastOrNull();val b=cs?.lastOrNull();val d=if(a!=null&&b!=null)b.close-a.close else null;val p=if(d!=null&&a!=null&&a.close!=0.0)(abs(d)/a.close*100.0).coerceIn(0.0,100.0)else null;put(x,FrameState(d?.let{if(it>0)1 else if(it<0)-1 else 0},p,remaining(x,System.currentTimeMillis()),!cs.isNullOrEmpty()))}};delay(15000)}}
 LaunchedEffect(Unit){while(true){delay(1000);frames=frames.mapValues{(x,s)->s.copy(remain=remaining(x,System.currentTimeMillis()))}}}
 Box(Modifier.fillMaxSize().background(B0)){Column(Modifier.fillMaxSize()){BHeader(selectedBot,live,onBackHome);BSwitch(mode){mode=it};AnimatedContent(targetState=mode,label="bmode"){m->if(m==BMode.BOT)BBot(bots,selectedBot,strategy,tf,live,notice,{onBotSelected(it);notice="تم اختيار V$it"},{tf=it},{notice=it},{s->repo.saveStrategy(selectedBot,s);bots=repo.load();notice="✓ تم الحفظ"},{n->repo.deleteStrategy(selectedBot,n);bots=repo.load();notice="تم الحذف"})else BMarket(frames,overall(frames))}}}
}

@Composable private fun BHeader(bot:Int,live:LiveState,back:()->Unit){val pulse by rememberInfiniteTransition(label="pulse").animateFloat(.45f,1f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="p");Column(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(B1,Color(0xFF27134A),B1)))){Row(Modifier.fillMaxWidth().padding(9.dp),verticalAlignment=Alignment.CenterVertically){Button(onClick=back,shape=RoundedCornerShape(4.dp,18.dp,4.dp,18.dp),colors=ButtonDefaults.buttonColors(containerColor=BP,contentColor=Color.White)){Text("⌂")};Column(Modifier.weight(1f).padding(horizontal=8.dp)){Text("AMAR • INTERFACE B",color=BC,fontSize=17.sp,fontWeight=FontWeight.Black);Text("V$bot • مختبر هاتف حي",color=BM,fontSize=9.sp)};Box(Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(if(live.connected)BG.copy(alpha=pulse)else BM))};if(live.connected)Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){Metric("الرصيد",money(live.balance),BC,Modifier.weight(1f));Metric("صفقات","${live.positions?:0}",BG,Modifier.weight(.75f));Metric("معلقة","${live.pending?:0}",BY,Modifier.weight(.75f));Metric("الربح/الخسارة",money(live.floating),if((live.floating?:0.0)>=0)BG else BR,Modifier.weight(1.25f))}}}
@Composable private fun Metric(l:String,v:String,c:Color,m:Modifier){Column(m.background(B2,RoundedCornerShape(10.dp)).border(1.dp,c.copy(alpha=.3f),RoundedCornerShape(10.dp)).padding(6.dp)){Text(l,color=BM,fontSize=7.sp,maxLines=1);Text(v,color=c,fontSize=10.sp,fontWeight=FontWeight.Black,maxLines=1)}}
@Composable private fun BSwitch(m:BMode,f:(BMode)->Unit){Row(Modifier.fillMaxWidth().padding(7.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){Tab("🤖 البوت",m==BMode.BOT,Modifier.weight(1f)){f(BMode.BOT)};Tab("◈ حالة السوق",m==BMode.MARKET,Modifier.weight(1f)){f(BMode.MARKET)}}}
@Composable private fun Tab(t:String,s:Boolean,m:Modifier,click:()->Unit){val c by animateColorAsState(if(s)BC else B2,tween(220),label="tab");Box(m.height(43.dp).background(c,RoundedCornerShape(13.dp)).clickable{click()},contentAlignment=Alignment.Center){Text(t,color=if(s)Color.Black else BT,fontWeight=FontWeight.Black,fontSize=11.sp)}}

@Composable private fun BBot(bots:List<AmarSavedBot>,bot:Int,s:AmarSavedStrategy?,tf:AmarTimeframe,live:LiveState,notice:String,botSelect:(Int)->Unit,tfSelect:(AmarTimeframe)->Unit,msg:(String)->Unit,save:(AmarSavedStrategy)->Unit,delete:(Int)->Unit){LazyColumn(Modifier.fillMaxSize(),contentPadding=androidx.compose.foundation.layout.PaddingValues(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{BCard("البوتات","V1 إلى V10"){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){(1..10).forEach{n->Box(Modifier.weight(1f).height(34.dp).background(if(n==bot)BG else B2,RoundedCornerShape(8.dp)).clickable{botSelect(n)},contentAlignment=Alignment.Center){Text("V$n",color=if(n==bot)Color.Black else BT,fontSize=8.sp,fontWeight=FontWeight.Black)}}}}};item{BCard("فريم الدخول","اختر الفريم والعداد حي"){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf(AmarTimeframe.M1,AmarTimeframe.M5,AmarTimeframe.M15,AmarTimeframe.M30,AmarTimeframe.H1).forEach{x->Box(Modifier.weight(1f).height(36.dp).background(if(x==tf)BP else B2,RoundedCornerShape(9.dp)).clickable{tfSelect(x)},contentAlignment=Alignment.Center){Text(x.shortLabel,color=BT,fontWeight=FontWeight.Black,fontSize=9.sp)}}};Text("${tf.arabicLabel} • المتبقي ${formatRemaining(remaining(tf,System.currentTimeMillis()))}",color=BC,fontSize=13.sp,fontWeight=FontWeight.Black,modifier=Modifier.padding(top=5.dp))}};if(live.connected)item{BCard("المراقبة الحية",if((live.positions?:0)>0||(live.pending?:0)>0)"صفقات نشطة" else "متصل — بانتظار صفقة"){Text(if((live.floating?:0.0)<0)"⚠ تنبيه خسارة" else "✓ الوضع مستقر",color=if((live.floating?:0.0)<0)BR else BG,fontSize=15.sp,fontWeight=FontWeight.Black);Text("الصفقات ${live.positions?:0} • المعلقة ${live.pending?:0} • العائم ${money(live.floating)}",color=BT,fontSize=9.sp)}};item{BEditor(s,bot,save,delete,msg)};item{BCard("أوامر سريعة","لا نعرض نجاح التنفيذ قبل تأكيد Runtime"){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("إغلاق الشراء",BR,Modifier.weight(1f)){msg("طلب إغلاق الشراء — انتظار تأكيد التنفيذ")};Cmd("إغلاق البيع",BP,Modifier.weight(1f)){msg("طلب إغلاق البيع — انتظار تأكيد التنفيذ")}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("إغلاق الكل",BR,Modifier.weight(1f)){msg("⚠ طلب إغلاق الكل — انتظار التحقق")};Cmd("إعادة البناء",BC,Modifier.weight(1f)){msg("طلب إعادة البناء — انتظار Runtime")}}}};if(notice.isNotBlank())item{Box(Modifier.fillMaxWidth().background(B2,RoundedCornerShape(12.dp)).padding(9.dp)){Text(notice,color=BT,fontSize=9.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())}}}}

@Composable private fun BEditor(s:AmarSavedStrategy?,bot:Int,save:(AmarSavedStrategy)->Unit,delete:(Int)->Unit,msg:(String)->Unit){var lot by remember(s?.number,s?.profile?.lot){mutableStateOf((s?.profile?.lot?:.01).toFloat())};var mult by remember(s?.number,s?.profile?.multiplier){mutableStateOf((s?.profile?.multiplier?:2.0).toFloat())};var grid by remember(s?.number,s?.profile?.gridStep){mutableStateOf((s?.profile?.gridStep?:30.0).toFloat())};var max by remember(s?.number,s?.profile?.maxOrders){mutableStateOf((s?.profile?.maxOrders?:10).toFloat())};var tp by remember(s?.number,s?.profile?.basketTp){mutableStateOf((s?.profile?.basketTp?:50.0).toFloat())};var sl by remember(s?.number,s?.profile?.basketSl){mutableStateOf((s?.profile?.basketSl?:-30.0).toFloat())};var buy by remember(s?.number,s?.profile?.buyEnabled){mutableStateOf(s?.profile?.buyEnabled?:true)};var sell by remember(s?.number,s?.profile?.sellEnabled){mutableStateOf(s?.profile?.sellEnabled?:true)};BCard("المحرّك — سحب بالإصبع","اسحب ثم تطبيق أو حفظ"){Slide("اللوت",lot,.01f..5f,.01f){lot=it};Slide("مضاعف الشبكة",mult,.5f..5f,.05f){mult=it};Slide("مسافة الشبكة",grid,0f..500f,1f){grid=it};Slide("عدد الأوامر",max,1f..50f,1f){max=it};Slide("هدف السلة $",tp,0f..1000f,1f){this@BEditor;tp=it};Slide("وقف السلة $",sl,-1000f..0f,1f){sl=it};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Toggle("BUY",buy,Modifier.weight(1f)){buy=!buy};Toggle("SELL",sell,Modifier.weight(1f)){sell=!sell}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Button(onClick={msg("✓ تم تطبيق القيم على V$bot")},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=BC,contentColor=Color.Black)){Text("تطبيق",fontWeight=FontWeight.Black)};Button(onClick={save(AmarSavedStrategy(1,"واجهة B • استراتيجية 01",AmarBot1RuntimeConfig(lot.toDouble(),grid.toDouble(),max.toInt().coerceAtLeast(1),mult.toDouble(),tp.toDouble(),sl.toDouble(),0.0,buy,sell)))},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=BG,contentColor=Color.Black)){Text("حفظ",fontWeight=FontWeight.Black)};Button(onClick={delete(1)},colors=ButtonDefaults.buttonColors(containerColor=BR,contentColor=Color.White)){Text("حذف")}}}}
@Composable private fun Slide(l:String,v:Float,r:ClosedFloatingPointRange<Float>,step:Float,set:(Float)->Unit){Column(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(l,color=BT,fontSize=8.sp,modifier=Modifier.weight(1f));Text(if(v<10)"%.2f".format(java.util.Locale.US,v) else "%.0f".format(java.util.Locale.US,v),color=BC,fontWeight=FontWeight.Black,fontSize=11.sp)};Slider(value=v,onValueChange={set((round(it/step)*step).coerceIn(r.start,r.endInclusive))},valueRange=r)}}
@Composable private fun Toggle(l:String,on:Boolean,m:Modifier,click:()->Unit){val c by animateColorAsState(if(on)BG else BR,tween(180),label="toggle");Button(onClick=click,modifier=m.height(38.dp),colors=ButtonDefaults.buttonColors(containerColor=c,contentColor=Color.Black),shape=RoundedCornerShape(6.dp,17.dp,6.dp,17.dp)){Text(if(on)"● $l ON" else "○ $l OFF",fontSize=9.sp,fontWeight=FontWeight.Black)}}
@Composable private fun Cmd(l:String,c:Color,m:Modifier,click:()->Unit){Button(onClick=click,modifier=m.height(42.dp),colors=ButtonDefaults.buttonColors(containerColor=c,contentColor=if(c==BC)Color.Black else Color.White),shape=RoundedCornerShape(18.dp,6.dp,18.dp,6.dp)){Text(l,fontSize=9.sp,fontWeight=FontWeight.Black)}}

@Composable private fun BMarket(frames:Map<AmarTimeframe,FrameState>,all:Pair<Int?,Double?>){LazyColumn(Modifier.fillMaxSize(),contentPadding=androidx.compose.foundation.layout.PaddingValues(8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){item{val c=when(all.first){1->BG;-1->BR;else->BY};BCard("التقييم الكامل","اتفاق الفريمات المتاحة فقط"){Text(when(all.first){1->"السوق متفق شرائياً";-1->"السوق متفق بيعياً";else->"السوق غير محسوم"},color=c,fontSize=18.sp,fontWeight=FontWeight.Black);Text(if(all.second==null)"—" else "اتفاق ${"%.1f".format(java.util.Locale.US,all.second)}%",color=c,fontSize=12.sp,fontWeight=FontWeight.Bold);Text("المتاح ${frames.values.count{it.available}} / ${AmarTimeframe.entries.size}",color=BM,fontSize=8.sp)}};items(AmarTimeframe.entries){x->val s=frames[x];val c=when(s?.bias){1->BG;-1->BR;else->BM};Card(colors=CardDefaults.cardColors(B1),shape=RoundedCornerShape(13.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(x.shortLabel,color=BC,fontWeight=FontWeight.Black,fontSize=12.sp);Text(x.arabicLabel,color=BM,fontSize=7.sp)};Column(horizontalAlignment=Alignment.End){Text(when(s?.bias){1->"شرائي";-1->"بيعي";else->"UNKNOWN"},color=c,fontWeight=FontWeight.Black,fontSize=10.sp);Text(if(s?.pct==null)"—" else "${"%.1f".format(java.util.Locale.US,s.pct)}%",color=c,fontWeight=FontWeight.Black,fontSize=12.sp);Text("متبقي ${formatRemaining(s?.remain?:remaining(x,System.currentTimeMillis()))}",color=BM,fontSize=7.sp)};Spacer(Modifier.width(7.dp));Box(Modifier.size(10.dp).background(c,RoundedCornerShape(50)))}}}}}
@Composable private fun BCard(t:String,sub:String,content:@Composable()->Unit){Card(colors=CardDefaults.cardColors(B1),shape=RoundedCornerShape(17.dp),modifier=Modifier.fillMaxWidth().border(1.dp,Color(0xFF6D7BFF).copy(alpha=.25f),RoundedCornerShape(17.dp))){Column(Modifier.padding(9.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(t,color=BT,fontWeight=FontWeight.Black,fontSize=12.sp);Text(sub,color=BM,fontSize=7.sp);content()}}}
private fun money(v:Double?):String=v?.let{"%.2f".format(java.util.Locale.US,it)}?:"—"
private fun remaining(tf:AmarTimeframe,now:Long):Long{val i=Instant.ofEpochMilli(now);val u=i.atZone(ZoneOffset.UTC);val n=when(tf){AmarTimeframe.MN1->u.withDayOfMonth(1).plusMonths(1).truncatedTo(ChronoUnit.DAYS);AmarTimeframe.W1->u.toLocalDate().plusDays((8-u.dayOfWeek.value).toLong()).atStartOfDay(ZoneOffset.UTC);AmarTimeframe.D1->u.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);AmarTimeframe.H1->u.truncatedTo(ChronoUnit.HOURS).plusHours(1);AmarTimeframe.H2->fixed(u,2,true);AmarTimeframe.H3->fixed(u,3,true);AmarTimeframe.H4->fixed(u,4,true);AmarTimeframe.H6->fixed(u,6,true);AmarTimeframe.H8->fixed(u,8,true);AmarTimeframe.H12->fixed(u,12,true);AmarTimeframe.M1->fixed(u,1,false);AmarTimeframe.M2->fixed(u,2,false);AmarTimeframe.M3->fixed(u,3,false);AmarTimeframe.M4->fixed(u,4,false);AmarTimeframe.M5->fixed(u,5,false);AmarTimeframe.M6->fixed(u,6,false);AmarTimeframe.M10->fixed(u,10,false);AmarTimeframe.M12->fixed(u,12,false);AmarTimeframe.M15->fixed(u,15,false);AmarTimeframe.M20->fixed(u,20,false);AmarTimeframe.M30->fixed(u,30,false)};return Duration.between(i,n.toInstant()).toMillis().coerceAtLeast(0)}
private fun fixed(u:ZonedDateTime,size:Int,hour:Boolean):ZonedDateTime{val cur=if(hour)u.hour else u.minute;val nv=((cur/size)+1)*size;return if(hour){if(nv>=24)u.truncatedTo(ChronoUnit.DAYS).plusDays(1)else u.truncatedTo(ChronoUnit.DAYS).plusHours(nv.toLong())}else{if(nv>=60)u.truncatedTo(ChronoUnit.HOURS).plusHours(1)else u.truncatedTo(ChronoUnit.HOURS).plusMinutes(nv.toLong())}}
private fun formatRemaining(ms:Long):String{val s=(ms/1000).coerceAtLeast(0);return if(s>=3600)"%02d:%02d:%02d".format(s/3600,(s%3600)/60,s%60)else "%02d:%02d".format(s/60,s%60)}
private fun overall(f:Map<AmarTimeframe,FrameState>):Pair<Int?,Double?>{val v=f.values.mapNotNull{it.bias}.filter{it!=0};if(v.isEmpty())return null to null;val b=v.count{it>0};val s=v.count{it<0};return(if(b>=s)1 else -1) to (maxOf(b,s).toDouble()/v.size*100.0)}
