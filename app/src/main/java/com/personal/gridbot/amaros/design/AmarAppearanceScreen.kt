package com.personal.gridbot.amaros.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private data class AppearancePreset(val id:Int,val name:String,val colors:List<Color>)

@Composable
fun AmarAppearanceScreen(){
    var selected by remember{mutableIntStateOf(0)}
    val presets=listOf(
        AppearancePreset(0,"أسود ذهبي سماوي",listOf(Color(0xFF05070B),Color(0xFFD8A83A),Color(0xFF00D9FF))),
        AppearancePreset(1,"ليل سماوي",listOf(Color(0xFF06121A),Color(0xFF00D9FF),Color(0xFF6C63FF))),
        AppearancePreset(2,"أسود بنفسجي",listOf(Color(0xFF07050D),Color(0xFF9C5CFF),Color(0xFFFF4FD8))),
        AppearancePreset(3,"أسود أخضر",listOf(Color(0xFF040A08),Color(0xFF00E6A0),Color(0xFFB7FF4A)))
    )
    LazyColumn(Modifier.fillMaxSize().background(Color(0xFF05070B)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{Text("غرفة المظهر",color=Color(0xFFFFE69A),fontSize=27.sp,fontWeight=FontWeight.Black);Text("اختر مظهر التطبيق. البنية جاهزة لإضافة 10 أو 20 أو 50 أو 100 خلفية لاحقًا.",color=Color(0xFF9BB6BE),fontSize=11.sp);Spacer(Modifier.height(8.dp))}
        items(presets){p->AppearanceCard(p,p.id==selected){selected=p.id}}
        item{Text("مساحة التوسعة",color=Color(0xFF00D9FF),fontWeight=FontWeight.Black,fontSize=15.sp);Text("يمكن إضافة الخلفيات المستقبلية كحزم مستقلة دون إعادة بناء بنية التطبيق.",color=Color(0xFF8CA8B1),fontSize=10.sp)}
    }
}

@Composable private fun AppearanceCard(p:AppearancePreset,selected:Boolean,onClick:()->Unit){
    Row(Modifier.fillMaxWidth().background(Color(0xFF0B1118),RoundedCornerShape(18.dp)).border(1.dp,if(selected)Color(0xFFD8A83A) else Color(0xFF23414B),RoundedCornerShape(18.dp)).clickable(onClick=onClick).padding(12.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)){
        p.colors.forEach{c->Spacer(Modifier.size(38.dp).background(c,RoundedCornerShape(10.dp)))}
        Column{Text(p.name,color=Color.White,fontWeight=FontWeight.Bold,fontSize=13.sp);Text(if(selected)"المظهر الحالي" else "اختيار المظهر",color=if(selected)Color(0xFFD8A83A) else Color(0xFF7E9AA3),fontSize=9.sp)}
    }
}
