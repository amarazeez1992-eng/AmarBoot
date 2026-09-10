'use client';

import { useEffect, useMemo, useState } from 'react';
import { amarDemoStream, type AmarEvent } from '../lib/events';
import { AMAR_UI_MANIFEST } from '../lib/amarUiManifest';

const rooms = [['home','الرئيسية','⌂'],['market','السوق','◈'],['chart','الرسم','⌁'],['bot','البوت','◇'],['risk','المخاطر','△'],['analysis','التحليل','◎'],['performance','الأداء','↗'],['testing','الاختبار','◌'],['tools','الأدوات','⚙']];
const nodes = [
  {id:'market',label:'السوق',x:16,y:23,tone:'cyan',value:'XAUUSD'},
  {id:'chart',label:'الرسم البياني',x:84,y:23,tone:'gold',value:'M5'},
  {id:'bot',label:'البوت',x:14,y:70,tone:'green',value:'جاهز'},
  {id:'risk',label:'المخاطر',x:86,y:70,tone:'rose',value:'5%'},
  {id:'analysis',label:'التحليل',x:50,y:86,tone:'violet',value:'82%'}
];

export default function Page(){
 const [entered,setEntered]=useState(false),[room,setRoom]=useState('home'),[selected,setSelected]=useState('home');
 const [events,setEvents]=useState<AmarEvent[]>([]),[pulse,setPulse]=useState(62),[equity,setEquity]=useState(1000),[time,setTime]=useState('');
 useEffect(()=>{
   const onEvent=(event:AmarEvent)=>{setEvents(c=>[event,...c].slice(0,8));setPulse(Math.round(55+Math.random()*40));if(event.type==='ACCOUNT_UPDATE'&&typeof event.payload.equity==='number')setEquity(event.payload.equity)};
   amarDemoStream.connect(onEvent); const timer=window.setInterval(()=>setTime(new Date().toLocaleTimeString('ar-IQ')),1000);
   return ()=>{amarDemoStream.disconnect(onEvent);window.clearInterval(timer)};
 },[]);
 const activeNode=useMemo(()=>nodes.find(n=>n.id===selected),[selected]);
 if(!entered)return <main className="portal" dir="rtl"><div className="aurora aurora-a"/><div className="aurora aurora-b"/><div className="portal-orbit orbit-one"/><div className="portal-orbit orbit-two"/><section className="portal-core"><div className="amar-emblem"><span>ع</span></div><div className="eyebrow">منصة عمار الذكية للتداول</div><h1>المحور <strong>الحي</strong></h1><p>مركز قيادة تفاعلي للأسواق والبوتات والتحليل والمخاطر — يعمل الآن في الوضع التجريبي الآمن.</p><button onClick={()=>setEntered(true)}>دخول إلى المحور <span>←</span></button><div className="safe-line"><i/> تجريبي · آمن · لا يوجد تنفيذ حقيقي</div></section><small className="portal-meta">AMAR UI · {AMAR_UI_MANIFEST.version} · {time||'جاهز'}</small></main>;
 return <main className="amar-app" dir="rtl">
  <header className="topbar glass"><div className="brand"><div className="brand-emblem">ع</div><div><b>عمار</b><span>المحور الذكي</span></div></div><div className="quote"><span>الذهب XAUUSD</span><strong>3472.18</strong><em>+0.42%</em></div><div className="top-status"><span className="demo"><i/> تجريبي</span><span>{time}</span></div></header>
  <div className="layout"><aside className="rail glass">{rooms.map(([id,label,icon])=><button key={id} className={room===id?'active':''} onClick={()=>{setRoom(id);setSelected(id)}}><b>{icon}</b><span>{label}</span></button>)}</aside>
   <section className="workspace"><div className="workspace-head"><div><small>المحور الحي</small><h2>{activeNode?.label||'مركز القيادة'}</h2></div><div className="chips"><span>الحساب التجريبي</span><span>الحارس مفعل</span><span>الإضاءة عالية</span></div></div>
    <div className="living-stage glass"><div className="stage-grid"/><div className="world-ring ring-1"/><div className="world-ring ring-2"/><div className="world-ring ring-3"/><div className="energy-beam beam-1"/><div className="energy-beam beam-2"/><div className="core3d"><div className="core-glow"/><div className="core-sphere"><span>عمار</span><b>{pulse}%</b><small>نشاط المحور</small></div></div>
      {nodes.map(node=><button key={node.id} className={`node node-${node.tone} ${selected===node.id?'selected':''}`} style={{left:`${node.x}%`,top:`${node.y}%`}} onClick={()=>setSelected(node.id)}><i/><b>{node.label}</b><small>{node.value}</small></button>)}
      <div className="stage-hint">اسحب · المس · استكشف — كل عقدة تفتح غرفة مستقلة</div></div>
    <div className="metrics"><Metric title="قيمة الحساب" value={`$${equity.toFixed(2)}`} sub="تجريبي"/><Metric title="نشاط عمار" value={`${pulse}%`} sub="حي الآن"/><Metric title="التراجع" value="2.10%" sub="ضمن الحدود"/><Metric title="المراكز" value="06" sub="محاكاة"/></div>
    <div className="lower-grid"><section className="panel glass"><header><b>تيار الأحداث</b><span>مباشر</span></header>{events.length===0?<div className="empty">بانتظار أحداث المحاكاة…</div>:events.slice(0,5).map((e,i)=><div className="event" key={`${e.ts}-${i}`}><i/><b>{e.type.replaceAll('_',' ')}</b><small>{Object.values(e.payload).slice(0,1).map(String).join('')}</small></div>)}</section><section className="panel glass"><header><b>حالة الحماية</b><span className="green">مفعلة</span></header><div className="protection"><div className="protection-orb">✓</div><div><strong>الحارس المركزي</strong><p>التداول الحقيقي مغلق. الواجهة لا تتخذ قرار شراء أو بيع.</p></div></div><div className="bars"><span><b>المخاطر</b><i style={{width:'18%'}}/></span><span><b>التعرض</b><i style={{width:'31%'}}/></span><span><b>الاستقرار</b><i style={{width:'86%'}}/></span></div></section></div>
   </section></div><footer><span>AMAR · {AMAR_UI_MANIFEST.version}</span><span>الوضع: تجريبي / محلي / محاكاة</span><span>المحرك: Kotlin + Godot 3D + React/Three</span></footer></main>;
}
function Metric({title,value,sub}:{title:string;value:string;sub:string}){return <div className="metric glass"><small>{title}</small><strong>{value}</strong><span>{sub}</span></div>}
