'use client';

import { useEffect, useMemo, useState } from 'react';
import { Activity, BarChart3, Bot, BrainCircuit, Camera, ChevronLeft, CircleHelp, FileSearch, Image as ImageIcon, LayoutDashboard, Mic, MonitorUp, Paperclip, Play, Search, Send, ShieldCheck, SlidersHorizontal, Sparkles, TrendingUp, Upload, Video, Volume2, X } from 'lucide-react';
import { amarDemoStream, type AmarEvent } from '../lib/events';
import { AMAR_UI_MANIFEST } from '../lib/amarUiManifest';

type ToolId = 'overview' | 'trade' | 'research' | 'vision' | 'voice' | 'screen' | 'files' | 'history';

const tools: { id: ToolId; label: string; short: string; icon: typeof BrainCircuit }[] = [
  { id: 'overview', label: 'المحور', short: 'الرئيسية', icon: LayoutDashboard },
  { id: 'trade', label: 'تحليل التداول', short: 'السوق', icon: TrendingUp },
  { id: 'research', label: 'البحث والتحقق', short: 'بحث', icon: FileSearch },
  { id: 'vision', label: 'تحليل الصور والفيديو', short: 'رؤية', icon: ImageIcon },
  { id: 'voice', label: 'التواصل الصوتي', short: 'صوت', icon: Mic },
  { id: 'screen', label: 'مشاركة الشاشة', short: 'شاشة', icon: MonitorUp },
  { id: 'files', label: 'الملفات والمصادر', short: 'ملفات', icon: Paperclip },
  { id: 'history', label: 'الجلسات والتقارير', short: 'سجل', icon: Activity },
];

const quickActions = [
  ['حلل الشارت الحالي', TrendingUp],
  ['ابحث وتحقق من المصادر', Search],
  ['حلل صورة / لقطة شاشة', Camera],
  ['ابدأ جلسة صوتية', Mic],
  ['شارك الشاشة', MonitorUp],
  ['أنشئ تقريرًا', FileSearch],
] as const;

