package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

private val BG=Color(0xFF061019)
private val PANEL=Color(0xFF0B1C27)
private val PANEL2=Color(0xFF102936)
private val LINE=Color(0xFF1D4555)
private val CYAN=Color(0xFF18E6FF)
private val GREEN=Color(0xFF00E7A0)
private val GOLD=Color(0xFFFFC84A)
private val RED=Color(0xFFFF5264)
private val TEXT=Color(0xFFE9FBFF)
private val MUTED=Color(0xFF8AA7B4)

private enum class MainPage(val number:Int,val title:String,val icon:String){
    MAIN(1,"الرئيسية","🤖"),
    STRATEGIES(2,"الاستراتيجيات","🧠"),
    BOTS(3,"البوتات","🗂️")
}

@Composable
fun Bot1UnifiedScreen(onBackHome:()->Unit){
    val context=LocalContext.current
    val repo=remember(context){AmarBotVaultRepository(context)}
    var bots by remember{mutableStateOf(repo.load())}
    var selectedBot by remember{mutableIntStateOf(1)}
    var selectedStrategy by remember{mutableIntStateOf(1)}
    var page by remember{mutableStateOf(MainPage.MAIN)}
    var lastCommand by remember{mutableStateOf("لا يوجد أمر مرسل")}
    var notice by remember{mutableStateOf("")}
    var editBot by remember{mutableStateOf<AmarSavedBot?>(null)}
    var editStrategy by remember{mutableStateOf<AmarSavedStrategy?>(null)}
    val current=bots.firstOrNull{it.botNumber==selectedBot}?:bots.firstOrNull{it.botNumber==1}
    val currentStrategy=current?.strategies?.firstOrNull{it.number==selectedStrategy}
    fun refresh(){bots=repo.load()}

    Surface(color=BG,modifier=Modifier.fillMaxSize()){
        Column(Modifier.fillMaxSize()){
            Header(current,onBackHome)
            NumericNavigation(page){page=it}
            if(notice.isNotBlank()) Text(notice,color=GREEN,fontSize=9.sp,modifier=Modifier.padding(horizontal=12.dp,vertical=2.dp))
            LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                when(page){
                    MainPage.MAIN->{
                        item{BotSelector(selectedBot){selectedBot=it}}
                        item{CommandPanel(lastCommand){lastCommand=it}}
                        item{BotStatus(current,currentStrategy)}
                        item{MarketStatus()}
                        item{Settings(currentStrategy?.profile?:AmarBot1RuntimeConfig())}
                        item{RuntimeSummary(current,currentStrategy)}
                    }
                    MainPage.STRATEGIES->{
                        item{BotSelector(selectedBot){selectedBot=it}}
                        item{StrategySelector(selectedStrategy){selectedStrategy=it}}
                        item{Strategies(current,selectedStrategy,{selectedStrategy=it},{editStrategy=it},{s->repo.saveStrategy(current?.botNumber?:1,s);refresh();notice="✓ تم حفظ الاستراتيجية ${s.number}"},{n->repo.deleteStrategy(current?.botNumber?:1,n);refresh();notice="تم حذف الاستراتيجية $n"})}
                    }
                    MainPage.BOTS->{
                        item{Vault(bots,selectedBot,{selectedBot=it;page=MainPage.MAIN},{editBot=it},{b->repo.deleteBot(b.botNumber);refresh();notice="تم حذف إعداد BOT ${b.botNumber}"},{b->repo.resetBot(b.botNumber);refresh();notice="تم تفريغ BOT ${b.botNumber}"},{val b=repo.addBot();refresh();selectedBot=b.botNumber;notice="تمت إضافة BOT ${b.botNumber}"})}
                    }
                }
            }
        }
    }
    editBot?.let{b->BotEditDialog(b,{name->repo.renameBot(b.botNumber,name);refresh();notice="تم تحديث اسم BOT ${b.botNumber}";editBot=null},{editBot=null})}
    editStrategy?.let{s->StrategyEditDialog(s,{updated->repo.saveStrategy(current?.botNumber?:1,updated);refresh();notice="✓ تم تحديث الاستراتيجية ${updated.number}";editStrategy=null},{editStrategy=null})}
}

