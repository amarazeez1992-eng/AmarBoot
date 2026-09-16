'use client';

import { useState } from 'react';

const nav = [
  ['home', 'المحور'], ['assistant', 'AI Assistant'], ['research', 'Research'],
  ['workspace', 'Workspace'], ['files', 'Files'], ['memory', 'Memory'], ['settings', 'Settings'],
] as const;

type View = typeof nav[number][0];

export default function Page() {
  const [entered, setEntered] = useState(false);
  const [view, setView] = useState<View>('home');
  const [prompt, setPrompt] = useState('');
  const [messages, setMessages] = useState<string[]>([]);

  const send = () => {
    const value = prompt.trim();
    if (!value) return;
    setMessages((items) => [...items, `أنت: ${value}`, 'AMAR AI: طلب مستلم — التحقق والقرار يبقيان خارج طبقة العرض.']);
    setPrompt('');
    setView('assistant');
  };

  if (!entered) return (
    <main className="amar-entry" dir="rtl">
      <div className="amar-orbit"><b>AMAR</b></div>
      <h1>AMAR AI</h1>
      <p>مساحة الذكاء الموحدة — واجهة العرض والتفاعل فقط، مع حدود واضحة للمحرك والحوكمة.</p>
      <button onClick={() => setEntered(true)}>دخول إلى مساحة AMAR AI ↗</button>
      <small>● Fail-Closed · لا Gemini · لا تنفيذ تداول من الواجهة</small>
    </main>
  );

  return (
    <main className="amar-app" dir="rtl">
      <header className="amar-top">
        <div className="amar-brand"><strong>AMAR AI</strong><small>Agent Workspace · UI Layer</small></div>
        <div className="amar-status">● جاهز</div>
      </header>
      <div className="amar-layout">
        <aside className="amar-nav">
          {nav.map(([id, label]) => <button key={id} className={view === id ? 'active' : ''} onClick={() => setView(id)}>{label}</button>)}
        </aside>
        <section className="amar-content">
          <div className="amar-hero"><div><small>AMAR AI / {view.toUpperCase()}</small><h2>{view === 'home' ? 'مساحة الذكاء الآمنة' : nav.find((x) => x[0] === view)?.[1]}</h2><p>الواجهة لا تنشئ سلطة موازية للمحرك ولا تدّعي تحققاً غير موجود.</p></div><div className="amar-core">AMAR</div></div>
          {view === 'assistant' || view === 'home' ? <>
            <div className="amar-cards"><article><b>Agent</b><span>المحرك المركزي صاحب القرار، والواجهة طبقة تفاعل فقط.</span></article><article><b>Evidence</b><span>النتيجة غير المؤكدة تبقى غير مؤكدة ولا تتحول إلى نجاح.</span></article><article><b>Security</b><span>الصوت والكاميرا ومشاركة الشاشة مؤجلة للربط المعتمد في Stage 10.</span></article></div>
            <div className="amar-chat"><div className="amar-messages">{messages.length ? messages.map((m, i) => <div key={i}>{m}</div>) : <div>مرحباً. اكتب طلبك لبدء مسار AMAR AI.</div>}</div><div className="amar-composer"><textarea value={prompt} onChange={(e) => setPrompt(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); } }} placeholder="اكتب طلبك…"/><button onClick={send}>إرسال</button></div></div>
          </> : <div className="amar-placeholder">{view === 'research' ? 'Research: البحث والتحقق الموثق يبقى تحت عقد المحرك.' : view === 'workspace' ? 'Workspace: مساحة العمل جاهزة للربط التدريجي بالعقود.' : view === 'files' ? 'Files: رفع الملف لا يعني تنفيذه.' : view === 'memory' ? 'Memory: لا تظهر ذاكرة مزعومة دون بيانات مؤكدة.' : 'Settings: إعدادات العرض فقط.'}</div>}
        </section>
      </div>
    </main>
  );
}
