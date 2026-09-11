package com.personal.gridbot.amaros.design

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.ui.theme.AmarThemeMode

private data class LayoutPreset(val id:Int,val name:String,val description:String,val colors:List<Color>)

@Composable
fun AmarAppearanceScreen(
    mode: AmarThemeMode,
    onModeChange: (AmarThemeMode)->Unit,
    layout: Int,
    onLayoutChange: (Int)->Unit
){
    val presets=listOf(
        LayoutPreset(1,"V1 — المركز الكروي","عمار في المنتصف + مدارات ودوائر محيطة",listOf(Color(0xFF19E6FF),Color(0xFFFFC84D))),
        LayoutPreset(2,"V2 — الزجاج الثلجي","واجهة Glassmorphism شفافة سماوية بنفسجية",listOf(Color(0xFFEAFBFF),Color(0xFF8C7CFF))),
        LayoutPreset(3,"V3 — المجرة المدارية","تكوين دائري عميق بعناصر موزعة حول المركز",listOf(Color(0xFF356CFF),Color(0xFFB46CFF))),
        LayoutPreset(4,"V4 — السماء اللامعة","واجهة فاتحة حية بطبقات زجاجية وردية وسماوية",listOf(Color(0xFFFF9DCC),Color(0xFF55DFFF))),
        LayoutPreset(5,"V5 — الماسة","تكوين هندسي مائل 45 درجة ومراكز دوارة",listOf(Color(0xFF00E6A0),Color(0xFFFFD15C))),
        LayoutPreset(6,"V6 — غرفة القيادة","شبكة قيادة عمودية وعناصر مستقلة",listOf(Color(0xFF49B9FF),Color(0xFF7D8CFF))),
        LayoutPreset(7,"V7 — النبض الأحمر","واجهة طاقة نابضة عالية التباين",listOf(Color(0xFFFF5B8A),Color(0xFFFFC84D))),
        LayoutPreset(8,"V8 — الجسر الجانبي","المركز يمين الشاشة والوظائف موزعة كجسر",listOf(Color(0xFF35E0FF),Color(0xFFB46CFF))),
        LayoutPreset(9,"V9 — الجليد الشفاف","واجهة زجاجية فاتحة شديدة الشفافية",listOf(Color(0xFF9AF4FF),Color(0xFFC9FFF0))),
        LayoutPreset(10,"V10 — ثلاثي الأبعاد","منظور 3D مائل مع طبقات عمق وحركة",listOf(Color(0xFF00D9FF),Color(0xFFB46CFF)))
    )
    val pulse=rememberInfiniteTransition(label="layoutPreview")
    val scale= pulse.animateFloat(.94f,1.06f,infiniteRepeatable(tween(1200),RepeatMode.Reverse),label="pulse").value
    LazyColumn(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{
            Text("واجهات عمار",fontSize=27.sp,fontWeight=FontWeight.Black,color=Color(0xFF19E6FF))
            Text("10 تصاميم مستقلة — اختيارك يغيّر البنية، وليس اللون فقط.",fontSize=11.sp,color=Color(0xFF7896A5))
        }
        item{
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf(AmarThemeMode.DARK to "داكن",AmarThemeMode.LIGHT to "فاتح",AmarThemeMode.AUTO to "تلقائي").forEach{(m,t)->
                    Box(Modifier.weight(1f).background(if(mode==m)Color(0xFF163746) else Color(0xFF0D202B),RoundedCornerShape(14.dp)).border(1.dp,if(mode==m)Color(0xFF19E6FF) else Color(0xFF214452),RoundedCornerShape(14.dp)).clickable{onModeChange(m)}.padding(9.dp),contentAlignment=Alignment.Center){Text(t,color=Color.White,fontSize=11.sp,fontWeight=FontWeight.Bold)}
                }
            }
        }
        items(presets){p->
            val selected=p.id==layout
            Row(Modifier.fillMaxWidth().background(Color(0xFF0D202B),RoundedCornerShape(20.dp)).border(1.dp,if(selected)Color(0xFFFFC84D) else Color(0xFF214452),RoundedCornerShape(20.dp)).clickable{onLayoutChange(p.id)}.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                Box(Modifier.size(58.dp).scale(if(selected)scale else 1f).background(Brush.radialGradient(p.colors+Color.Transparent),CircleShape),contentAlignment=Alignment.Center){Text("V${p.id}",color=Color.White,fontWeight=FontWeight.Black,fontSize=13.sp)}
                Column(Modifier.weight(1f)){Text(p.name,color=Color.White,fontWeight=FontWeight.Black,fontSize=14.sp);Text(p.description,color=Color(0xFF8EAAB4),fontSize=10.sp);Text(if(selected)"✓ الواجهة الحالية — حية وتفاعلية" else "اضغط للتفعيل والمعاينة",color=if(selected)Color(0xFF00E6A0) else Color(0xFF6E8A94),fontSize=9.sp)}
            }
        }
    }
}
