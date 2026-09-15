'use client';

import './quantum.css';
import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Activity, BarChart3, Bot, BrainCircuit, CheckCircle2, ChevronLeft, CircleAlert, Clipboard,
  Code2, Database, Download, Edit3, Eye, EyeOff, FileArchive, FileCode2, FileImage, FileSearch,
  FileText, FolderOpen, Gauge, Globe2, History, Image as ImageIcon, LayoutGrid, Link2, LockKeyhole,
  Menu, MessageSquare, Mic, MonitorUp, MoreHorizontal, Network, Paperclip, Pause, Play, Plus,
  RefreshCw, Save, Search, Send, Settings2, ShieldCheck, Sparkles, Square, Trash2, Upload,
  Video, Volume2, WandSparkles, X, Zap
} from 'lucide-react';
import type {
  AmarAgentBridge, AmarAgentResponse, AmarAgentStatus, AmarProgressStage, AmarSource,
  AmarVerificationState
} from './amarAgentBridge';

type ViewId = 'home' | 'assistant' | 'research' | 'workspace' | 'files' | 'memory' | 'analytics' | 'settings';
type PanelId = 'core' | 'progress' | 'sources' | 'composer' | 'activity' | 'editor';
type SearchMode = 'restricted' | 'open';
type Note = { id: string; title: string; body: string };

type Props = { bridge?: AmarAgentBridge };

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

