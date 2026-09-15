'use client';

import { useMemo, useRef, useState } from 'react';
import {
  Activity, Archive, BarChart3, Bot, BrainCircuit, CheckCircle2, ChevronLeft, CircleHelp,
  ClipboardCheck, Cloud, Code2, Database, Eye, EyeOff, FileArchive, FileCode2, FileImage,
  FileSearch, FileText, FolderOpen, Gauge, Globe2, History, Image as ImageIcon, LayoutGrid,
  Link2, LockKeyhole, Menu, MessageSquare, Mic, MonitorUp, MoreHorizontal, Network, Paperclip,
  Play, Plus, Radar, RefreshCw, Search, Send, Settings2, ShieldCheck, Sparkles, Square,
  TerminalSquare, Upload, Video, Volume2, WandSparkles, X, Zap
} from 'lucide-react';
import type { AmarAgentBridge, AmarAgentResponse, AmarAgentStatus, AmarSource } from './amarAgentBridge';

type ViewId = 'home' | 'assistant' | 'research' | 'workspace' | 'files' | 'memory' | 'analytics' | 'settings';
type PanelId = 'core' | 'context' | 'sources' | 'progress' | 'composer' | 'activity';

const nav: { id: ViewId; label: string; icon: typeof Bot }[] = [
  { id: 'home', label: 'المحور', icon: LayoutGrid },
  { id: 'assistant', label: 'AI Assistant', icon: Bot },
  { id: 'research', label: 'Research', icon: Globe2 },
  { id: 'workspace', label: 'Workspace', icon: FolderOpen },
  { id: 'files', label: 'Files', icon: Paperclip },
  { id: 'memory', label: 'Memory', icon: BrainCircuit },
  { id: 'analytics', label: 'Analytics', icon: BarChart3 },
  { id: 'settings', label: 'Settings', icon: Settings2 },
];

const statusMeta: Record<AmarAgentStatus, { label: string; tone: string }> = {
  ready: { label: 'جاهز', tone: 'ok' }, thinking: { label: 'يفكر', tone: 'ai' }, searching: { label: 'يبحث', tone: 'wait' },
  analyzing: { label: 'يحلل', tone: 'ai' }, learning: { label: 'يتعلم', tone: 'ai' }, complete: { label: 'اكتمل', tone: 'ok' }, error: { label: 'خطأ', tone: 'danger' },
};

const defaultSources: AmarSource[] = [
  { id: 's1', title: 'مصدر الجلسة الأساسي', status: 'verified', confidence: 94, excerpt: 'مرجع موثوق مرتبط بسياق الطلب.' },
  { id: 's2', title: 'مصدر مقارن', status: 'checking', confidence: 78, excerpt: 'جارٍ فحص التطابق والتعارض.' },
  { id: 's3', title: 'سجل سابق', status: 'verified', confidence: 88, excerpt: 'معلومة محفوظة من مساحة العمل.' },
];

