package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val BG=Color(0xFF061019);private val PANEL=Color(0xFF0B1C27);private val PANEL2=Color(0xFF102936)
private val LINE=Color(0xFF1D4555);private val CYAN=Color(0xFF18E6FF);private val GREEN=Color(0xFF00E7A0)
private val GOLD=Color(0xFFFFC84A);private val RED=Color(0xFFFF5264);private val TEXT=Color(0xFFE9FBFF);private val MUTED=Color(0xFF8AA7B4)
private enum class Tab(val title:String,val icon:String){BOT("البوت","🤖"),MARKET("السوق","📈"),STRATEGIES("الاستراتيجيات","🧠"),BOTS("البوتات","🗂️")}

@Composable
fun Bot1UnifiedScreen(onBackHome:()->Unit){
    val context=LocalContext.current
    val repo=remember(context){AmarBotVaultRepository(context)}
    var bots by remember{mutableStateOf(repo.load())};var selectedBot by remember{mutableIntStateOf(1)}
    var tab by remember{mutableStateOf(Tab.BOT)};var strategy by remember{mutableIntStateOf(1)}
    var lastCommand by remember{mutableStateOf("لا يوجد أمر مرسل")};var notice by remember{mutableStateOf("")}
    var editBot by remember{mutableStateOf<AmarSavedBot?>(null)};var editStrategy by remember{mutableStateOf<AmarSavedStrategy?>(null)}
    val current=bots.firstOrNull{it.botNumber==selectedBot}?:bots.firstOrNull()
    fun refresh(){bots=repo.load()}
    Surface(color=BG,modifier=Modifier.fillMaxSize()){
        Column(Modifier.fillMaxSize()){
            Row(Modifier.fillMaxWidth().background(PANEL).padding(9.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick=onBackHome,colors=ButtonDefaults.buttonColors(containerColor=GOLD,contentColor=Color.Black)){Text("⌂")}
                Column(Modifier.weight(1f)){Text("AMAR BOT OS",color=CYAN,fontSize=20.sp,fontWeight=FontWeight.Black);Text("BOT ${current?.botNumber?:1} • ${current?.name?:"غير محدد"}",color=MUTED,fontSize=9.sp)}
                Pill("RUNTIME: UNKNOWN",GOLD)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(7.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){Tab.values().forEach{t->Button(onClick={tab=t},colors=ButtonDefaults.buttonColors(containerColor=if(tab==t)CYAN else PANEL2,contentColor=if(tab==t)Color.Black else TEXT),shape=RoundedCornerShape(14.dp)){Text("${t.icon} ${t.title}",fontSize=10.sp,fontWeight=FontWeight.Bold)}}}
            if(notice.isNotBlank()) Text(notice,color=GREEN,fontSize=9.sp,modifier=Modifier.padding(horizontal=12.dp))
            LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                item{CommandPanel(lastCommand){lastCommand=it}}
                when(tab){
                    Tab.BOT->{item{Hero()};item{RuntimeDetails()};item{Config(current?.strategies?.firstOrNull()?.profile?:AmarBot1RuntimeConfig())}}
                    Tab.MARKET->{item{Market()};item{MarketDetails()};item{RuntimeDetails()}}
                    Tab.STRATEGIES->{item{Strategies(current,strategy,{strategy=it},{s->editStrategy=s},{s->repo.saveStrategy(current?.botNumber?:1,s);refresh();notice="✓ تم حفظ الاستراتيجية ${s.number}"},{n->repo.deleteStrategy(current?.botNumber?:1,n);refresh();notice="تم حذف الاستراتيجية $n"})}}
                    Tab.BOTS->{item{Vault(bots,selectedBot,{selectedBot=it;tab=Tab.BOT},{b->editBot=b},{b->repo.deleteBot(b.botNumber);refresh();notice="تم حذف إعداد BOT ${b.botNumber}"},{b->repo.resetBot(b.botNumber);refresh();notice="تم تفريغ BOT ${b.botNumber}"},{val b=repo.addBot();refresh();selectedBot=b.botNumber;notice="تمت إضافة BOT ${b.botNumber}"})}}
                }
            }
        }
    }
    editBot?.let{b->BotEditDialog(b,{name->repo.renameBot(b.botNumber,name);refresh();notice="تم تحديث اسم BOT ${b.botNumber}";editBot=null},{editBot=null})}
    editStrategy?.let{s->StrategyEditDialog(s,{updated->repo.saveStrategy(current?.botNumber?:1,updated);refresh();notice="✓ تم تحديث الاستراتيجية ${updated.number}";editStrategy=null},{editStrategy=null})}
}