export default function QuantumWorkspace({ bridge }: Props) {
  const [view, setView] = useState<ViewId>('home');
  const [status, setStatus] = useState<AmarAgentStatus>('ready');
  const [response, setResponse] = useState<AmarAgentResponse | null>(null);
  const [message, setMessage] = useState('');
  const [sources, setSources] = useState<AmarSource[]>([]);
  const [searchMode, setSearchMode] = useState<SearchMode>('restricted');
  const [progressStage, setProgressStage] = useState<AmarProgressStage | null>(null);
  const [recording, setRecording] = useState(false);
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

  const copyText = async (text: string) => {
    try { await navigator.clipboard.writeText(text); } catch { /* permission denied */ }
  };

  const pasteText = async () => {
    try {
      const text = await navigator.clipboard.readText();
      setNoteDraft((value) => `${value}${text}`);
    } catch { /* permission denied */ }
  };

  const [editorStyle] = useState({ color: editorColor, fontSize: editorSize, fontWeight: editorWeight });

  useEffect(() => { setNotes((current) => current); }, []);

  const meta = statusMeta[status];
  const togglePanel = (id: PanelId) => setPanels((current) => ({ ...current, [id]: !current[id] }));
  const toggleScreenShare = () => setScreenSharing((current) => !current);

  const submitPrompt = async () => {
    if (!bridge?.send || !message.trim()) return;
    setStatus('thinking');
    setProgressStage('understanding');
    const result = await bridge.send({ text: message, searchMode });
    setResponse(result);
    setStatus(result.status);
    setProgressStage(null);
    setMessage('');
  };

  const analyzeFile = async (file: File) => {
    if (!bridge?.analyzeFile) return;
    setStatus('thinking');
    setProgressStage('understanding');
    const result = await bridge.analyzeFile(file, searchMode);
    setResponse(result);
    setStatus(result.status);
    setProgressStage(null);
  };

  const fileRef = useRef<HTMLInputElement>(null);
  const mediaRef = useRef<HTMLInputElement>(null);
  const videoRef = useRef<HTMLInputElement>(null);

  return (
    <div className={`quantum-shell ${theme}`}>
      <header className="q-header"><button onClick={() => setMobileNav((v) => !v)}><Menu size={18} /></button><b>AMAR AI</b><span>{meta.label}</span></header>
      {mobileNav && <nav>{nav.map((item) => <button key={item.id} onClick={() => { setView(item.id); setMobileNav(false); }}><item.icon size={16} />{item.label}</button>)}</nav>}
      <main>
        <section className="workspace-grid">
          {panels.core && <section className="core-panel glass"><div className="panel-title"><b>QUANTUM CORE</b><PanelToggle visible onClick={() => togglePanel('core')} /></div><div className={`core-status ${meta.tone}`}>{meta.label}</div><div className="core-orb"><Sparkles size={40} /></div></section>}
          {panels.progress && <section className="progress-panel glass"><div className="panel-title"><b>ANALYSIS</b><PanelToggle visible onClick={() => togglePanel('progress')} /></div>{safeStages.map((stage) => <div key={stage.id} className={progressStage === stage.id ? 'stage active' : 'stage'}>{stage.label}</div>)}</section>}
          {panels.sources && <section className="sources-panel glass"><div className="panel-title"><b>EVIDENCE</b><PanelToggle visible onClick={() => togglePanel('sources')} /></div>{response?.sources?.map((source) => <div key={source.id}>{source.title}</div>)}</section>}
          {panels.composer && <section className="composer-panel glass"><div className="search-mode"><button className={searchMode === 'restricted' ? 'active' : ''} onClick={() => setSearchMode('restricted')}>🔒 بحث بقيود</button><button className={searchMode === 'open' ? 'active' : ''} onClick={() => setSearchMode('open')}>🌐 بحث مفتوح</button></div><div className="composer-tools"><button onClick={() => fileRef.current?.click()}><Paperclip size={17} /></button><button onClick={() => mediaRef.current?.click()}><ImageIcon size={17} /></button><button onClick={() => videoRef.current?.click()}><Video size={17} /></button><button onClick={() => setRecording(!recording)} disabled={!bridge?.send}>{recording ? <Square size={15} /> : <Mic size={17} />}</button><button onClick={toggleScreenShare} className={screenSharing ? 'screen-live' : ''}>{screenSharing ? <Square size={15} /> : <MonitorUp size={17} />}</button></div><textarea value={message} onChange={(event) => setMessage(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); void submitPrompt(); } }} placeholder="اكتب طلبًا…" rows={2} /><button className="send" onClick={() => void submitPrompt()} disabled={!bridge?.send || status === 'thinking'}><Send size={16} /></button><PanelToggle visible onClick={() => togglePanel('composer')} /></section>}
          {screenSharing && <div className="screen-share-banner glass"><span className="live-dot" /><b>الشاشة قيد المشاركة</b><button onClick={toggleScreenShare}><Square size={13} /> Stop</button></div>}
          {panels.activity && <section className="activity-panel glass"><div className="panel-title"><b>SESSION SAFETY</b><PanelToggle visible onClick={() => togglePanel('activity')} /></div><div className="activity-row"><span><Activity size={14} /> {meta.label}</span><span><LockKeyhole size={14} /> صلاحيات خارج UI</span><span><ShieldCheck size={14} /> Fail-Closed</span><span><Zap size={14} /> Item 10 adapter</span></div></section>}
          {panels.editor && <EditorPanel style={editorStyle} showPalette={showPalette} setShowPalette={setShowPalette} onCopy={() => void copyText(noteDraft)} onPaste={() => void pasteText()} onToggle={() => togglePanel('editor')} />}
          <footer className="q-footer"><span>AMAR AI · ITEM 10 UI</span><span>القرار والتحقق خارج طبقة الواجهة</span><span>{screenSharing ? 'SCREEN SHARE ACTIVE' : 'READY'} · FAIL-CLOSED</span></footer>
        </section>
      </main>
      <input ref={fileRef} hidden type="file" multiple onChange={(event) => { const file = event.target.files?.[0]; if (file) void analyzeFile(file); event.currentTarget.value = ''; }} />
      <input ref={mediaRef} hidden type="file" accept="image/*" onChange={(event) => { const file = event.target.files?.[0]; if (file) void analyzeFile(file); event.currentTarget.value = ''; }} />
      <input ref={videoRef} hidden type="file" accept="video/*" onChange={(event) => { const file = event.target.files?.[0]; if (file) void analyzeFile(file); event.currentTarget.value = ''; }} />
    </div>
  );
}

function PanelToggle({ visible, onClick }: { visible: boolean; onClick: () => void }) { return <button onClick={onClick} aria-label={visible ? 'hide' : 'show'}>{visible ? <EyeOff size={15} /> : <Eye size={15} />}</button>; }

function EditorPanel({ style, showPalette, setShowPalette, onCopy, onPaste, onToggle }: { style: React.CSSProperties; showPalette: boolean; setShowPalette: (value: boolean) => void; onCopy: () => void; onPaste: () => void; onToggle: () => void }) { return <section className="editor-panel glass"><div className="panel-title"><b>EDITOR</b><PanelToggle visible onClick={onToggle} /></div><div style={style}>محرر الواجهة جاهز للتحرير</div><div className="editor-actions"><button onClick={() => setShowPalette(!showPalette)}>Palette</button><button onClick={onCopy}>Copy</button><button onClick={onPaste}>Paste</button></div>{showPalette && <div className="palette">Editor settings</div>}</section>; }