export default function QuantumWorkspace({ bridge }: { bridge?: AmarAgentBridge }) {
  const [view, setView] = useState<ViewId>('home');
  const [status, setStatus] = useState<AmarAgentStatus>('ready');
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState<AmarAgentResponse[]>([]);
  const [sources, setSources] = useState<AmarSource[]>(defaultSources);
  const [recording, setRecording] = useState(false);
  const [compactNav, setCompactNav] = useState(false);
  const [mobileNav, setMobileNav] = useState(false);
  const [panels, setPanels] = useState<Record<PanelId, boolean>>({ core: true, context: true, sources: true, progress: true, composer: true, activity: true });
  const [theme, setTheme] = useState<'aurora' | 'obsidian'>('aurora');
  const fileRef = useRef<HTMLInputElement>(null);

  const active = nav.find((item) => item.id === view) ?? nav[0];
  const ActiveIcon = active.icon;
  const meta = statusMeta[status];

  function togglePanel(id: PanelId) { setPanels((p) => ({ ...p, [id]: !p[id] })); }
  function toggleAll(value: boolean) { setPanels({ core: value, context: value, sources: value, progress: value, composer: value, activity: value }); }

  async function submitPrompt() {
    const prompt = message.trim();
    if (!prompt) return;
    setMessage('');
    setStatus('thinking');
    if (!bridge?.send) {
      setMessages((m) => [...m, { id: crypto.randomUUID(), text: `واجهة QUANTUM استلمت الطلب: ${prompt}`, status: 'complete', confidence: 92 }]);
      window.setTimeout(() => setStatus('complete'), 450);
      return;
    }
    try {
      const result = await bridge.send({ id: crypto.randomUUID(), prompt, mode: view });
      setMessages((m) => [...m, result]);
      if (result.sources) setSources(result.sources);
      setStatus(result.status ?? 'complete');
    } catch {
      setStatus('error');
      setMessages((m) => [...m, { id: crypto.randomUUID(), text: 'تعذر إكمال الطلب عبر الوكيل المتصل.', status: 'error' }]);
    }
  }

  async function runResearch() {
    setStatus('searching');
    if (bridge?.search) {
      try { setSources(await bridge.search(message || 'بحث جديد')); } catch { setStatus('error'); return; }
    }
    window.setTimeout(() => setStatus('analyzing'), 500);
    window.setTimeout(() => setStatus('complete'), 1000);
  }

  async function analyzeFile(file: File) {
    setStatus('analyzing');
    if (bridge?.analyzeFile) {
      try { setMessages((m) => [...m, await bridge.analyzeFile(file)]); setStatus('complete'); return; } catch { setStatus('error'); return; }
    }
    setMessages((m) => [...m, { id: crypto.randomUUID(), text: `تم تجهيز ${file.name} للتحليل. الربط التنفيذي غير متصل بعد.`, status: 'complete' }]);
    setStatus('complete');
  }

  const progress = useMemo(() => ({ analysis: status !== 'ready', verification: ['searching', 'analyzing', 'complete'].includes(status), sources: sources.length > 0, result: status === 'complete' }), [status, sources]);

  return (
    <main className={`quantum-app theme-${theme}`} dir="rtl">
      <div className="q-ambient ambient-a" /><div className="q-ambient ambient-b" /><div className="q-grid" />
      <header className="q-topbar glass">
        <div className="q-brand"><button className="q-logo" onClick={() => setView('home')}><span>Q</span><i /></button><div><strong>QUANTUM AI</strong><small>CORE v5.0 · ACTIVE</small></div></div>
        <div className="q-top-center"><span className={`status-dot ${meta.tone}`} /> <b>{meta.label}</b><em>·</em><span>AGENT-READY WORKSPACE</span></div>
        <div className="q-top-actions"><button onClick={() => toggleAll(true)} title="إظهار الكل"><Eye size={16} /></button><button onClick={() => toggleAll(false)} title="إخفاء الكل"><EyeOff size={16} /></button><button onClick={() => setTheme(theme === 'aurora' ? 'obsidian' : 'aurora')} title="المظهر"><WandSparkles size={16} /></button><button onClick={() => setMobileNav(!mobileNav)} className="mobile-only"><Menu size={18} /></button></div>
      </header>

      <div className="q-shell">
        <aside className={`q-sidebar glass ${compactNav ? 'collapsed' : ''} ${mobileNav ? 'mobile-open' : ''}`}>
          <div className="q-sidebar-head"><span>WORKSPACE</span><button onClick={() => setCompactNav(!compactNav)}><ChevronLeft size={15} /></button></div>
          <nav>{nav.map((item) => { const Icon = item.icon; return <button key={item.id} className={view === item.id ? 'active' : ''} onClick={() => { setView(item.id); setMobileNav(false); }}><Icon size={17} /><span>{item.label}</span></button>; })}</nav>
          <div className="q-sidebar-foot"><ShieldCheck size={14} /><span>Local UI boundary · safe by design</span></div>
        </aside>

        <section className="q-main">
          <div className="q-heading glass">
            <div><span className="eyebrow"><ActiveIcon size={13} /> QUANTUM CORE / {view.toUpperCase()}</span><h1>{view === 'home' ? 'مساحة الذكاء المتقدمة' : active.label}</h1><p>{view === 'home' ? 'تحليل، بحث، رؤية، صوت، ملفات وذاكرة ضمن نظام واجهة واحد قابل للربط بأي AI Agent.' : 'كل وحدة مستقلة بصريًا وقابلة للتوسعة دون كسر بقية النظام.'}</p></div>
            {panels.core && <div className="core-orb"><div className="core-ring ring-1" /><div className="core-ring ring-2" /><div className="core-ring ring-3" /><span>Q</span><small>{meta.label}</small></div>}
            <PanelToggle visible={panels.core} onClick={() => togglePanel('core')} />
          </div>

          {view === 'home' && <HomeView onNavigate={setView} />}
          {view === 'assistant' && <AssistantView messages={messages} />}
          {view === 'research' && <ResearchView sources={sources} onRun={runResearch} />}
          {view === 'workspace' && <WorkspaceView />}
          {view === 'files' && <FilesView onPick={() => fileRef.current?.click()} />}
          {view === 'memory' && <MemoryView />}
          {view === 'analytics' && <AnalyticsView />}
          {view === 'settings' && <SettingsView theme={theme} setTheme={setTheme} />}

          {panels.progress && <section className="progress-panel glass"><div className="panel-title"><b>SAFE OPERATIONAL PROGRESS</b><PanelToggle visible onClick={() => togglePanel('progress')} /></div><div className="progress-steps">{[['analysis','تحليل'],['verification','تحقق'],['sources','مصادر'],['result','نتيجة']].map(([key,label], i) => <div className={(progress as any)[key] ? 'done' : ''} key={key}><span>{(progress as any)[key] ? <CheckCircle2 size={14} /> : i + 1}</span><b>{label}</b>{i < 3 && <i />}</div>)}</div></section>}

          {panels.sources && <section className="sources-panel glass"><div className="panel-title"><div><b>EVIDENCE & SOURCES</b><small>{sources.length} مصادر · مقارنة وتعقب تعارض</small></div><PanelToggle visible onClick={() => togglePanel('sources')} /></div><div className="source-grid">{sources.map((s) => <div className="source-card" key={s.id}><div className="source-icon"><Globe2 size={15} /></div><div><b>{s.title}</b><p>{s.excerpt}</p><small className={`source-state ${s.status}`}>{s.status === 'verified' ? 'موثق' : s.status === 'conflict' ? 'تعارض' : 'قيد التحقق'} · {s.confidence ?? 0}%</small></div><MoreHorizontal size={15} /></div>)}</div></section>}

          {panels.composer && <section className="composer glass"><div className="composer-left"><button onClick={() => fileRef.current?.click()} title="رفع ملف"><Paperclip size={17} /></button><button onClick={() => setRecording(!recording)} className={recording ? 'recording' : ''} title="صوت">{recording ? <Square size={15} /> : <Mic size={17} />}</button><button title="رؤية"><ImageIcon size={17} /></button><button title="مشاركة الشاشة"><MonitorUp size={17} /></button></div><textarea value={message} onChange={(e) => setMessage(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); submitPrompt(); } }} placeholder="اكتب طلبًا لـ QUANTUM… حلل، ابحث، افحص، قارن، برمج أو أنشئ تقريرًا" rows={1} /><button className="send" onClick={submitPrompt}><Send size={16} /></button><PanelToggle visible onClick={() => togglePanel('composer')} /></section>}

          {panels.activity && <section className="activity-panel glass"><div className="panel-title"><b>SESSION ACTIVITY</b><PanelToggle visible onClick={() => togglePanel('activity')} /></div><div className="activity-row"><span><Activity size={14} /> {status === 'ready' ? 'بانتظار الطلب' : `الحالة: ${meta.label}`}</span><span><Database size={14} /> Context isolated</span><span><LockKeyhole size={14} /> E2E-ready</span><span><Zap size={14} /> Agent bridge ready</span></div></section>}

          <footer className="q-footer"><span>QUANTUM AI · CORE v5.0</span><span>واجهة قابلة للربط · التنفيذ خارج طبقة UI</span><span>2M TOKEN CONTEXT · E2E-READY</span></footer>
        </section>
      </div>
      <input ref={fileRef} hidden type="file" multiple onChange={(e) => { const file = e.target.files?.[0]; if (file) void analyzeFile(file); e.currentTarget.value = ''; }} />
    </main>
  );
}

