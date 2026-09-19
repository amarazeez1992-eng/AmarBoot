package com.personal.gridbot.amaros.agent

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

interface AmarReasoning { suspend fun generate(context: AmarAgentContext): AmarAgentResponse }

class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
 override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
  val request=context.userText.substringBefore("\n\nEvidence summary:").trim()
  if(request.isBlank()) return AmarAgentResponse("اكتب طلبك وسأفهم المقصود وأحدد المسار المناسب له.")
  val q=normalize(request)
  val evidence=context.userText.substringAfter("Evidence summary:","").substringBefore("Stage 2 deliberation:").trim()
  val answer=when {
   any(q,"هلو","مرحبا","السلام عليكم","اهلا","hello","hi","hey") -> "أهلاً بك. أنا AMAR AI Agent. أفهم الطلب، أحدد مساره، أستخدم الأدلة والأدوات المتاحة، ثم أتحقق من النتيجة قبل عرضها."
   any(q,"من انت","عرف نفسك","ما اسمك","who are you","your name") -> "أنا AMAR AI، المحرك المركزي للمشروع. أعمل عبر التخطيط والبحث والأدلة والتحقق والنقد والقرار ثم صياغة الإجابة، دون الاعتماد على Gemini أو GPT كمحرك خارجي."
   any(q,"كم الوقت","الوقت الان","الساعة الان","what time","current time") -> "الوقت المحلي على الجهاز الآن: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss",Locale.getDefault()))+"."
   any(q,"كم التاريخ","التاريخ الان","التاريخ اليوم","what date","today date") -> "التاريخ المحلي على الجهاز الآن: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd",Locale.getDefault()))+"."
   any(q,"ما هو اليوم","اي يوم","what day","which day") -> "اليوم المحلي هو: "+LocalDateTime.now().dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL,Locale.getDefault())+"."
   any(q,"ماذا تستطيع","ما قدراتك","ماذا يمكنك","what can you do","capabilities") -> "قدراتي: فهم الطلب، التخطيط، البحث العام عند الحاجة، جمع الأدلة، التحقق، النقد وصياغة النتيجة. لا أنفذ صفقات حقيقية."
   evidence.lineSequence().any{it.trim().startsWith("source=")} -> researchAnswer(request,evidence)
   else -> "فهمت طلبك: "+request+"\n\nالمسار الداخلي: فهم المقصود → التخطيط → البحث عند الحاجة → الأدلة → التحقق → النقد → القرار → الإجابة.\nلا أختلق معلومة غير متاحة."
  }
  return AmarAgentResponse(answer=answer,actions=context.tools.map{it.id})
 }
 private fun researchAnswer(request:String,evidence:String):String {
  val rows=evidence.lineSequence().filter{it.trim().startsWith("source=")}.mapNotNull{val p=it.trim().removePrefix("source=").split(" | ",limit=3);if(p.size==3) Triple(p[0],p[1],p[2]) else null}.toList()
  if(rows.isEmpty()) return "فهمت طلبك: "+request+"\n\nتم البحث لكن لم تصل أدلة قابلة للعرض؛ لن أختلق نتيجة."
  return buildString{append("نتيجة البحث الداخلي: ").append(request).append("\n\nالأدلة التي وصلت فعلياً:");rows.take(6).forEachIndexed{i,r->append("\n\n").append(i+1).append(". ").append(r.first);if(r.third.isNotBlank())append(": ").append(r.third);append("\n").append(r.second)};append("\n\nعند نقص الأدلة أو تعارضها أبقي النتيجة غير مؤكدة بدلاً من اختلاقها.")}
 }
 private fun normalize(v:String)=v.lowercase(Locale.getDefault()).replace('أ','ا').replace('إ','ا').replace('آ','ا').replace('ة','ه').replace(Regex("[؟?!.,،؛:]+")," ").replace(Regex("\\s+")," ").trim()
 private fun any(t:String,vararg terms:String)=terms.any{t.contains(it)}
 override suspend fun respond(context:AmarAgentContext):AmarAgentResponse=generate(context)
}