'use client';

import './quantum.css';
import './item10.css';
import { useEffect, useRef, useState, type CSSProperties } from 'react';
import {
  Activity, BarChart3, Bot, BrainCircuit, CheckCircle2, ChevronLeft, CircleAlert, Clipboard,
  Database, Download, Edit3, Eye, EyeOff, FileArchive, FileCode2, FileImage, FileSearch,
  FileText, FolderOpen, Gauge, Globe2, History, Image as ImageIcon, LayoutGrid, Link2, LockKeyhole,
  Menu, MessageSquare, Mic, MonitorUp, MoreHorizontal, Paperclip, Play, Plus, Save, Search,
  Send, Settings2, ShieldCheck, Square, Trash2, Video, Volume2, WandSparkles, X, Zap
} from 'lucide-react';
import type { AmarAgentBridge, AmarAgentResponse, AmarAgentStatus, AmarProgressStage, AmarSource } from './amarAgentBridge';

type ViewId = 'home' | 'assistant' | 'research' | 'workspace' | 'files' | 'memory' | 'analytics' | 'settings';
type PanelId = 'core' | 'progress' | 'sources' | 'composer' | 'activity' | 'editor';
type SearchMode = 'restricted' | 'open';
type Note = { id: string; title: string; body: string };
type Props = { bridge?: AmarAgentBridge };

type Capabilities = NonNullable<AmarAgentBridge['capabilities']>;

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

const safeStages: { id: AmarProgressStage; label: string }[] = [
  { id: 'understanding', label: 'فهم الطلب' },
  { id: 'planning', label: 'وضع خطة' },
  { id: 'searching', label: 'البحث' },
  { id: 'source_analysis', label: 'تحليل المصادر' },
  { id: 'evidence_comparison', label: 'مقارنة الأدلة' },
  { id: 'conflict_check', label: 'فحص التعارض' },
  { id: 'verification', label: 'التحقق' },
  { id: 'preparing_result', label: 'تجهيز النتيجة' },
];

const statusMeta: Record<AmarAgentStatus, { label: string; tone: string }> = {
  ready: { label: 'جاهز', tone: 'ok' },
  thinking: { label: 'فهم الطلب', tone: 'ai' },
  searching: { label: 'البحث', tone: 'wait' },
  analyzing: { label: 'تحليل المصادر', tone: 'ai' },
  learning: { label: 'التحقق', tone: 'ai' },
  complete: { label: 'اكتمل', tone: 'ok' },
  error: { label: 'خطأ', tone: 'danger' },
  uncertain: { label: 'غير مؤكد', tone: 'wait' },
  blocked: { label: 'محجوب', tone: 'danger' },
};

const terminalStatuses: AmarAgentStatus[] = ['complete', 'error', 'uncertain', 'blocked'];