function PanelToggle({ visible, onClick }: { visible: boolean; onClick: () => void }) { return <button className="panel-eye" onClick={onClick} title={visible ? 'إخفاء' : 'إظهار'}>{visible ? <Eye size={14} /> : <EyeOff size={14} />}</button>; }

function HomeView({ onNavigate }: { onNavigate: (v: ViewId) => void }) {
  const cards: [ViewId, typeof Bot, string, string][] = [
    ['assistant', Bot, 'AI Assistant', 'محادثة نصية وصوتية مع حالات تشغيل واضحة.'],
    ['research', Globe2, 'Research', 'مصادر متعددة، تحقق، ثقة وتعارضات.'],
    ['workspace', FolderOpen, 'Workspace', 'مشاريع، مجلدات، نسخ، بحث واستعادة.'],
    ['files', FileSearch, 'File Analysis', 'PDF · TXT · DOC · ZIP · APK · صور · كود · روابط.'],
    ['memory', BrainCircuit, 'Memory', 'ذاكرة قابلة للبحث: معلومات، تفضيلات ونتائج.'],
    ['analytics', BarChart3, 'Analytics', 'جودة، دقة، تحقق، أخطاء وأداء.'],
  ];
  return <div className="home-grid">{cards.map(([id, Icon, title, text]) => <button className="module-card glass" key={id} onClick={() => onNavigate(id)}><div className="module-icon"><Icon size={19} /></div><div><small>QUANTUM MODULE</small><b>{title}</b><p>{text}</p></div><ChevronLeft size={16} /></button>)}</div>;
}

