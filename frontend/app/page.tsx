'use client';

import { useState } from 'react';

const nav = [
  ['home', 'المحور'], ['assistant', 'AMAR Agent'], ['research', 'البحث'],
  ['workspace', 'مساحة العمل'], ['files', 'الملفات'], ['memory', 'الذاكرة'], ['settings', 'الإعدادات'],
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
    setMessages((items) => [...items, `أنت: ${value}`, 'AMAR AI: الطلب داخل مسار الوكيل المركزي — بانتظار النتيجة الموثقة.']);
    setPrompt('');
    setView('assistant');
  };

  if (!entered) return (
    <main className="amar-entry" dir="rtl">
      <div className="amar-orbit"><div className="amar-orbit-ring"/><b>AMAR</b></div>
      <div className="amar-kicker">INTELLIGENCE WORKSPACE</div>
      <h1>AMAR AI</h1>
      <p>واجهة ثلاثية الأبعاد ديناميكية لمساحة الذكاء المركزية. العرض والتفاعل مرتبطان بحدود الوكيل والمحركات والأدلة.</p>
      <button onClick={() => setEntered(true)}>دخول إلى مساحة AMAR AI <span>↗</span></button>
      <small>● Fail-Closed · Agent-Centric · Evidence First</small>
    </main>
  );

  return (
    <main className="amar-app" dir="rtl">
      <div className="amar-particle-field" aria-hidden="true" />
      <header className="amar-top">
        <div className="amar-brand"><div className="amar-brand-orb">A</div><div><strong>AMAR AI</strong><small>Central Agent Workspace</small></div></div>
        <div className="amar-status"><i/> جاهز</div>
      </header>
      <div className="amar-layout">
        <aside className="amar-nav">
          <div className="amar-nav-title">SYSTEM</div>
          {nav.map(([id, label]) => <button key={id} className={view === id ? 'active' : ''} onClick={() => setView(id)}><span>{label}</span><b>{id === 'assistant' ? 'AI' : '•'}</b></button>)}
        </aside>
        <section className="amar-content">
          <div className="amar-hero">
            <div><div className="amar-kicker">AMAR AI / {view.toUpperCase()}</div><h2>{view === 'home' ? 'مركز الذكاء' : nav.find((x) => x[0] === view)?.[1]}</h2><p>طبقة تفاعل واحدة؛ القرار والذاكرة والأدلة تحت سلطة AMAR AI Agent.</p></div>
            <div className="amar-core"><span>AMAR</span><i/><em/></div>
          </div>
          {view === 'assistant' || view === 'home' ? <>
            <div className="amar-cards"><article><b>AGENT</b><strong>العقل المركزي</strong><span>توجيه الطلبات إلى الوكيل والمحركات المرتبطة به دون سلطة موازية.</span></article><article><b>EVIDENCE</b><strong>دليل قابل للتتبع</strong><span>الحالة تعرض ما تم إثباته فقط، مع إبقاء المجهول مجهولاً.</span></article><article><b>CONTROL</b><strong>حدود آمنة</strong><span>التنفيذ منفصل عن العرض ويظل محكوماً بالصلاحيات والتأكيد.</span></article></div>
            <div className="amar-chat"><div className="amar-chat-head"><span>AMAR AGENT</span><small>CONNECTED UI CHANNEL</small></div><div className="amar-messages">{messages.length ? messages.map((m, i) => <div key={i} className={i % 2 === 0 ? 'user' : ''}>{m}</div>) : <div>مرحباً. اكتب طلبك لبدء جلسة مع AMAR AI.</div>}</div><div className="amar-composer"><textarea value={prompt} onChange={(e) => setPrompt(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); } }} placeholder="اكتب طلبك للوكيل…"/><button onClick={send}>إرسال ↗</button></div></div>
          </> : <div className="amar-placeholder"><div className="amar-placeholder-core">{view === 'research' ? 'RESEARCH' : view === 'workspace' ? 'WORKSPACE' : view === 'files' ? 'FILES' : view === 'memory' ? 'MEMORY' : 'SETTINGS'}</div><p>{view === 'research' ? 'البحث والتحقق الموثق ضمن حدود المحرك.' : view === 'workspace' ? 'مساحة موحدة للمواد والمخرجات المرتبطة بالعقود.' : view === 'files' ? 'الملفات تدخل كبيانات؛ لا تنفيذ تلقائي.' : view === 'memory' ? 'الذاكرة لا تعرض إلا بيانات مؤكدة وقابلة للتتبع.' : 'إعدادات العرض والمؤثرات فقط.'}</p></div>}
        </section>
      </div>
    </main>
  );
}