export default function Item10Workspace({ bridge }: Props) {
  const [view, setView] = useState<ViewId>('home');
  const [status, setStatus] = useState<AmarAgentStatus>('ready');
  const [response, setResponse] = useState<AmarAgentResponse | null>(null);
  const [message, setMessage] = useState('');
  const [sources, setSources] = useState<AmarSource[]>([]);
  const [searchMode, setSearchMode] = useState<SearchMode>('restricted');
  const [progressStage, setProgressStage] = useState<AmarProgressStage | null>(null);
  const [screenSharing, setScreenSharing] = useState(false);
  const [mobileNav, setMobileNav] = useState(false);
  const [theme, setTheme] = useState<'aurora' | 'obsidian'>('aurora');
  const [showEvidence, setShowEvidence] = useState(false);
  const [panels, setPanels] = useState<Record<PanelId, boolean>>({ core: true, progress: true, sources: true, composer: true, activity: true, editor: true });
  const [editorColor, setEditorColor] = useState('#dff7fb');
  const [editorSize, setEditorSize] = useState(16);
  const [editorWeight, setEditorWeight] = useState(400);
  const [showPalette, setShowPalette] = useState(false);
  const [notes, setNotes] = useState<Note[]>([]);
  const [activeNoteId, setActiveNoteId] = useState<string | null>(null);
  const [noteDraft, setNoteDraft] = useState('');
  const [cardScale, setCardScale] = useState(100);
  const fileRef = useRef<HTMLInputElement>(null);
  const mediaRef = useRef<HTMLInputElement>(null);
  const videoRef = useRef<HTMLInputElement>(null);

  const active = nav.find((item) => item.id === view) ?? nav[0];
  const ActiveIcon = active.icon;
  const meta = statusMeta[status];
  const isRunning = !terminalStatuses.includes(status) && status !== 'ready';
  const capabilities: Capabilities = bridge?.capabilities ?? {};

  useEffect(() => {
    try {
      const saved = localStorage.getItem('amar-ui-settings');
      if (!saved) return;
      const parsed = JSON.parse(saved) as Partial<{ theme: 'aurora' | 'obsidian'; color: string; size: number; weight: number; scale: number; notes: Note[] }>;
      if (parsed.theme) setTheme(parsed.theme);
      if (parsed.color) setEditorColor(parsed.color);
      if (parsed.size) setEditorSize(Math.min(32, Math.max(12, parsed.size)));
      if (parsed.weight) setEditorWeight(Math.min(800, Math.max(300, parsed.weight)));
      if (parsed.scale) setCardScale(Math.min(120, Math.max(80, parsed.scale)));
      if (parsed.notes) setNotes(parsed.notes);
    } catch { /* local UI preferences are non-critical */ }
  }, []);

  useEffect(() => {
    try { localStorage.setItem('amar-ui-settings', JSON.stringify({ theme, color: editorColor, size: editorSize, weight: editorWeight, scale: cardScale, notes })); } catch { /* local UI preferences are non-critical */ }
  }, [theme, editorColor, editorSize, editorWeight, cardScale, notes]);

  useEffect(() => {
    if (!bridge?.onStatus) return;
    return bridge.onStatus(setStatus);
  }, [bridge]);

  useEffect(() => {
    if (!bridge?.onProgress) return;
    return bridge.onProgress(setProgressStage);
  }, [bridge]);

  function togglePanel(id: PanelId) { setPanels((p) => ({ ...p, [id]: !p[id] })); }
  function toggleAll(value: boolean) { setPanels({ core: value, progress: value, sources: value, composer: value, activity: value, editor: value }); }

  function applyResponse(result: AmarAgentResponse) {
    setResponse(result);
    if (result.sources) setSources(result.sources);
    if (result.progressStage) setProgressStage(result.progressStage);
    const confirmed = result.status === 'complete' && result.verification === 'confirmed';
    setStatus(confirmed ? 'complete' : (result.status === 'blocked' ? 'blocked' : result.status === 'error' ? 'error' : 'uncertain'));
    setShowEvidence(false);
  }

  async function submitPrompt() {
    const prompt = message.trim();
    if (!prompt) return;
    if (!bridge?.send) {
      setStatus('blocked');
      setResponse({ id: crypto.randomUUID(), text: 'التنفيذ غير متاح: لا يوجد عقد Item 10 متصل بهذه الواجهة.', status: 'blocked', verification: 'blocked' });
      return;
    }
    setMessage(''); setResponse(null); setShowEvidence(false); setProgressStage('understanding'); setStatus('thinking');
    try { applyResponse(await bridge.send({ id: crypto.randomUUID(), prompt, mode: view, searchMode })); }
    catch { setStatus('error'); setResponse({ id: crypto.randomUUID(), text: 'تعذر الحصول على حالة صحيحة من طبقة Item 10.', status: 'error', verification: 'blocked' }); }
  }

  async function runResearch() {
    const query = message.trim();
    if (!query) return;
    if (!bridge?.search) { setStatus('blocked'); setResponse({ id: crypto.randomUUID(), text: 'البحث غير متاح بعد في عقد Item 10 المتصل.', status: 'blocked', verification: 'blocked' }); return; }
    setResponse(null); setShowEvidence(false); setProgressStage('searching'); setStatus('searching');
    try {
      const result = await bridge.search(query, searchMode);
      setSources(result);
      setProgressStage(result.length ? 'verification' : 'conflict_check');
      setStatus(result.length ? 'uncertain' : 'uncertain');
    } catch { setStatus('error'); setResponse({ id: crypto.randomUUID(), text: 'تعذر الحصول على نتيجة بحث موثقة.', status: 'error', verification: 'blocked' }); }
  }

  async function analyzeFile(file: File) {
    if (!bridge?.analyzeFile) { setStatus('blocked'); setResponse({ id: crypto.randomUUID(), text: 'تحليل هذا الملف غير متاح بعد في عقد Item 10 المتصل.', status: 'blocked', verification: 'blocked' }); return; }
    setResponse(null); setShowEvidence(false); setProgressStage('understanding'); setStatus('analyzing');
    try { applyResponse(await bridge.analyzeFile(file)); }
    catch { setStatus('error'); setResponse({ id: crypto.randomUUID(), text: 'تعذر الحصول على حالة تحقق صحيحة لتحليل الملف.', status: 'error', verification: 'blocked' }); }
  }

  async function toggleScreenShare() {
    if (!screenSharing) {
      if (!bridge?.startScreenShare || capabilities.screenShare === false) { setStatus('blocked'); setResponse({ id: crypto.randomUUID(), text: 'مشاركة الشاشة غير متاحة بعد في عقد Item 10 المتصل.', status: 'blocked', verification: 'blocked' }); return; }
      try { await bridge.startScreenShare(); setScreenSharing(true); } catch { setScreenSharing(false); setStatus('blocked'); }
      return;
    }
    bridge?.stopScreenShare?.(); setScreenSharing(false);
  }

  function createNote() { const id = crypto.randomUUID(); setNotes((items) => [...items, { id, title: `صفحة ${items.length + 1}`, body: '' }]); setActiveNoteId(id); setNoteDraft(''); }
  function openNote(note: Note) { setActiveNoteId(note.id); setNoteDraft(note.body); }
  function saveNote() { if (activeNoteId) setNotes((items) => items.map((n) => n.id === activeNoteId ? { ...n, body: noteDraft } : n)); }
  function deleteNote() { if (activeNoteId) setNotes((items) => items.filter((n) => n.id !== activeNoteId)); setActiveNoteId(null); setNoteDraft(''); }
  async function copyText(text: string) { try { await navigator.clipboard.writeText(text); } catch { /* permission denied */ } }

  const currentStageIndex = progressStage ? safeStages.findIndex((s) => s.id === progressStage) : -1;
  const evidenceAvailable = Boolean((response?.sources?.length || sources.length) && capabilities.evidence === true);
  const editorStyle: CSSProperties = { color: editorColor, fontSize: `${editorSize}px`, fontWeight: editorWeight };

  return <main className={`quantum-app theme-${theme}`} dir="rtl" style={{ '--card-scale': `${cardScale / 100}` } as CSSProperties}>
    <div className="q-ambient ambient-a" /><div className="q-ambient ambient-b" /><div className="q-grid" />
    <header className="q-topbar glass"><div className="q-brand"><button className="q-logo" onClick={() => setView('home')}><span>Q</span><i /></button><div><strong>AMAR AI</strong><small>ITEM 10 UI · AGENT BOUNDARY</small></div></div><div className="q-top-center"><span className={`status-dot ${meta.tone}`} /><b>{meta.label}</b><em>·</em><span>Presentation / Interaction Layer</span></div><div className="q-top-actions"><button onClick={() => toggleAll(true)} title="إظهار الكل"><Eye size={16} /></button><button onClick={() => toggleAll(false)} title="إخفاء الكل"><EyeOff size={16} /></button><button onClick={() => setTheme(theme === 'aurora' ? 'obsidian' : 'aurora')} title="المظهر"><WandSparkles size={16} /></button><button onClick={() => setMobileNav(!mobileNav)} className="mobile-only"><Menu size={18} /></button></div></header>
    <div className="q-shell"><aside className={`q-sidebar glass ${mobileNav ? 'mobile-open' : ''}`}><div className="q-sidebar-head"><span>AMAR AI</span><button onClick={() => setMobileNav(false)}><X size={15} /></button></div><nav>{nav.map((item) => { const Icon = item.icon; return <button key={item.id} className={view === item.id ? 'active' : ''} onClick={() => { setView(item.id); setMobileNav(false); }}><Icon size={17} /><span>{item.label}</span></button>; })}</nav><div className="q-sidebar-foot"><ShieldCheck size={14} /><span>لا تنفيذ من طبقة الواجهة</span></div></aside>
      <section className="q-main" style={{ transform: `scale(${1 + (cardScale - 100) / 1000})`, transformOrigin: 'top center' }}>
        <div className="q-heading glass"><div><span className="eyebrow"><ActiveIcon size={13} /> AMAR AI / {view.toUpperCase()}</span><h1>{view === 'home' ? 'مساحة الذكاء الآمنة' : active.label}</h1><p>واجهة متحركة ومتجاوبة؛ Item 10 يحتفظ بالقرار، التحقق، الأدلة، الذاكرة والحوكمة.</p></div>{panels.core && <div className="core-orb"><div className="core-ring ring-1" /><div className="core-ring ring-2" /><div className="core-ring ring-3" /><span>Q</span><small>{meta.label}</small></div>}<PanelToggle visible={panels.core} onClick={() => togglePanel('core')} /></div>
        <section className="search-mode glass"><div><small>SEARCH POLICY</small><b>وضع البحث</b><span>اختيار الواجهة فقط؛ لا يتجاوز الصلاحيات أو الموافقات.</span></div><div className="mode-switch"><button className={searchMode === 'restricted' ? 'selected' : ''} onClick={() => setSearchMode('restricted')} disabled={capabilities.searchRestricted === false}><LockKeyhole size={15} /> بحث بقيود</button><button className={searchMode === 'open' ? 'selected' : ''} onClick={() => setSearchMode('open')} disabled={capabilities.searchOpen !== true}><Globe2 size={15} /> بحث مفتوح</button></div></section>
        {view === 'home' && <HomeView onNavigate={setView} capabilities={capabilities} />}
        {view === 'assistant' && <AssistantView response={response} onCopy={copyText} />}
        {view === 'research' && <ResearchView sources={sources} onRun={() => void runResearch()} enabled={Boolean(bridge?.search)} showEvidence={showEvidence} setShowEvidence={setShowEvidence} evidenceAvailable={evidenceAvailable} />}
        {view === 'workspace' && <WorkspaceView notes={notes} activeNoteId={activeNoteId} noteDraft={noteDraft} setNoteDraft={setNoteDraft} onCreate={createNote} onOpen={openNote} onSave={saveNote} onDelete={deleteNote} onCopy={copyText} />}
        {view === 'files' && <FilesView onPick={() => fileRef.current?.click()} onImage={() => mediaRef.current?.click()} onVideo={() => videoRef.current?.click()} capabilities={capabilities} />}
        {view === 'memory' && <MemoryView />}{view === 'analytics' && <AnalyticsView />}{view === 'settings' && <SettingsView theme={theme} setTheme={setTheme} editorColor={editorColor} setEditorColor={setEditorColor} editorSize={editorSize} setEditorSize={setEditorSize} editorWeight={editorWeight} setEditorWeight={setEditorWeight} showPalette={showPalette} setShowPalette={setShowPalette} cardScale={cardScale} setCardScale={setCardScale} />}
        {panels.progress && <ProgressPanel currentStageIndex={currentStageIndex} isRunning={isRunning} onToggle={() => togglePanel('progress')} />}
        {panels.sources && <SourcesPanel sources={sources} response={response} showEvidence={showEvidence} setShowEvidence={setShowEvidence} evidenceAvailable={evidenceAvailable} onToggle={() => togglePanel('sources')} />}
        {panels.composer && <section className="composer glass"><div className="composer-left"><button onClick={() => fileRef.current?.click()} title="ملف"><Paperclip size={17} /></button><button onClick={() => mediaRef.current?.click()} disabled={capabilities.image !== true} title={capabilities.image === true ? 'صورة' : 'الصورة غير متاحة بعد'}><ImageIcon size={17} /></button><button onClick={() => videoRef.current?.click()} disabled={capabilities.video !== true} title={capabilities.video === true ? 'فيديو' : 'الفيديو غير متاح بعد'}><Video size={17} /></button><button disabled title="الصوت غير متاح بعد في عقد Item 10"><Mic size={17} /></button><button onClick={() => void toggleScreenShare()} disabled={!bridge?.startScreenShare || capabilities.screenShare === false} className={screenSharing ? 'screen-live' : ''} title={screenSharing ? 'Stop Screen Share' : 'Start Screen Share'}>{screenSharing ? <Square size={15} /> : <MonitorUp size={17} />}</button></div><textarea value={message} onChange={(e) => setMessage(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); void submitPrompt(); } }} placeholder="اكتب طلبًا… لا تظهر النتيجة قبل حالة تحقق صحيحة من Item 10" rows={2} /><button className="send" onClick={() => void submitPrompt()} disabled={!bridge?.send || isRunning}><Send size={16} /></button><PanelToggle visible onClick={() => togglePanel('composer')} /></section>}
        {screenSharing && <div className="screen-share-banner glass"><span className="live-dot" /><b>الشاشة قيد المشاركة</b><span>لا تحكم حساس دون تأكيد المستخدم.</span><button onClick={() => void toggleScreenShare()}><Square size={13} /> Stop</button></div>}
        {panels.activity && <section className="activity-panel glass"><div className="panel-title"><b>SESSION SAFETY</b><PanelToggle visible onClick={() => togglePanel('activity')} /></div><div className="activity-row"><span><Activity size={14} /> {meta.label}</span><span><LockKeyhole size={14} /> الصلاحيات خارج UI</span><span><ShieldCheck size={14} /> Fail-Closed</span><span><Zap size={14} /> Item 10 adapter</span></div></section>}
        {panels.editor && <EditorPanel style={editorStyle} showPalette={showPalette} setShowPalette={setShowPalette} onCopy={() => void copyText(noteDraft)} onPaste={async () => { try { setNoteDraft((v) => `${v}${await navigator.clipboard.readText()}`); } catch { /* permission denied */ } }} onToggle={() => togglePanel('editor')} />}
        <footer className="q-footer"><span>AMAR AI · ITEM 10 UI</span><span>القرار والتحقق خارج طبقة الواجهة</span><span>{screenSharing ? 'SCREEN SHARE ACTIVE' : 'READY'} · FAIL-CLOSED</span></footer>
      </section>
    </div>
    <input ref={fileRef} hidden type="file" multiple onChange={(e) => { const file = e.target.files?.[0]; if (file) void analyzeFile(file); e.currentTarget.value = ''; }} />
    <input ref={mediaRef} hidden type="file" accept="image/*" onChange={(e) => { const file = e.target.files?.[0]; if (file) void analyzeFile(file); e.currentTarget.value = ''; }} />
    <input ref={videoRef} hidden type="file" accept="video/*" onChange={(e) => { const file = e.target.files?.[0]; if (file) void analyzeFile(file); e.currentTarget.value = ''; }} />
  </main>;
}