export default function Page() {
  const [entered, setEntered] = useState(false);
  const [active, setActive] = useState<ToolId>('overview');
  const [events, setEvents] = useState<AmarEvent[]>([]);
  const [time, setTime] = useState('');
  const [energy, setEnergy] = useState(82);
  const [equity, setEquity] = useState(1000);
  const [message, setMessage] = useState('');
  const [composerOpen, setComposerOpen] = useState(true);
  const [compact, setCompact] = useState(false);

  useEffect(() => {
    const cb = (event: AmarEvent) => {
      setEvents((current) => [event, ...current].slice(0, 6));
      setEnergy(Math.round(70 + Math.random() * 27));
      if (event.type === 'ACCOUNT_UPDATE' && typeof event.payload.equity === 'number') setEquity(event.payload.equity);
    };
    amarDemoStream.connect(cb);
    const timer = window.setInterval(() => setTime(new Date().toLocaleTimeString('ar-IQ')), 1000);
    return () => {
      amarDemoStream.disconnect(cb);
      window.clearInterval(timer);
    };
  }, []);

  const activeTool = useMemo(() => tools.find((tool) => tool.id === active) ?? tools[0], [active]);
  const ActiveIcon = activeTool.icon;

  if (!entered) {
    return (
      <main className="agent-entry" dir="rtl">
        <div className="entry-noise" />
        <div className="entry-orbit orbit-one" />
        <div className="entry-orbit orbit-two" />
        <div className="entry-glow" />
        <section className="agent-entry-card">
          <div className="agent-mark"><span>ع</span><i /></div>
          <div className="eyebrow">AMAR AI · INTELLIGENCE WORKSPACE</div>
          <h1>مساعدك الذكي</h1>
          <p>تحليل تداول، بحث، رؤية، صوت ومشاركة شاشة — داخل مساحة واحدة قابلة للتطوير.</p>
          <div className="entry-capabilities">
            <span><TrendingUp /> تداول</span><span><FileSearch /> بحث</span><span><ImageIcon /> رؤية</span><span><Mic /> صوت</span><span><MonitorUp /> شاشة</span>
          </div>
          <button className="entry-launch" onClick={() => setEntered(true)}><Sparkles size={18} /><b>فتح مساحة عمار</b><ChevronLeft size={18} /></button>
          <small><span className="live-dot" /> واجهة تجريبية آمنة · لا تنفذ أوامر تداول من الواجهة</small>
        </section>
        <footer>AMAR AI · {AMAR_UI_MANIFEST.version} · {time || 'READY'}</footer>
      </main>
    );
  }

  return (
    <main className={`agent-shell ${compact ? 'compact-mode' : ''}`} dir="rtl">
      <header className="agent-topbar glass-panel">
        <div className="brand-block">
          <button className="brand-orb" onClick={() => setCompact((value) => !value)} aria-label="تبديل الواجهة"><span>ع</span><i /></button>
          <div><b>عمار AI</b><small>INTELLIGENCE WORKSPACE</small></div>
        </div>
        <div className="session-status"><span className="live-dot" /> جلسة نشطة <b>{time}</b></div>
        <div className="top-actions">
          <button title="الحالة"><Activity size={17} /></button>
          <button title="الحماية"><ShieldCheck size={17} /></button>
          <button title="الإعدادات"><SlidersHorizontal size={17} /></button>
        </div>
      </header>

      <div className="agent-layout">
        <aside className="agent-sidebar glass-panel">
          <div className="sidebar-head"><span>المساحات</span><button onClick={() => setCompact((value) => !value)}><ChevronLeft size={15} /></button></div>
          <nav>
            {tools.map((tool) => {
              const Icon = tool.icon;
              return <button key={tool.id} className={active === tool.id ? 'selected' : ''} onClick={() => setActive(tool.id)}><Icon size={18} /><span>{tool.label}</span><em>{tool.short}</em></button>;
            })}
          </nav>
          <div className="sidebar-footer"><CircleHelp size={15} /><span>مساحة قابلة للتوسع</span></div>
        </aside>

        <section className="agent-main">
          <div className="agent-hero glass-panel">
            <div className="hero-copy"><div className="section-kicker"><ActiveIcon size={15} /> {activeTool.label}</div><h1>{active === 'overview' ? 'ماذا تريد أن نحلل اليوم؟' : activeTool.label}</h1><p>{active === 'overview' ? 'اختر أداة أو اكتب طلبك. صممت المساحة لتضيف أدوات جديدة لاحقًا دون تغيير شكل الواجهة.' : 'هذه مساحة واجهة مخصصة لهذه المهمة، ويمكن ربطها بالخدمات والمحركات لاحقًا دون إعادة بناء التصميم.'}</p></div>
            <div className="hero-orb"><div className="orb-ring ring-a" /><div className="orb-ring ring-b" /><span>ع</span><small>{energy}%</small></div>
          </div>

          <div className="workspace-grid">
            <section className="primary-workspace glass-panel">
              <div className="workspace-head"><div><span className="mini-status"><i /> LIVE UI</span><b>{activeTool.label}</b></div><button onClick={() => setActive('overview')}><X size={16} /></button></div>
              {active === 'overview' && <Overview equity={equity} events={events} onSelect={setActive} />}
              {active === 'trade' && <TradeView />}
              {active === 'research' && <ResearchView />}
              {active === 'vision' && <VisionView />}
              {active === 'voice' && <VoiceView />}
              {active === 'screen' && <ScreenView />}
              {active === 'files' && <FilesView />}
              {active === 'history' && <HistoryView events={events} />}
            </section>

            <aside className="agent-inspector glass-panel">
              <div className="inspector-title"><span>لوحة السياق</span><b>AMAR CORE</b></div>
              <div className="context-card"><span>الحالة</span><strong>جاهز للتحليل</strong><small>الواجهة فقط · التنفيذ منفصل</small></div>
              <div className="context-row"><span>طاقة الجلسة</span><b>{energy}%</b></div>
              <div className="meter"><i style={{ width: `${energy}%` }} /></div>
              <div className="context-row"><span>رصيد تجريبي</span><b>${equity.toFixed(2)}</b></div>
              <div className="context-row"><span>مصادر الجلسة</span><b>{Math.max(events.length, 1)}</b></div>
              <div className="inspector-actions"><button><Upload size={15} /> إضافة مصدر</button><button><ShieldCheck size={15} /> فحص</button></div>
            </aside>
          </div>

          {composerOpen && <section className="agent-composer glass-panel">
            <div className="composer-tools"><button title="ملف"><Paperclip size={18} /></button><button title="صورة"><ImageIcon size={18} /></button><button title="صوت"><Mic size={18} /></button><button title="شاشة"><MonitorUp size={18} /></button></div>
            <textarea value={message} onChange={(event) => setMessage(event.target.value)} placeholder="اكتب لعمار: حلل الذهب، ابحث عن سبب الحركة، افحص هذه الصورة..." rows={1} />
            <button className="send-button" onClick={() => setMessage('')}><Send size={17} /></button>
          </section>}

          <div className="quick-strip">{quickActions.map(([label, Icon]) => <button key={label} onClick={() => setMessage(label)}><Icon size={14} />{label}</button>)}</div>
        </section>
      </div>

      <footer className="agent-footer"><span>AMAR AI · WORKSPACE</span><span>تجريبي فقط · الواجهة لا تتخذ قرارات التداول</span><span>{time}</span></footer>
    </main>
  );
}