function AssistantView({ messages }: { messages: AmarAgentResponse[] }) { return <div className="assistant-view"><div className="chat-state glass"><div className="chat-avatar"><Bot size={25} /></div><div><small>QUANTUM CORE</small><b>جاهز للتواصل</b><p>تحليل → تحقق → مصادر → نتيجة. يمكن للوكيل الخارجي التحكم بالحالات دون تغيير الواجهة.</p></div><div className="voice-tools"><button><Volume2 size={17} /></button><button><Mic size={17} /></button></div></div><div className="chat-list">{messages.length ? messages.map((m) => <article className="message-card" key={m.id}><span className="message-tag">{m.status ?? 'complete'}</span><p>{m.text}</p>{m.confidence && <small>ثقة {m.confidence}%</small>}</article>) : <div className="empty-state"><MessageSquare size={26} /><b>لا توجد رسائل بعد</b><span>ابدأ من مربع الإدخال أدناه.</span></div>}</div></div>; }

function ResearchView({ sources, onRun }: { sources: AmarSource[]; onRun: () => void }) { return <div className="research-view"><div className="research-bar glass"><Search size={17} /><span>استعلام بحث متعدد المصادر...</span><button onClick={onRun}><Play size={14} /> ابدأ التحقق</button></div><div className="research-layout"><div className="research-main glass"><div className="panel-title"><b>COMPARISON MATRIX</b><span>مقارنة الأدلة والتعارضات</span></div><div className="matrix">{sources.map((s, i) => <div key={s.id}><span>{i + 1}</span><b>{s.title}</b><em>{s.confidence}%</em><small>{s.status}</small></div>)}</div></div><div className="confidence glass"><Gauge size={20} /><small>CONFIDENCE</small><strong>{Math.round(sources.reduce((a, s) => a + (s.confidence ?? 0), 0) / Math.max(sources.length, 1))}%</strong><span>Evidence-weighted</span></div></div></div>; }