@Composable private fun Header(current:AmarSavedBot?,onBackHome:()->Unit){
    Row(Modifier.fillMaxWidth().background(PANEL).padding(9.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
        Button(onClick=onBackHome,colors=ButtonDefaults.buttonColors(containerColor=GOLD,contentColor=Color.Black)){Text("⌂")}
        Column(Modifier.weight(1f)){Text("AMAR BOT OS",color=CYAN,fontSize=20.sp,fontWeight=FontWeight.Black);Text("BOT ${current?.botNumber?:1} • ${current?.name?:"بوت 1"}",color=MUTED,fontSize=9.sp)}
        Pill("RUNTIME: UNKNOWN",GOLD)
    }
}

@Composable private fun NumericNavigation(page:MainPage,select:(MainPage)->Unit){
    Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=7.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){
        MainPage.values().forEach{p->Button(onClick={select(p)},modifier=Modifier.weight(1f).height(45.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=if(page==p)CYAN else PANEL2,contentColor=if(page==p)Color.Black else TEXT)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(p.number.toString(),fontSize=15.sp,fontWeight=FontWeight.Black);Text("${p.icon} ${p.title}",fontSize=8.sp,fontWeight=FontWeight.Bold)}}}
    }
}

@Composable private fun BotSelector(selected:Int,select:(Int)->Unit){
    Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){
        Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
            Text("تحديد البوت",color=GOLD,fontSize=16.sp,fontWeight=FontWeight.Black)
            Text("BOT 01 إلى BOT 10 — اختيار البوت لا ينفذ تداولًا بحد ذاته.",color=MUTED,fontSize=8.sp)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){(1..10).forEach{n->Button(onClick={select(n)},modifier=Modifier.weight(1f).height(36.dp),contentPadding=PaddingValues(0.dp),colors=ButtonDefaults.buttonColors(containerColor=if(selected==n)CYAN else PANEL2,contentColor=if(selected==n)Color.Black else TEXT),shape=RoundedCornerShape(8.dp)){Text("$n",fontSize=9.sp,fontWeight=FontWeight.Black)}}}
        }
    }
}

@Composable private fun StrategySelector(selected:Int,select:(Int)->Unit){
    Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){
        Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
            Text("تحديد الاستراتيجية",color=GOLD,fontSize=16.sp,fontWeight=FontWeight.Black)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){(1..10).forEach{n->Button(onClick={select(n)},modifier=Modifier.weight(1f).height(36.dp),contentPadding=PaddingValues(0.dp),colors=ButtonDefaults.buttonColors(containerColor=if(selected==n)CYAN else PANEL2,contentColor=if(selected==n)Color.Black else TEXT),shape=RoundedCornerShape(8.dp)){Text("$n",fontSize=9.sp,fontWeight=FontWeight.Black)}}}
        }
    }
}

@Composable private fun CommandPanel(last:String,send:(String)->Unit){
    Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){
        Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("التحكم بالبوت",color=GOLD,fontSize=17.sp,fontWeight=FontWeight.Black);Text(last,color=MUTED,fontSize=8.sp)}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("تشغيل",GREEN){send("START • طلب تشغيل BOT 1")};Cmd("إيقاف",GOLD){send("STOP • طلب إيقاف BOT 1")};Cmd("إغلاق شراء",RED){send("CLOSE_BUY • طلب إغلاق BUY لـ BOT 1")}}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("إغلاق بيع",RED){send("CLOSE_SELL • طلب إغلاق SELL لـ BOT 1")};Cmd("إغلاق الكل",RED){send("CLOSE_ALL • طلب إغلاق BOT 1")};Cmd("إعادة بناء",CYAN){send("REBUILD • طلب إعادة بناء BOT 1")}}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Cmd("Grid",CYAN){send("REBUILD_GRID • طلب إعادة بناء Grid")};Cmd("Tracking",CYAN){send("REBUILD_TRACKING • طلب إعادة بناء Tracking")};Cmd("طوارئ",RED){send("EMERGENCY_LOCK • طلب قفل الطوارئ")}}
            Text("الأزرار تمثل طلبات أوامر؛ الحالة الفعلية تأتي من MT5/runtime ولا تتغير من UI وحدها.",color=MUTED,fontSize=8.sp)
        }
    }
}