function PanelToggle({ visible, onClick }: { visible: boolean; onClick: () => void }) { return <button className="panel-eye" onClick={onClick} title={visible ? 'إخفاء' : 'إظهار'}>{visible ? <Eye size={14} /> : <EyeOff size={14} />}</button>; }

function HomeView({ onNavigate, capabilities }: { onNavigate: (v: ViewId) => void; capabilities: Capabilities }) {
  const cards: [ViewId, typeof Bot, string, string][] = [['assistant', Bot, 'AI Assistant', 'نص وحالات تحقق، وأدلة عند توفرها.'], ['research', Globe2, 'Research', 'بحث بقيود أو مفتوح وفق صلاحيات Item 10.'], ['workspace', FolderOpen, 'Workspace', 'مفكرة وصفحات متعددة ونسخ محلية للواجهة.'], ['files', FileSearch, 'File Analysis', 'ملفات وصور وفيديو دون تنفيذ تلقائي.'], ['memory', BrainCircuit, 'Memory', 'عرض فقط؛ الذاكرة الفعلية خارج UI.'], ['analytics', BarChart3, 'Analytics', 'مؤشرات فقط عندما تأتي من طبقة موثوقة.']];
  return <div className="home-grid">{cards.map(([id, Icon, title, text]) => <button className="module-card glass" key={id} onClick={() => onNavigate(id)}><div className="module-icon"><Icon size={19} /></div><div><small>AMAR MODULE</small><b>{title}</b><p>{text}</p>{id === 'research' && <small>{capabilities.searchOpen === true ? 'Open search available' : 'Open search unavailable'}</small>}</div><ChevronLeft size={16} /></button>)}</div>;
}

