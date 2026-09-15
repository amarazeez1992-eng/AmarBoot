'use client';

import { useState } from 'react';
import { Bot, BrainCircuit, ChartNoAxesCombined, ChevronLeft, Globe2, Settings2, ShieldCheck, Sparkles } from 'lucide-react';
import Item10Workspace from './amar-ui';
import { createAmarEngineBridge } from './amar-ui/amarEngineBridge';

const bridge = createAmarEngineBridge();

export default function HomeWorkspace() {
  const [agentOpen, setAgentOpen] = useState(false);
  const [pulse, setPulse] = useState('جاهز');

  if (agentOpen) {
    return <Item10Workspace bridge={bridge} />;
  }

  const openAgent = () => { setPulse('AMAR AI Agent متصل'); setAgentOpen(true); };

  return (
    <main className="amar-home quantum-app theme-obsidian" dir="rtl">
      <div className="q-ambient ambient-a" /><div className="q-ambient ambient-b" /><div className="q-grid" />
      <header className="q-topbar glass">
        <div className="q-brand"><div className="q-logo"><span>A</span><i /></div><div><strong>AMAR AI</strong><small>QUANTUM HOME · LOCAL INTELLIGENCE</small></div></div>
        <div className="q-top-center"><span className="status-dot ok" /><b>{pulse}</b><em>·</em><span>Gemini غير متصل</span></div>
        <div className="q-top-actions"><button onClick={openAgent} aria-label="فتح الوكيل"><Bot size={17}/></button></div>
      </header>

      <section className="q-heading glass">
        <div><div className="eyebrow"><Sparkles size={13}/> AMAR CORE</div><h1>مركز القيادة الذكي</h1><p>واجهة AMAR الرئيسية الجديدة. كل طلب يمكن أن ينتقل إلى الوكيل الداخلي، حيث تُجمع الأدلة وتُشغّل محركات AMAR المحلية ضمن حدود الصلاحيات.</p><button className="home-agent-button" onClick={openAgent}><Bot size={18}/> فتح Amar AI Agent <ChevronLeft size={17}/></button></div>
        <div className="core-orb"><span>A</span><small>LOCAL CORE</small><i className="core-ring ring-1"/><i className="core-ring ring-2"/><i className="core-ring ring-3"/></div>
      </section>

      <section className="home-grid">
        <button className="module-card glass" onClick={openAgent}><span className="module-icon"><Bot size={20}/></span><div><small>AGENT</small><b>Amar AI Agent</b><p>المسار المركزي للأسئلة والتحليل والأوامر الموجهة للتطبيق.</p></div><ChevronLeft size={17}/></button>
        <button className="module-card glass" onClick={openAgent}><span className="module-icon"><ChartNoAxesCombined size={20}/></span><div><small>INTELLIGENCE</small><b>السوق والتحقق</b><p>تحليل السوق، الدقة، عدم اليقين، التحقق والاختبارات.</p></div><ChevronLeft size={17}/></button>
        <button className="module-card glass" onClick={openAgent}><span className="module-icon"><Globe2 size={20}/></span><div><small>RESEARCH</small><b>البحث والمصادر</b><p>بحث مقيد أو مفتوح مع فصل الأدلة عن الفرضيات.</p></div><ChevronLeft size={17}/></button>
        <button className="module-card glass" onClick={openAgent}><span className="module-icon"><BrainCircuit size={20}/></span><div><small>MEMORY</small><b>الذاكرة والمعرفة</b><p>الوصول إلى طبقات المعرفة والذاكرة من خلال الوكيل.</p></div><ChevronLeft size={17}/></button>
        <button className="module-card glass" onClick={openAgent}><span className="module-icon"><Settings2 size={20}/></span><div><small>CONTROL</small><b>التحكم بالتطبيق</b><p>الوكيل يرسل أوامر التطبيق المسموح بها عبر حدود Android.</p></div><ChevronLeft size={17}/></button>
        <button className="module-card glass" onClick={openAgent}><span className="module-icon"><ShieldCheck size={20}/></span><div><small>SAFETY</small><b>الحماية والتدقيق</b><p>فشل مغلق، أدلة قابلة للتتبع، ولا تجاوز لصلاحيات النظام.</p></div><ChevronLeft size={17}/></button>
      </section>

      <section className="progress-panel glass"><div className="panel-title"><b>AMAR ENGINE PIPELINE</b><span>Provider-neutral · Gemini غير مستخدم</span></div><div className="progress-steps expanded-safe"><div className="done"><span>1</span><b>فهم</b><i/></div><div className="done"><span>2</span><b>تخطيط</b><i/></div><div className="done"><span>3</span><b>محركات</b><i/></div><div><span>4</span><b>أدلة</b><i/></div><div><span>5</span><b>تعارض</b><i/></div><div><span>6</span><b>تحقق</b><i/></div><div><span>7</span><b>نتيجة</b></div></div></section>
      <footer className="q-footer"><span>AMAR AI · LOCAL AUTHORITY</span><span>واجهة رئيسية جديدة → وكيل AMAR الداخلي</span><span>Gemini: DISCONNECTED</span></footer>
    </main>
  );
}