@Composable private fun RowScope.Cmd(text:String,color:Color,click:()->Unit)=Button(onClick=click,modifier=Modifier.weight(1f).height(40.dp),colors=ButtonDefaults.buttonColors(containerColor=color,contentColor=if(color==GOLD)Color.Black else Color.White),contentPadding=PaddingValues(2.dp)){Text(text,fontSize=8.sp,fontWeight=FontWeight.Black)}

@Composable private fun BotStatus(current:AmarSavedBot?,strategy:AmarSavedStrategy?){
    Card(colors=CardDefaults.cardColors(PANEL2),shape=RoundedCornerShape(20.dp)){
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("حالة البوت",color=CYAN,fontSize=19.sp,fontWeight=FontWeight.Black);Pill("UNKNOWN",GOLD)}
            Text("BOT ${current?.botNumber?:1} • Grid_Martingale_Basket_v2",color=TEXT,fontWeight=FontWeight.Bold)
            Text("BOT_1 • Magic 20260908 • v2.00 • ${strategy?.name?:"STRATEGY_01"}",color=MUTED,fontSize=9.sp)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Metric("Runtime","UNKNOWN",Modifier.weight(1f));Metric("Sync","UNKNOWN",Modifier.weight(1f));Metric("MT5","WAITING",Modifier.weight(1f))}
        }
    }
}

@Composable private fun MarketStatus(){
    Card(colors=CardDefaults.cardColors(PANEL2),shape=RoundedCornerShape(20.dp)){
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("حالة السوق",color=CYAN,fontSize=19.sp,fontWeight=FontWeight.Black);Text("XAUUSD • الذهب",color=MUTED,fontSize=9.sp)};Pill("WAITING DATA",GOLD)}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Metric("Bid","—",Modifier.weight(1f));Metric("Ask","—",Modifier.weight(1f));Metric("Spread","—",Modifier.weight(1f))}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Metric("الاتجاه","—",Modifier.weight(1f));Metric("التذبذب","—",Modifier.weight(1f));Metric("الجلسة","—",Modifier.weight(1f))}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){Metric("الفريم","M1 / M5",Modifier.weight(1f));Metric("آخر تحديث","—",Modifier.weight(1f));Metric("الاتصال","READ-ONLY",Modifier.weight(1f))}
        }
    }
}

@Composable private fun Settings(c:AmarBot1RuntimeConfig){
    Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){
        Column(Modifier.padding(11.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
            Text("الإعدادات",color=GOLD,fontSize=17.sp,fontWeight=FontWeight.Black)
            Key("اللوت الابتدائي",fmt(c.lot));Key("مسافة الشبكة",fmt(c.gridStep));Key("Max Orders",c.maxOrders.toString());Key("مضاعفة اللوت",fmt(c.multiplier));Key("Basket TP",money(c.basketTp));Key("Basket SL",money(c.basketSl));Key("Trailing",money(c.trailing));Key("BUY",if(c.buyEnabled)"ON" else "OFF");Key("SELL",if(c.sellEnabled)"ON" else "OFF")
        }
    }
}