function AssistantView({ response, onCopy }: { response: AmarAgentResponse | null; onCopy: (text: string) => void }) {
  return <div className="assistant-view"><div className="chat-state glass"><div className="chat-avatar"><Bot size={25} /></div><div><small>ITEM 10</small><b>النتيجة لا تظهر إلا بعد التحقق</b><p>المراحل المعروضة آمنة فقط؛ لا Chain-of-Thought ولا تفكير سري.</p></div><div className="voice-tools"><button disabled><Volume2 size={17} /></button><button disabled><Mic size={17} /></button></div></div>{response ? <article className={`message-card glass verification-${response.verification ?? 'uncertain'}`}><span className="message-tag">{response.verification === 'confirmed' ? 'Confirmed' : response.verification === 'blocked' ? 'Blocked' : 'Uncertain'}</span>{response.verification === 'confirmed' && response.status === 'complete' ? <><p>{response.text}</p>{response.durationMs != null && <small>الزمن: {response.durationMs} ms</small>}<button onClick={() => onCopy(response.text)}><Clipboard size={14} /> نسخ</button></> : <div className="uncertainty-box"><CircleAlert size={18} /><div><b>{response.verification === 'blocked' ? 'العملية محجوبة' : 'الأدلة غير كافية للتأكيد'}</b><p>{response.uncertaintyReason || response.text}</p></div></div>}</article> : <div className="empty-state glass"><MessageSquare size={26} /><b>لا توجد نتيجة</b><span>ابدأ من مربع الإدخال بعد ربط عقد Item 10.</span></div>}</div>;
}