@Composable private fun CommandPanel(last:String,send:(String)->Unit){Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("لوحة الأوامر",color=GOLD,fontSize=17.sp,fontWeight=FontWeight.Black);Text(last,color=MUTED,fontSize=8.sp)};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("تشغيل",GREEN){send("START • طلب تشغيل BOT 1")};Cmd("إيقاف",GOLD){send("STOP • طلب إيقاف BOT 1")};Cmd("إغلاق الكل",RED){send("CLOSE_ALL • طلب إغلاق BOT 1")}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("إعادة بناء",CYAN){send("REBUILD • طلب Global Rebuild")};Cmd("Grid",CYAN){send("REBUILD_GRID • طلب إعادة بناء الشبكة")};Cmd("Tracking",CYAN){send("REBUILD_TRACKING • طلب إعادة بناء التتبع")}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("BUY",GREEN){send("SET_BUY_ENABLED • ON")};Cmd("SELL",GREEN){send("SET_SELL_ENABLED • ON")};Cmd("طوارئ",RED){send("EMERGENCY_LOCK • طلب قفل الطوارئ")}};Text("الأزرار تمثل طلبات أوامر؛ الحالة الفعلية تأتي من MT5/runtime ولا تتغير من UI وحدها.",color=MUTED,fontSize=8.sp)}}}
@Composable private fun RowScope.Cmd(text:String,color:Color,click:()->Unit)=Button(onClick=click,modifier=Modifier.weight(1f).height(40.dp),colors=ButtonDefaults.buttonColors(containerColor=color,contentColor=if(color==GOLD)Color.Black else Color.White),contentPadding=PaddingValues(2.dp)){Text(text,fontSize=8.sp,fontWeight=FontWeight.Black)}
@Composable private fun Hero(){Card(colors=CardDefaults.cardColors(PANEL2),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text("BOT 1 • Grid_Martingale_Basket_v2",color=CYAN,fontSize=18.sp,fontWeight=FontWeight.Black);Text("الحالة: UNKNOWN / بانتظار runtime الحقيقي من MT5",color=GOLD,fontSize=10.sp);Text("BOT_1 • Magic 20260908 • v2.00 • STRATEGY_01",color=MUTED,fontSize=9.sp)}}}
@Composable private fun RuntimeDetails(){val d=listOf("الرصيد" to "—","Equity" to "—","الربح العائم" to "—","الصفقات المفتوحة" to "0","الأوامر المعلقة" to "0","متوسط السلة" to "—","التعرض" to "—","Basket TP" to "$50.00","Basket SL" to "-$30.00","Trailing" to "$0.00","Martingale" to "2.00x","Grid Step" to "30","Max Orders" to "10","Direction" to "BUY + SELL","Reconciliation" to "UNKNOWN","الاتصال" to "READ-ONLY");Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text("تفاصيل التشغيل — 16+ نقطة",color=GOLD,fontWeight=FontWeight.Black);d.chunked(2).forEach{pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){pair.forEach{(a,b)->Metric(a,b,Modifier.weight(1f))};if(pair.size==1)Spacer(Modifier.weight(1f))}}}}}
@Composable private fun Market(){Card(colors=CardDefaults.cardColors(PANEL2),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("XAUUSD",color=CYAN,fontSize=24.sp,fontWeight=FontWeight.Black);Text("الذهب • حالة السوق",color=MUTED,fontSize=10.sp)};Pill("WAITING DATA",GOLD)};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Metric("Bid","—",Modifier.weight(1f));Metric("Ask","—",Modifier.weight(1f));Metric("Spread","—",Modifier.weight(1f))}}}}}
@Composable private fun MarketDetails(){val d=listOf("الاتجاه" to "—","قوة الحركة" to "—","الجلسة" to "—","التذبذب" to "—","آخر شمعة" to "—","الفريم" to "M1 / M5","حالة الاتصال" to "READ-ONLY","آخر تحديث" to "—");Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(10.dp)){Text("حالة السوق",color=GOLD,fontWeight=FontWeight.Black);d.forEach{Key(it.first,it.second)}}}}
@Composable private fun Config(c:AmarBot1RuntimeConfig){Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(11.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text("إعدادات BOT 1",color=GOLD,fontWeight=FontWeight.Black);Key("Lot",fmt(c.lot));Key("Grid",fmt(c.gridStep));Key("Max Orders",c.maxOrders.toString());Key("Multiplier",fmt(c.multiplier));Key("Basket TP",money(c.basketTp));Key("Basket SL",money(c.basketSl));Key("Trailing",money(c.trailing))}}}
@Composable private fun Strategies(current:AmarSavedBot?,selected:Int,select:(Int)->Unit,edit:(AmarSavedStrategy)->Unit,save:(AmarSavedStrategy)->Unit,delete:(Int)->Unit){Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Text("الاستراتيجيات — 10 خانات",color=CYAN,fontSize=18.sp,fontWeight=FontWeight.Black);(1..10).forEach{n->val s=current?.strategies?.firstOrNull{it.number==n};Row(Modifier.fillMaxWidth().background(if(n==selected)PANEL2 else PANEL,RoundedCornerShape(14.dp)).border(1.dp,if(n==selected)CYAN else LINE,RoundedCornerShape(14.dp)).clickable{select(n)}.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Text(if(s==null)"○" else "✓",color=if(s==null)MUTED else GREEN,fontWeight=FontWeight.Black);Spacer(Modifier.width(7.dp));Column(Modifier.weight(1f)){Text(s?.name?:"استراتيجية رقم $n",color=TEXT,fontWeight=FontWeight.Bold);Text(if(s==null)"غير محفوظ" else "محفوظة • ${s.riskProfile}",color=if(s==null)MUTED else GREEN,fontSize=8.sp)};if(s!=null){TextButton(onClick={edit(s)}){Text("تحرير",color=CYAN,fontSize=8.sp)};TextButton(onClick={delete(n)}){Text("حذف",color=RED,fontSize=8.sp)}}else{TextButton(onClick={save(AmarSavedStrategy(n,"استراتيجية رقم $n",AmarBot1RuntimeConfig()))}){Text("حفظ",color=GREEN,fontSize=8.sp)}};Text("$n",color=GOLD,fontWeight=FontWeight.Black)}};current?.strategies?.firstOrNull{it.number==selected}?.let{s->Key("Risk",s.riskProfile);Key("Rebuild",s.rebuildRule);Key("Entry",s.entryRule);Key("Metadata",s.metadata)}}}
@Composable private fun Vault(bots:List<AmarSavedBot>,selected:Int,select:(Int)->Unit,edit:(AmarSavedBot)->Unit,delete:(AmarSavedBot)->Unit,reset:(AmarSavedBot)->Unit,add:()->Unit){Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("خزنة البوتات",color=CYAN,fontSize=18.sp,fontWeight=FontWeight.Black);Button(onClick=add,colors=ButtonDefaults.buttonColors(containerColor=GREEN,contentColor=Color.Black)){Text("+ بوت",fontSize=9.sp)}};Text("BOT 1 هو البوت الحقيقي المعتمد؛ البقية خزائن إعداد وليست محركات تداول.",color=MUTED,fontSize=9.sp);bots.forEach{b->Row(Modifier.fillMaxWidth().background(if(b.botNumber==selected)PANEL2 else PANEL,RoundedCornerShape(15.dp)).border(1.dp,if(b.botNumber==selected)CYAN else LINE,RoundedCornerShape(15.dp)).clickable{select(b.botNumber)}.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(b.name,color=TEXT,fontWeight=FontWeight.Black);Text("${b.strategies.size}/10 استراتيجيات محفوظة",color=MUTED,fontSize=8.sp)};TextButton(onClick={edit(b)}){Text("تعديل",color=CYAN,fontSize=8.sp)};TextButton(onClick={reset(b)}){Text("تفريغ",color=GOLD,fontSize=8.sp)};if(b.botNumber!=1)TextButton(onClick={delete(b)}){Text("حذف",color=RED,fontSize=8.sp)};Pill(if(b.botNumber==1)"REAL BOT" else "VAULT",if(b.botNumber==1)GREEN else GOLD)}}}}
@Composable private fun BotEditDialog(bot:AmarSavedBot,save:(String)->Unit,cancel:()->Unit){var name by remember(bot){mutableStateOf(bot.name)};AlertDialog(onDismissRequest=cancel,title={Text("تحرير ${bot.name}")},text={OutlinedTextField(value=name,onValueChange={name=it},label={Text("اسم البوت")},singleLine=true)},confirmButton={TextButton(onClick={save(name)}){Text("حفظ",color=GREEN)}},dismissButton={TextButton(onClick=cancel){Text("إلغاء")}})}
@Composable private fun StrategyEditDialog(strategy:AmarSavedStrategy,save:(AmarSavedStrategy)->Unit,cancel:()->Unit){var name by remember(strategy){mutableStateOf(strategy.name)};var risk by remember(strategy){mutableStateOf(strategy.riskProfile)};var rebuild by remember(strategy){mutableStateOf(strategy.rebuildRule)};var entry by remember(strategy){mutableStateOf(strategy.entryRule)};var metadata by remember(strategy){mutableStateOf(strategy.metadata)};AlertDialog(onDismissRequest=cancel,title={Text("تحرير الاستراتيجية ${strategy.number}")},text={Column(verticalArrangement=Arrangement.spacedBy(6.dp)){OutlinedTextField(name,{name=it},label={Text("الاسم")},singleLine=true);OutlinedTextField(risk,{risk=it},label={Text("Risk")},singleLine=true);OutlinedTextField(rebuild,{rebuild=it},label={Text("Rebuild")},singleLine=true);OutlinedTextField(entry,{entry=it},label={Text("Entry")},singleLine=true);OutlinedTextField(metadata,{metadata=it},label={Text("ملاحظات")})}},confirmButton={TextButton(onClick={save(strategy.copy(name=name.ifBlank{strategy.name},riskProfile=risk,rebuildRule=rebuild,entryRule=entry,metadata=metadata))}){Text("حفظ",color=GREEN)}},dismissButton={TextButton(onClick=cancel){Text("إلغاء")}})}
@Composable private fun Metric(a:String,b:String,m:Modifier){Column(m.background(PANEL2,RoundedCornerShape(9.dp)).padding(7.dp)){Text(a,color=MUTED,fontSize=8.sp);Text(b,color=TEXT,fontSize=11.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun Key(a:String,b:String){Row(Modifier.fillMaxWidth().padding(vertical=2.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a,color=MUTED,fontSize=9.sp);Text(b,color=TEXT,fontSize=9.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun Pill(t:String,c:Color){Text(t,color=c,fontSize=8.sp,fontWeight=FontWeight.Black,modifier=Modifier.background(c.copy(alpha=.12f),RoundedCornerShape(20.dp)).padding(horizontal=8.dp,vertical=5.dp))}
private fun fmt(v:Double)=String.format(Locale.US,"%.2f",v);private fun money(v:Double)=String.format(Locale.US,"%.2f $",v)