function Overview({ equity, events, onSelect }: { equity: number; events: AmarEvent[]; onSelect: (id: ToolId) => void }) {
  return <div className="overview-view">
    <div className="overview-cards">
      <Metric icon={TrendingUp} label="XAUUSD" value="3,472.18" change="+0.42%" />
      <Metric icon={BarChart3} label="الاتجاه" value="مراقبة" change="ثقة 82%" />
      <Metric icon={ShieldCheck} label="المخاطر" value="محكومة" change="2.10% DD" />
      <Metric icon={Activity} label="الحساب" value={`$${equity.toFixed(2)}`} change="DEMO" />
    </div>
    <div className="capability-grid">
      <Capability icon={TrendingUp} title="تحليل تداولي" text="شارت، اتجاه، مستويات، سيناريوهات ومخاطر في مساحة واحدة." onClick={() => onSelect('trade')} />
      <Capability icon={FileSearch} title="بحث وتحقيق" text="جمع المصادر، المقارنة، التحقق، ثم إخراج نتيجة منظمة." onClick={() => onSelect('research')} />
      <Capability icon={ImageIcon} title="رؤية متعددة الوسائط" text="صورة، لقطة شاشة وفيديو كمساحات تحليل مستقلة." onClick={() => onSelect('vision')} />
      <Capability icon={Mic} title="تواصل صوتي" text="مساحة صوتية مصممة للمحادثة والاستماع والتبديل بين الأدوار." onClick={() => onSelect('voice')} />
      <Capability icon={MonitorUp} title="مشاركة الشاشة" text="وضع شاشة مخصص للتحليل المشترك مع مؤشرات الخصوصية." onClick={() => onSelect('screen')} />
      <Capability icon={Paperclip} title="ملفات ومصادر" text="أضف ملفات ومراجع واحتفظ بسياق الجلسة قابلًا للتوسع." onClick={() => onSelect('files')} />
    </div>
    <div className="activity-feed"><div className="feed-title"><b>نبض الجلسة</b><span>آخر الأحداث</span></div>{events.length ? events.map((event, index) => <div className="feed-item" key={`${event.type}-${index}`}><i /><span>{event.type.replaceAll('_', ' ')}</span><small>{index === 0 ? 'الآن' : `${index} حدث سابق`}</small></div>) : <div className="empty-feed"><Bot size={22} /> بانتظار أول نشاط</div>}</div>
  </div>;
}