function ResearchView({ sources, onRun, enabled, showEvidence, setShowEvidence, evidenceAvailable }: { sources: AmarSource[]; onRun: () => void; enabled: boolean; showEvidence: boolean; setShowEvidence: (v: boolean) => void; evidenceAvailable: boolean }) {
  return <div className="research-view"><div className="research-bar glass"><Search size={17} /><span>استعلام البحث من مربع الإدخال السفلي</span><button onClick={onRun} disabled={!enabled}><Play size={14} /> ابدأ البحث</button></div><div className="research-layout"><div className="research-main glass"><div className="panel-title"><b>COMPARISON MATRIX</b><span>الأدلة والتعارضات من Item 10 فقط</span></div>{sources.length ? <div className="matrix">{sources.map((s, i) => <div key={s.id}><span>{i + 1}</span><b>{s.title}</b><em>{s.confidence ?? '—'}%</em><small>{s.status === 'conflict' ? 'تعارض' : s.status === 'verified' ? 'موثق' : 'قيد التحقق'}</small></div>)}</div> : <div className="empty-state"><Globe2 size={24} /><b>لا توجد أدلة مستلمة</b><span>لن تُنشئ الواجهة مصادر من تلقاء نفسها.</span></div>}</div><div className="confidence glass"><Gauge size={20} /><small>CONFIDENCE</small><strong>{sources.length ? `${Math.round(sources.reduce((a, s) => a + (s.confidence ?? 0), 0) / sources.length)}%` : '—'}</strong><span>لا تعني هذه النسبة تأكيدًا مستقلاً من الواجهة.</span></div></div>{evidenceAvailable && <button className="evidence-button" onClick={() => setShowEvidence(!showEvidence)}>{showEvidence ? 'إخفاء الأدلة والمصادر' : 'عرض الأدلة والمصادر'}</button>}</div>;
}

