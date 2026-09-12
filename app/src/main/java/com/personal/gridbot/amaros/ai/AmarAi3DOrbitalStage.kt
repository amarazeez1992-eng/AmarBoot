package com.personal.gridbot.amaros.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val StageBg=Color(0xFF03070B);private val StageCyan=Color(0xFF1DE5FF);private val StageGold=Color(0xFFFFC84A);private val StageWhite=Color(0xFFEFFFFF)

@Composable
fun AmarAi3DOrbitalStage(modifier:Modifier=Modifier){
 val transition=rememberInfiniteTransition(label="amar_ai_orbital");val t by transition.animateFloat(0f,1f,infiniteRepeatable(tween(12000,easing=FastOutSlowInEasing),RepeatMode.Restart),label="orbit");val bob by transition.animateFloat(-1f,1f,infiniteRepeatable(tween(2300,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="robot_bob");val pulse by transition.animateFloat(.72f,1f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="robot_pulse");val newsAngle=t*6.2831855f
 Box(modifier.fillMaxWidth().height(250.dp),contentAlignment=Alignment.TopCenter){Canvas(Modifier.matchParentSize().padding(horizontal=8.dp)){val cx=size.width/2f;val cy=size.height*.48f+bob*5f;val rx=size.width*.34f;val ry=size.height*.20f;drawRect(Brush.verticalGradient(listOf(StageBg,Color(0xFF07131A),StageBg)));drawCircle(StageCyan.copy(alpha=.06f),size.minDimension*.30f,Offset(cx,cy));drawCircle(StageGold.copy(alpha=.035f),size.minDimension*.40f,Offset(cx,cy));for(i in 0..2){val scale=1f+i*.14f;val oval=Rect(cx-rx*scale,cy-ry*scale,cx+rx*scale,cy+ry*scale);drawOval(Brush.linearGradient(listOf(StageCyan.copy(alpha=.10f/scale),StageGold.copy(alpha=.04f/scale))),oval.topLeft,oval.size,style=Stroke(width=1.2f+i*.45f))};fun orbitPoint(angle:Float,scale:Float)=Offset(cx+rx*scale*cos(angle),cy+ry*scale*sin(angle));listOf(orbitPoint(newsAngle,1f),orbitPoint(newsAngle+2.094f,.88f),orbitPoint(newsAngle+4.188f,1.08f)).forEachIndexed{index,p->drawCircle(if(index==1)StageGold else StageCyan,4f+pulse*2f,p);drawCircle(StageWhite.copy(alpha=.55f),1.5f,p)}
 val headW=size.width*.16f;val headH=size.height*.18f;val head=Rect(cx-headW/2,cy-size.height*.28f,cx+headW/2,cy-size.height*.10f);val headRadius=CornerRadius(.18f*headW,.18f*headH);drawRoundRect(Brush.radialGradient(listOf(Color(0xFF183746),Color(0xFF071017))),head.topLeft,head.size,headRadius);drawRoundRect(StageCyan.copy(alpha=.70f),head.topLeft,head.size,headRadius,style=Stroke(2.2f));drawCircle(StageCyan.copy(alpha=.28f),headW*.48f,Offset(cx,head.center.y));drawCircle(StageWhite.copy(alpha=pulse),headW*.045f,Offset(cx-headW*.18f,head.center.y));drawCircle(StageWhite.copy(alpha=pulse),headW*.045f,Offset(cx+headW*.18f,head.center.y));drawLine(StageGold,Offset(cx,head.top-headH*.16f),Offset(cx,head.top-headH*.42f),2f,cap=StrokeCap.Round);drawCircle(StageGold.copy(alpha=pulse),4f,Offset(cx,head.top-headH*.46f))
 val bodyW=size.width*.21f;val bodyH=size.height*.22f;val body=Rect(cx-bodyW/2,cy-size.height*.06f,cx+bodyW/2,cy+size.height*.16f);drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF173A47),Color(0xFF061016))),body.topLeft,body.size,CornerRadius(24f,24f));drawRoundRect(StageGold.copy(alpha=.58f),body.topLeft,body.size,CornerRadius(24f,24f),style=Stroke(2f));val core=Rect(body.left+bodyW*.12f,body.top+bodyH*.16f,body.right-bodyW*.12f,body.bottom-bodyH*.16f);drawRoundRect(StageCyan.copy(alpha=.22f),core.topLeft,core.size,CornerRadius(14f,14f),style=Stroke(1.2f));val armY=body.center.y-2f;drawLine(StageCyan.copy(alpha=.8f),Offset(body.left,armY),Offset(body.left-bodyW*.28f,armY+18f),6f,cap=StrokeCap.Round);drawLine(StageCyan.copy(alpha=.8f),Offset(body.right,armY),Offset(body.right+bodyW*.28f,armY+18f),6f,cap=StrokeCap.Round);drawCircle(StageGold,5f,Offset(body.left-bodyW*.28f,armY+18f));drawCircle(StageGold,5f,Offset(body.right+bodyW*.28f,armY+18f));val legY=body.bottom;drawLine(StageCyan.copy(alpha=.65f),Offset(cx-bodyW*.23f,legY),Offset(cx-bodyW*.23f,legY+22f),7f,cap=StrokeCap.Round);drawLine(StageCyan.copy(alpha=.65f),Offset(cx+bodyW*.23f,legY),Offset(cx+bodyW*.23f,legY+22f),7f,cap=StrokeCap.Round)};Text("AMAR AI  •  المشرف الذكي",color=StageCyan.copy(alpha=.82f),fontSize=9.sp,fontWeight=FontWeight.Black,modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=6.dp))}
}
