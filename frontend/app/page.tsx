'use client';

import { useEffect, useMemo, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { Activity, Bell, Bot, BrainCircuit, ChartNoAxesCombined, Gauge, ShieldCheck, Sparkles, TestTube2, Wrench } from 'lucide-react';
import AmarCore3D from '../components/AmarCore3D';
import { amarDemoStream, type AmarEvent } from '../lib/events';

const rooms = [
  ['Command Center', BrainCircuit], ['Market', Activity], ['Chart', ChartNoAxesCombined], ['Bot Lab', Bot],
  ['Indicators', Sparkles], ['Analysis', Gauge], ['Risk', ShieldCheck], ['Positions', Activity],
  ['Performance', ChartNoAxesCombined], ['Testing', TestTube2], ['Tools', Wrench], ['Alerts', Bell]
] as const;

export default function Page() {
  const [entered, setEntered] = useState(false);
  const [room, setRoom] = useState(0);
  const [events, setEvents] = useState<AmarEvent[]>([]);
  const [equity, setEquity] = useState(1012.84);
  const [confidence, setConfidence] = useState(82);

  useEffect(() => {
    const disconnect = amarDemoStream.connect(event => {
      setEvents(current => [event, ...current].slice(0, 10));
      if (event.type === 'ACCOUNT_UPDATE' && typeof event.payload.equity === 'number') setEquity(event.payload.equity);
      if (event.type === 'SIGNAL_UPDATE' && typeof event.payload.confidence === 'number') setConfidence(event.payload.confidence);
    });
    return () => { disconnect(); };
  }, []);

  const activity = useMemo(() => Math.min(1, events.length / 10), [events.length]);

  if (!entered) return (
    <main className="portal">
      <div className="aurora" />
      <div className="portal-grid" />
      <motion.div className="portal-core" animate={{ scale: [0.98, 1.05, 0.98], rotate: [0, 2, -2, 0] }} transition={{ duration: 5, repeat: Infinity }}>
        <div className="core-ring" /><div className="core-dot" />
      </motion.div>
      <section className="portal-copy">
        <div className="eyebrow">AMAR INTELLIGENT TRADING SYSTEM</div>
        <h1>AMAR <span>AURORA</span></h1>
        <p>Trading Command OS · Nexus Spatial Interface</p>
        <button className="enter" onClick={() => setEntered(true)}>دخول إلى النظام <span>↗</span></button>
        <div className="safe">DEMO · LOCAL · SAFE · SIMULATED EVENTS</div>
      </section>
    </main>
  );

  const [title] = rooms[room];
  return (
    <main className="os">
      <div className="aurora" />
      <header className="topbar glass">
        <div><b>AMAR / NEXUS</b><small> {title} · XAUUSD · M5</small></div>
        <div className="status"><i /> DEMO · SIMULATED WS · NOMINAL</div>
      </header>
      <section className="nexus-layout">
        <aside className="room-orbit glass">
          <div className="orbit-label">NEXUS ROOMS</div>
          {rooms.map(([name, Icon], i) => <motion.button key={name} className={i === room ? 'room active' : 'room'} onClick={() => setRoom(i)} whileHover={{ scale: 1.04, x: -3 }} whileTap={{ scale: .97 }}>
            <Icon size={17} /><span>{name}</span><em>{i === room ? 'LIVE' : String(i + 1).padStart(2, '0')}</em>
          </motion.button>)}
        </aside>

        <section className="stage">
          <div className="stage-head"><div><div className="eyebrow">LIVING WORKSPACE</div><h2>{title}</h2></div><div className="live-pill">● LIVE DEMO</div></div>
          <div className="core-stage glass">
            <AmarCore3D activity={activity} />
            <div className="core-hud left"><b>XAUUSD</b><span>3472.18</span><small>SPREAD 0.12</small></div>
            <div className="core-hud right"><b>AMAR SCORE</b><span>{confidence}%</span><small>BUY BIAS</small></div>
          </div>
          <div className="metric-row">
            <Metric label="EQUITY" value={`$${equity.toFixed(2)}`} tone="cyan" />
            <Metric label="FLOATING P/L" value="+$12.84" tone="green" />
            <Metric label="POSITIONS" value="06" tone="violet" />
            <Metric label="RISK" value="5%" tone="gold" />
          </div>
        </section>

        <aside className="right-stack">
          <Panel title="DECISION FEED"><Feed events={events} /></Panel>
          <Panel title="SYSTEM MATRIX"><div className="matrix"><span>MARKET <b>ONLINE</b></span><span>AMAR GRID <b>READY</b></span><span>RISK GUARDIAN <b>ARMED</b></span><span>MT5 <b>NOT CONNECTED</b></span></div></Panel>
          <Panel title="MOTION ENGINE"><div className="motion-bars"><span /><i /><b /><em /> <small>CINEMATIC</small></div></Panel>
        </aside>
      </section>
    </main>
  );
}

function Metric({ label, value, tone }: { label: string; value: string; tone: string }) { return <motion.div className={`metric glass ${tone}`} animate={{ y: [0, -2, 0] }} transition={{ duration: 3.2, repeat: Infinity }}><small>{label}</small><strong>{value}</strong></motion.div>; }
function Panel({ title, children }: { title: string; children: React.ReactNode }) { return <section className="panel glass"><div className="panel-title">{title}<span>LIVE</span></div>{children}</section>; }
function Feed({ events }: { events: AmarEvent[] }) { return <div className="feed">{events.slice(0, 5).map((e, i) => <motion.div key={`${e.ts}-${i}`} initial={{ opacity: 0, x: 12 }} animate={{ opacity: 1, x: 0 }}><b>{e.type.replace('_', ' ')}</b><span>{Object.entries(e.payload).slice(0, 2).map(([k, v]) => `${k}: ${String(v).slice(0, 16)}`).join(' · ')}</span></motion.div>)}</div>; }