function WorkspaceView({ notes, activeNoteId, noteDraft, setNoteDraft, onCreate, onOpen, onSave, onDelete, onCopy }: { notes: Note[]; activeNoteId: string | null; noteDraft: string; setNoteDraft: (v: string) => void; onCreate: () => void; onOpen: (n: Note) => void; onSave: () => void; onDelete: () => void; onCopy: (v: string) => void }) {
  return <div className="workspace-view"><div className="workspace-tree glass"><div className="panel-title"><b>NOTEBOOK</b><button onClick={onCreate}><Plus size={14} /></button></div>{notes.map((note) => <button className={activeNoteId === note.id ? 'tree-row active' : 'tree-row'} key={note.id} onClick={() => onOpen(note)}><FileText size={15} /><span>{note.title}</span></button>)}{!notes.length && <div className="empty-state"><FileText size={20} /><span>لا توجد صفحات بعد.</span></div>}</div><div className="workspace-canvas glass"><div className="canvas-head"><span>EDITOR</span><div><button onClick={() => void onCopy(noteDraft)} disabled={!activeNoteId}><Clipboard size={14} /></button><button onClick={onSave} disabled={!activeNoteId}><Save size={14} /></button><button onClick={onDelete} disabled={!activeNoteId}><Trash2 size={14} /></button></div></div><textarea value={noteDraft} onChange={(e) => setNoteDraft(e.target.value)} placeholder="اكتب هنا…" disabled={!activeNoteId} /></div></div>;
}