@Composable private fun RuntimeSummary(current:AmarSavedBot?,strategy:AmarSavedStrategy?){
    val c=strategy?.profile?:AmarBot1RuntimeConfig()
    val data=listOf("الرصيد" to "—","Equity" to "—","الربح العائم" to "—","الصفقات المفتوحة" to "0","الأوامر المعلقة" to "0","متوسط السلة" to "—","التعرض" to "—","الاستراتيجية" to "${strategy?.number?:1}/10","البوت" to "${current?.botNumber?:1}/10","Reconciliation" to "UNKNOWN","Execution" to "BLOCKED/WAITING","Risk" to "—")
    Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(18.dp)){
        Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
            Text("تفاصيل التشغيل",color=GOLD,fontWeight=FontWeight.Black)
            data.chunked(2).forEach{pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){pair.forEach{(a,b)->Metric(a,b,Modifier.weight(1f))};if(pair.size==1)Spacer(Modifier.weight(1f))}}
            Text("Grid / Basket / Trailing / Entry تبقى وفق كود البوت الأصلي؛ الواجهة لا تعيد تعريف الاستراتيجية.",color=MUTED,fontSize=8.sp)
            Key("Lot Model","${fmt(c.lot)} × ${fmt(c.multiplier)}")
        }
    }
}

@Composable private fun Strategies(current:AmarSavedBot?,selected:Int,select:(Int)->Unit,edit:(AmarSavedStrategy)->Unit,save:(AmarSavedStrategy)->Unit,delete:(Int)->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(7.dp)){
        Text("الاستراتيجيات — 10 / 10",color=CYAN,fontSize=18.sp,fontWeight=FontWeight.Black)
        (1..10).forEach{n->
            val s=current?.strategies?.firstOrNull{it.number==n}
            Row(Modifier.fillMaxWidth().background(if(n==selected)PANEL2 else PANEL,RoundedCornerShape(14.dp)).border(1.dp,if(n==selected)CYAN else LINE,RoundedCornerShape(14.dp)).clickable{select(n)}.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                Text(if(s==null)"○" else "✓",color=if(s==null)MUTED else GREEN,fontWeight=FontWeight.Black);Spacer(Modifier.width(7.dp));Column(Modifier.weight(1f)){Text(s?.name?:"استراتيجية رقم $n",color=TEXT,fontWeight=FontWeight.Bold);Text(if(s==null)"غير محفوظة" else "محفوظة • ${s.riskProfile}",color=if(s==null)MUTED else GREEN,fontSize=8.sp)}
                if(s!=null){TextButton(onClick={edit(s)}){Text("تحرير",color=CYAN,fontSize=8.sp)};TextButton(onClick={delete(n)}){Text("حذف",color=RED,fontSize=8.sp)}}else{TextButton(onClick={save(AmarSavedStrategy(n,"استراتيجية رقم $n",AmarBot1RuntimeConfig()))}){Text("حفظ",color=GREEN,fontSize=8.sp)}}
                Text("$n",color=GOLD,fontWeight=FontWeight.Black)
            }
        }
        current?.strategies?.firstOrNull{it.number==selected}?.let{s->Card(colors=CardDefaults.cardColors(PANEL),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(10.dp)){Key("Risk",s.riskProfile);Key("Rebuild",s.rebuildRule);Key("Entry",s.entryRule);Key("Metadata",s.metadata)}}}
    }
}