function WorkspaceView() { return <div className="workspace-view"><div className="workspace-tree glass"><div className="panel-title"><b>PROJECT EXPLORER</b><button><Plus size={14} /></button></div>{['Amar AI','Research','Trading Analysis','Reports','Archive'].map((x, i) => <div className="tree-row" key={x}><FolderOpen size={15} /><span>{x}</span><small>{i + 2}</small></div>)}</div><div className="workspace-canvas glass"><div className="canvas-head"><span>WORKSPACE CANVAS</span><div><button><History size={14} /></button><button><RefreshCw size={14} /></button><button><Archive size={14} /></button></div></div><div className="version-strip"><span>v5.0</span><b>Current working state</b><small>Autosave · Version history · Restore-ready</small></div><div className="canvas-placeholder"><Network size={30} /><b>مساحة عمل حية</b><span>أضف أدوات وملفات ولوحات مستقبلية دون إعادة بناء الهيكل.</span></div></div></div>; }

function FilesView({ onPick }: { onPick: () => void }) { const types = [[FileText,'PDF / TXT / DOC'],[FileArchive,'ZIP / ARCHIVE'],[FileCode2,'CODE / JSON / LOG'],[FileImage,'IMAGE / VIDEO'],[Link2,'EXTERNAL LINK'],[ShieldCheck,'APK · ISOLATED READ-ONLY']]; return <div className="files-view"><button className="drop-zone glass" onClick={onPick}><Upload size={24} /><b>أضف ملفًا أو مصدرًا</b><span>اسحب/اختر ملفًا — لا يوجد تنفيذ تلقائي للملفات الخطرة.</span></button><div className="file-types">{types.map(([Icon, label]) => <div key={label}><Icon size={18} /><span>{label as string}</span></div>)}</div><div className="pipeline glass"><div className="panel-title"><b>ANALYSIS PIPELINE</b><small>File → Scan → Parse → Analyze → Verify → Report</small></div><div className="pipeline-track">{['File','Scan','Parse','Analyze','Verify','Report'].map((x) => <span key={x}><i /><b>{x}</b></span>)}</div></div></div>; }

function MemoryView() { return <div className="memory-grid">{[['Saved info', Database, 'معلومات محفوظة وقابلة للبحث'],['Preferences', Settings2, 'تفضيلات المستخدم والواجهة'],['Past projects', FolderOpen, 'مشاريع ونسخ سابقة'],['Past results', History, 'نتائج وتقارير سابقة'],['Sources', Globe2, 'المصادر والأدلة'],['Retrieve', Search, 'بحث واسترجاع سياقي']].map(([title, Icon, text]) => <div className="memory-card glass" key={title as string}><Icon size={19} /><b>{title as string}</b><p>{text as string}</p><button>فتح <ChevronLeft size={13} /></button></div>)}</div>; }

function AnalyticsView() { return <div className="analytics-grid">{[['Answer quality','94%','مؤشر جودة الإجابات'],['Verification','91%','نسبة التحقق'],['Evidence','87%','قوة الأدلة'],['Errors detected','03','أخطاء مرصودة'],['System performance','98%','أداء الواجهة'],['Source coverage','92%','تغطية المصادر']].map(([a,b,c]) => <div className="analytics-card glass" key={a}><small>{a}</small><strong>{b}</strong><div className="analytic-bar"><i style={{ width: b.includes('%') ? b : '38%' }} /></div><span>{c}</span></div>)}</div>; }

function SettingsView({ theme, setTheme }: { theme: 'aurora' | 'obsidian'; setTheme: (t: 'aurora' | 'obsidian') => void }) { return <div className="settings-view glass"><div className="setting"><div><b>Theme engine</b><small>Dynamic visual system</small></div><button onClick={() => setTheme(theme === 'aurora' ? 'obsidian' : 'aurora')}>{theme}</button></div><div className="setting"><div><b>Panel density</b><small>Cards remain structured and editable</small></div><div className="seg"><button>Compact</button><button className="selected">Balanced</button><button>Detailed</button></div></div><div className="setting"><div><b>Motion</b><small>Smooth, purposeful transitions only</small></div><div className="switch on" /></div><div className="setting"><div><b>Safety boundary</b><small>UI never executes agent/tool logic</small></div><ShieldCheck size={20} /></div><div className="setting"><div><b>Agent adapter</b><small>Any compatible AI Agent can connect through amarAgentBridge.ts</small></div><Code2 size={20} /></div></div>; }