function FilesView({ onPick, onImage, onVideo, capabilities }: { onPick: () => void; onImage: () => void; onVideo: () => void; capabilities: Capabilities }) {
  const types: [typeof FileText, string, () => void, boolean][] = [[FileText, 'PDF / TXT / DOC', onPick, capabilities.text === true], [FileArchive, 'ZIP / ARCHIVE', onPick, capabilities.text === true], [FileCode2, 'CODE / JSON / LOG', onPick, capabilities.text === true], [FileImage, 'IMAGE', onImage, capabilities.image === true], [Video, 'VIDEO', onVideo, capabilities.video === true], [Link2, 'EXTERNAL LINK', () => undefined, false]];
  return <div className="files-view"><div className="file-action-grid">{types.map(([Icon, label, action, enabled]) => <button key={label} className="drop-zone glass" onClick={action} disabled={!enabled}><Icon size={22} /><b>{label}</b><span>{enabled ? 'متاح عبر العقد المتصل' : 'غير متاحة بعد'}</span></button>)}</div><div className="pipeline glass"><div className="panel-title"><b>SAFE PRESENTATION PIPELINE</b><small>File → Scan → Parse → Analyze → Verify → Report</small></div><div className="pipeline-track">{['File', 'Scan', 'Parse', 'Analyze', 'Verify', 'Report'].map((x) => <span key={x}><i /><b>{x}</b></span>)}</div><small>الواجهة تعرض الحالة فقط؛ لا تنفذ الملفات أو تغيّر قرارات Item 10.</small></div></div>;
}

function MemoryView() { return <div className="memory-grid">{[['Saved info', Database, 'العرض فقط؛ التخزين الفعلي خارج UI'], ['Preferences', Settings2, 'تفضيلات واجهة محلية'], ['Past projects', FolderOpen, 'مساحات عمل محلية'], ['Past results', History, 'لا تعرض نتيجة غير متحققة'], ['Sources', Globe2, 'أدلة تأتي من طبقة موثوقة'], ['Retrieve', Search, 'الاسترجاع الفعلي عبر العقد']].map(([title, Icon, text]) => <div className="memory-card glass" key={title as string}><Icon size={19} /><b>{title as string}</b><p>{text as string}</p><button disabled>فتح <ChevronLeft size={13} /></button></div>)}</div>; }

function AnalyticsView() { return <div className="analytics-grid">{[['Verification', '—', 'بانتظار بيانات Item 10'], ['Evidence', '—', 'بانتظار الأدلة الموثقة'], ['Errors', '—', 'لا بيانات مصطنعة'], ['Performance', '—', 'يُعرض عند وصول قياس موثوق']].map(([a, b, c]) => <div className="analytics-card glass" key={a}><small>{a}</small><strong>{b}</strong><span>{c}</span></div>)}</div>; }

function SettingsView({ theme, setTheme, editorColor, setEditorColor, editorSize, setEditorSize, editorWeight, setEditorWeight, showPalette, setShowPalette, cardScale, setCardScale }: { theme: 'aurora' | 'obsidian'; setTheme: (v: 'aurora' | 'obsidian') => void; editorColor: string; setEditorColor: (v: string) => void; editorSize: number; setEditorSize: (v: number) => void; editorWeight: number; setEditorWeight: (v: number) => void; showPalette: boolean; setShowPalette: (v: boolean) => void; cardScale: number; setCardScale: (v: number) => void }) {
  return <div className="settings-view glass"><div className="setting"><div><b>Theme</b><small>واجهة ديناميكية قابلة للتبديل</small></div><button onClick={() => setTheme(theme === 'aurora' ? 'obsidian' : 'aurora')}>{theme}</button></div><div className="setting"><div><b>لون الكتابة</b><small>محلي للمحرر فقط</small></div><input type="color" value={editorColor} onChange={(e) => setEditorColor(e.target.value)} /></div><div className="setting"><div><b>حجم الخط</b><small>12–32px</small></div><input type="range" min="12" max="32" value={editorSize} onChange={(e) => setEditorSize(Number(e.target.value))} /></div><div className="setting"><div><b>سماكة الخط</b><small>300–800</small></div><input type="range" min="300" max="800" step="100" value={editorWeight} onChange={(e) => setEditorWeight(Number(e.target.value))} /></div><div className="setting"><div><b>لوحة الألوان</b><small>إظهار / إخفاء</small></div><button onClick={() => setShowPalette(!showPalette)}>{showPalette ? 'إخفاء' : 'إظهار'}</button></div><div className="setting"><div><b>حجم البطاقات</b><small>80–120% مع حفظ الإعداد</small></div><input type="range" min="80" max="120" value={cardScale} onChange={(e) => setCardScale(Number(e.target.value))} /></div><div className="setting"><div><b>Safety boundary</b><small>لا صلاحيات تنفيذية داخل UI</small></div><ShieldCheck size={20} /></div></div>;
}