@Composable private fun Vault(bots:List<AmarSavedBot>,selected:Int,select:(Int)->Unit,edit:(AmarSavedBot)->Unit,delete:(AmarSavedBot)->Unit,reset:(AmarSavedBot)->Unit,add:()->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(7.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("البوتات — 10 / 10",color=CYAN,fontSize=18.sp,fontWeight=FontWeight.Black);Button(onClick=add,colors=ButtonDefaults.buttonColors(containerColor=GREEN,contentColor=Color.Black)){Text("+ بوت",fontSize=9.sp)}}
        Text("BOT 1 هو البوت الحقيقي المعتمد؛ BOT 2–10 خانات إعداد/خزنة حتى يتم اعتماد محركاتها.",color=MUTED,fontSize=9.sp)
        (1..10).forEach{n->
            val b=bots.firstOrNull{it.botNumber==n}
            Row(Modifier.fillMaxWidth().background(if(n==selected)PANEL2 else PANEL,RoundedCornerShape(15.dp)).border(1.dp,if(n==selected)CYAN else LINE,RoundedCornerShape(15.dp)).clickable{select(n)}.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text(b?.name?:"بوت $n",color=TEXT,fontWeight=FontWeight.Black);Text("${b?.strategies?.size?:0}/10 استراتيجيات محفوظة",color=MUTED,fontSize=8.sp)}
                if(b!=null){TextButton(onClick={edit(b)}){Text("تعديل",color=CYAN,fontSize=8.sp)};TextButton(onClick={reset(b)}){Text("تفريغ",color=GOLD,fontSize=8.sp)};if(b.botNumber!=1)TextButton(onClick={delete(b)}){Text("حذف",color=RED,fontSize=8.sp)}}
                Pill(if(n==1)"REAL BOT" else "VAULT",if(n==1)GREEN else GOLD)
            }
        }
    }
}

@Composable private fun BotEditDialog(bot:AmarSavedBot,save:(String)->Unit,cancel:()->Unit){var name by remember(bot){mutableStateOf(bot.name)};AlertDialog(onDismissRequest=cancel,title={Text("تحرير ${bot.name}")},text={OutlinedTextField(value=name,onValueChange={name=it},label={Text("اسم البوت")},singleLine=true)},confirmButton={TextButton(onClick={save(name)}){Text("حفظ",color=GREEN)}},dismissButton={TextButton(onClick=cancel){Text("إلغاء")}})}

@Composable private fun StrategyEditDialog(strategy:AmarSavedStrategy,save:(AmarSavedStrategy)->Unit,cancel:()->Unit){
    var name by remember(strategy){mutableStateOf(strategy.name)};var risk by remember(strategy){mutableStateOf(strategy.riskProfile)};var rebuild by remember(strategy){mutableStateOf(strategy.rebuildRule)};var entry by remember(strategy){mutableStateOf(strategy.entryRule)};var metadata by remember(strategy){mutableStateOf(strategy.metadata)}
    AlertDialog(onDismissRequest=cancel,title={Text("تحرير الاستراتيجية ${strategy.number}")},text={Column(verticalArrangement=Arrangement.spacedBy(6.dp)){OutlinedTextField(name,{name=it},label={Text("الاسم")},singleLine=true);OutlinedTextField(risk,{risk=it},label={Text("Risk")},singleLine=true);OutlinedTextField(rebuild,{rebuild=it},label={Text("Rebuild")},singleLine=true);OutlinedTextField(entry,{entry=it},label={Text("Entry")},singleLine=true);OutlinedTextField(metadata,{metadata=it},label={Text("ملاحظات")})}},confirmButton={TextButton(onClick={save(strategy.copy(name=name.ifBlank{strategy.name},riskProfile=risk,rebuildRule=rebuild,entryRule=entry,metadata=metadata))}){Text("حفظ",color=GREEN)}},dismissButton={TextButton(onClick=cancel){Text("إلغاء")}})
}

@Composable private fun Metric(a:String,b:String,m:Modifier){Column(m.background(PANEL2,RoundedCornerShape(9.dp)).padding(7.dp)){Text(a,color=MUTED,fontSize=8.sp);Text(b,color=TEXT,fontSize=11.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun Key(a:String,b:String){Row(Modifier.fillMaxWidth().padding(vertical=2.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a,color=MUTED,fontSize=9.sp);Text(b,color=TEXT,fontSize=9.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun Pill(t:String,c:Color){Text(t,color=c,fontSize=8.sp,fontWeight=FontWeight.Black,modifier=Modifier.background(c.copy(alpha=.12f),RoundedCornerShape(20.dp)).padding(horizontal=8.dp,vertical=5.dp))}
private fun fmt(v:Double)=String.format(Locale.US,"%.2f",v)
private fun money(v:Double)=String.format(Locale.US,"%.2f $",v)
