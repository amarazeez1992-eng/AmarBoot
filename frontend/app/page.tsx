'use client';

import { useState } from 'react';

const nav = [
  ['home', 'الرئيسية'], ['assistant', 'الإيجنت'], ['research', 'البحث'],
  ['workspace', 'مساحة العمل'], ['files', 'الملفات'], ['memory', 'الذاكرة'], ['settings', 'الإعدادات'],
] as const;

type View = typeof nav[number][0];

const viewCopy: Record<View, { eyebrow: string; title: string; text: string }> = {
  home: { eyebrow: 'AMAR AI · HOME', title: 'مركز الذكاء', text: 'واجهة موحّدة ونظيفة للوصول إلى قدرات AMAR AI دون عناصر زخرفية مشتتة.' },
  assistant: { eyebrow: 'AMAR AI · AGENT', title: 'الإيجنت', text: 'المحادثة والتحليل هنا؛ سلطة القرار والتنفيذ تبقى خارج طبقة العرض.' },
  research: { eyebrow: 'AMAR AI · RESEARCH', title: 'البحث والتحقق', text: 'مسار مخصص للبحث، جمع الأدلة، والتحقق قبل عرض النتيجة.' },
  workspace: { eyebrow: 'AMAR AI · WORKSPACE', title: 'مساحة العمل', text: 'مكان منظم للملفات والنتائج والمخرجات المرتبطة بالمهمة.' },
  files: { eyebrow: 'AMAR AI · FILES', title: 'الملفات', text: 'إدارة الملفات دون تنفيذ تلقائي؛ الرفع والتخزين منفصلان عن التنفيذ.' },
  memory: { eyebrow: 'AMAR AI · MEMORY', title: 'الذاكرة', text: 'عرض الذاكرة المؤكدة فقط، دون اختلاق بيانات أو ادعاء تذكّر غير موجود.' },
  settings: { eyebrow: 'AMAR AI · SETTINGS', title: 'الإعدادات', text: 'تخصيص مظهر الواجهة وسلوك العرض دون تغيير عقود المحرك.' },
};

export default function Page() {
  const [entered, setEntered] = useState(false);
  const [view, setView] = useState<View>('home');
  const [prompt, setPrompt] = useState('');
  const [messages, setMessages] = useState<string[]>([]);
  const current = viewCopy[view];

  const send = () => {
    const value = prompt.trim();
    if (!value) return;
    setMessages((items) => [...items, `أنت: ${value}`, 'AMAR AI: تم استلام الطلب — التحقق والقرار خارج طبقة الواجهة.']);
    setPrompt('');
    setView('assistant');
  };

  if (!entered) return (
    <main className="amar-entry" dir="rtl">
      <div className="entry-panel">
        <div className="entry-kicker"><span /> AMAR AI <span /></div>
        <h1>ذكاء يعمل معك.</h1>
        <p>مساحة Agent حديثة وهادئة، مبنية حول المحادثة والعمل والبحث — بدون الواجهة الدائرية القديمة أو عناصر مختبر البوتات.</p>
        <button onClick={() => setEntered(true)}>الدخول إلى AMAR AI <b>←</b></button>
        <div className="entry-meta"><span>AGENT WORKSPACE</span><span>FAIL-CLOSED</span><span>NO AUTO-EXECUTION</span></div>
      </div>
    </main>
  );

  return (
    <main className="amar-app" dir="rtl">
      <header className="amar-top">
        <div className="brand-block">
          <strong>AMAR AI</strong>
          <span>INTELLIGENCE WORKSPACE</span>
        </div>
        <div className="top-actions">
          <span className="secure-dot" />
          <span>النظام جاهز</span>
          <button aria-label="الإعدادات" onClick={() => setView('settings')}>⚙</button>
        </div>
      </header>

      <div className="amar-shell">
        <aside className="amar-nav" aria-label="التنقل الرئيسي">
          <div className="nav-title">المساحة</div>
          {nav.map(([id, label]) => (
            <button key={id} className={view === id ? 'active' : ''} onClick={() => setView(id)}>
              <span className={`nav-icon nav-${id}`} aria-hidden="true" />
              <span>{label}</span>
            </button>
          ))}
          <div className="nav-foot"><span className="secure-dot" /> طبقة العرض فقط</div>
        </aside>

        <section className="amar-content">
          <div className="hero-row">
            <div className="hero-copy">
              <div className="eyebrow">{current.eyebrow}</div>
              <h1>{current.title}</h1>
              <p>{current.text}</p>
            </div>
            <div className="hero-mark"><span>AI</span><small>AMAR</small></div>
          </div>

          {view === 'home' && (
            <div className="home-grid">
              <button className="feature-card feature-main" onClick={() => setView('assistant')}>
                <div className="card-label">AGENT</div><h2>ابدأ محادثة جديدة</h2><p>اسأل، حلّل، خطط، وراجع النتائج من مساحة واحدة.</p><span className="card-arrow">←</span>
              </button>
              <button className="feature-card" onClick={() => setView('research')}>
                <div className="card-label">RESEARCH</div><h3>بحث موثّق</h3><p>انتقل إلى مسار البحث والتحقق.</p><span className="card-arrow">←</span>
              </button>
              <button className="feature-card" onClick={() => setView('workspace')}>
                <div className="card-label">WORKSPACE</div><h3>مساحة العمل</h3><p>نظّم الملفات والنتائج في مكان واحد.</p><span className="card-arrow">←</span>
              </button>
            </div>
          )}

          {view === 'assistant' && (
            <div className="assistant-panel">
              <div className="panel-head"><div><span className="live-line" /> AGENT ONLINE</div><small>Proposal / Verification / Execution boundaries preserved</small></div>
              <div className="amar-messages">{messages.length ? messages.map((m, i) => <div key={i} className={m.startsWith('أنت:') ? 'user-msg' : 'ai-msg'}>{m}</div>) : <div className="empty-state"><strong>مرحباً.</strong><span>ما الذي تريد أن نعمل عليه؟</span></div>}</div>
              <div className="amar-composer"><textarea value={prompt} onChange={(e) => setPrompt(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); } }} placeholder="اكتب طلبك هنا…"/><button onClick={send}>إرسال <b>←</b></button></div>
            </div>
          )}

          {view !== 'home' && view !== 'assistant' && (
            <div className="module-panel"><div className="module-title">{current.title}</div><div className="module-line" /><p>{current.text}</p><div className="module-state">MODULE READY · AWAITING AUTHORIZED CONNECTION</div></div>
          )}
        </section>
      </div>
    </main>
  );
}