function ProgressPanel({ currentStageIndex, isRunning, onToggle }: { currentStageIndex: number; isRunning: boolean; onToggle: () => void }) { return <section className={`progress-panel glass ${isRunning ? 'running' : 'collapsed-terminal'}`}><div className="panel-title"><div><b>SAFE OPERATIONAL PROGRESS</b><small>{isRunning ? 'مراحل آمنة فقط' : 'تم تقليل شاشة التقدم بعد انتهاء العملية'}</small></div><PanelToggle visible onClick={onToggle} /></div><div className="progress-steps expanded-safe">{safeStages.map((stage, index) => <div className={index <= currentStageIndex && currentStageIndex >= 0 ? 'done' : ''} key={stage.id}><span>{index <= currentStageIndex && currentStageIndex >= 0 ? <CheckCircle2 size={14} /> : index + 1}</span><b>{stage.label}</b>{index < safeStages.length - 1 && <i />}</div>)}</div><small className="progress-safety">لا يتم عرض التفكير الداخلي أو Chain-of-Thought.</small></section>; }

function SourcesPanel({ sources, response, showEvidence, setShowEvidence, evidenceAvailable, onToggle }: { sources: AmarSource[]; response: AmarAgentResponse | null; showEvidence: boolean; setShowEvidence: (v: boolean) => void; evidenceAvailable: boolean; onToggle: () => void }) { return <section className="sources-panel glass"><div className="panel-title"><div><b>EVIDENCE & SOURCES</b><small>لا مصادر مصطنعة من الواجهة</small></div><PanelToggle visible onClick={onToggle} /></div>{evidenceAvailable ? <><button className="evidence-button" onClick={() => setShowEvidence(!showEvidence)}>{showEvidence ? 'إخفاء الأدلة والمصادر' : 'عرض الأدلة والمصادر'}</button>{showEvidence && <div className="source-grid">{(response?.sources?.length ? response.sources : sources).map((s) => <div className="source-card" key={s.id}><div className="source-icon"><Globe2 size={15} /></div><div><b>{s.title}</b><p>{s.excerpt || 'بدون مقتطف.'}</p><small className={`source-state ${s.status}`}>{s.status === 'verified' ? 'موثق' : s.status === 'conflict' ? 'تعارض' : 'قيد التحقق'} {s.confidence != null ? `· ${s.confidence}%` : ''}</small></div><MoreHorizontal size={15} /></div>)}</div>}</> : <div className="empty-state"><Globe2 size={22} /><span>الأدلة والمصادر غير متاحة بعد عبر العقد المتصل.</span></div>}</section>; }

function EditorPanel({ style, showPalette, setShowPalette, onCopy, onPaste, onToggle }: { style: CSSProperties; showPalette: boolean; setShowPalette: (v: boolean) => void; onCopy: () => void; onPaste: () => void; onToggle: () => void }) { return <section className="editor-panel glass"><div className="panel-title"><div><b>EDITOR CONTROLS</b><small>إعدادات العرض المحلية</small></div><PanelToggle visible onClick={onToggle} /></div><div className="editor-toolbar"><button onClick={onCopy}><Clipboard size={14} /> نسخ</button><button onClick={onPaste}><Download size={14} /> لصق</button><button onClick={() => setShowPalette(!showPalette)}><Edit3 size={14} /> {showPalette ? 'إخفاء الألوان' : 'إظهار الألوان'}</button><span style={style}>Aa</span></div>{showPalette && <div className="palette-row"><span>لون الكتابة</span><span className="palette-dot" /><span>حجم 12–32</span><span>سماكة 300–800</span></div>}</section>; }