function Metric({ icon: Icon, label, value, change }: { icon: typeof Activity; label: string; value: string; change: string }) { return <div className="metric-card"><div className="metric-icon"><Icon size={16} /></div><small>{label}</small><b>{value}</b><span>{change}</span></div>; }
function Capability({ icon: Icon, title, text, onClick }: { icon: typeof Activity; title: string; text: string; onClick: () => void }) { return <button className="capability-card" onClick={onClick}><div className="cap-icon"><Icon size={18} /></div><div><b>{title}</b><p>{text}</p></div><ChevronLeft size={16} /></button>; }
function TradeView() { return <div className="feature-view"><div className="feature-chart"><div className="chart-toolbar"><span>XAUUSD · M5</span><span>3,472.18 <em>+0.42%</em></span></div><div className="chart-grid"><div className="price-line line-one" /><div className="price-line line-two" /><div className="candles">{[38,54,44,66,52,72,61,78,69,88,76,92,81,96].map((h, i) => <i key={i} style={{ height: `${h}%` }} />)}</div><div className="chart-note"><Sparkles size={14} /> مساحة تحليل الشارت</div></div></div><div className="scenario-row"><span>سيناريو صاعد <b>مراقبة</b></span><span>سيناريو هابط <b>مراقبة</b></span><span>حماية <b>مفعلة</b></span></div></div>; }
function ResearchView() { return <div className="feature-view"><div className="research-search"><Search size={18} /><span>موضوع البحث والتحقق...</span><button><Play size={13} /> بدء</button></div><div className="research-columns"><div><small>المصادر</small><div className="source-box">مصدر 01 <i>قيد الانتظار</i></div><div className="source-box">مصدر 02 <i>قيد الانتظار</i></div><div className="source-box">مصدر 03 <i>قيد الانتظار</i></div></div><div className="evidence-box"><small>خريطة الأدلة</small><div className="evidence-core"><FileSearch size={22} /><b>النتيجة ستظهر هنا</b><span>جمع · مقارنة · تحقق · تقرير</span></div></div></div></div>; }
function VisionView() { return <div className="feature-view"><div className="drop-zone"><ImageIcon size={32} /><b>أسقط صورة أو مقطعًا هنا</b><span>واجهة التحليل البصري · OCR · فهم المشهد · المقارنة</span><button><Upload size={15} /> اختيار ملف</button></div><div className="vision-tools"><span><Camera size={15} /> صورة</span><span><Video size={15} /> فيديو</span><span><BarChart3 size={15} /> استخراج بيانات</span></div></div>; }
function VoiceView() { return <div className="feature-view voice-view"><div className="voice-orb"><Volume2 size={28} /><span>جاهز</span></div><div><b>جلسة صوتية</b><p>المحادثة، الاستماع، الإيقاف والاستئناف — عناصر الواجهة جاهزة للربط لاحقًا.</p><div className="voice-actions"><button><Mic size={16} /> بدء الميكروفون</button><button><Volume2 size={16} /> اختبار الصوت</button></div></div></div>; }
function ScreenView() { return <div className="feature-view screen-view"><div className="screen-preview"><div className="preview-bar"><i /><i /><i /><span>AMAR SCREEN SESSION</span></div><div className="preview-center"><MonitorUp size={32} /><b>مشاركة الشاشة</b><small>ستظهر معاينة الشاشة هنا</small></div></div><div className="privacy-note"><ShieldCheck size={16} /><span>وضع الخصوصية ظاهر دائمًا قبل بدء المشاركة.</span></div></div>; }
function FilesView() { return <div className="feature-view"><div className="file-drop"><Paperclip size={25} /><b>مصادر الجلسة</b><span>PDF · صور · نصوص · روابط · ملفات بحث</span><button><Upload size={15} /> إضافة مصدر</button></div><div className="file-tags"><span>مصادر موثوقة</span><span>قيد التحليل</span><span>محفوظة للجلسة</span></div></div>; }
function HistoryView({ events }: { events: AmarEvent[] }) { return <div className="feature-view history-view"><div className="report-card"><Activity size={20} /><div><b>تقارير الجلسات</b><p>ملخص التحليلات والبحوث والوسائط في سجل قابل للتوسعة.</p></div><button><FileSearch size={15} /> فتح</button></div>{events.length ? events.map((event, index) => <div className="history-row" key={`${event.type}-${index}`}><span>{event.type.replaceAll('_', ' ')}</span><small>{index + 1} · نشاط</small><ChevronLeft size={14} /></div>) : <div className="empty-feed">لا توجد جلسات بعد.</div>}</div>; }
