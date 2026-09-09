'use client';
import { useEffect, useMemo, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { ArrowUpRight, Bell, CircleDot, Crosshair, LockKeyhole, Radio, Scan, Settings2, ShieldCheck, Sparkles } from 'lucide-react';
import AmarCore3D from '../components/AmarCore3D';
import AmarSpatialNexus, { nexusNodes } from '../components/AmarSpatialNexus';
import AmarChartWorld from '../components/AmarChartWorld';
import AmarDecisionStream from '../components/AmarDecisionStream';
import AmarRiskPulse from '../components/AmarRiskPulse';
import { amarDemoStream, type AmarEvent } from '../lib/events';

const telemetry = [['XAUUSD','3472.18','+0.42%'],['EURUSD','1.1718','+0.08%'],['BTCUSD','112842','-0.31%'],['NAS100','24186','+0.22%']];

export default function Page(){
 const [entered,setEntered]=useState(false),[active,setActive]=useState('intelligence'),[events,setEvents]=useState<AmarEvent[]>([]),[equity,setEquity]=useState(1012.84),[confidence,setConfidence]=useState(82),[pulse,setPulse]=useState(.52);
 useEffect(()=>{
   const onEvent=(event: AmarEvent)=>{
     setEvents(c=>[event,...c].slice(0,14));
     setPulse(Math.min(1,.32+Math.random()*.68));
     if(event.type==='ACCOUNT_UPDATE'&&typeof event.payload.equity==='number')setEquity(event.payload.equity);
     if(event.type==='SIGNAL_UPDATE'&&typeof event.payload.confidence==='number')setConfidence(event.payload.confidence);
   };
   amarDemoStream.connect(onEvent);
   if(typeof window!=='undefined'&&'serviceWorker' in navigator)navigator.serviceWorker.register('/sw.js').catch(()=>undefined);
   return ()=>{ amarDemoStream.disconnect(onEvent); };
 },[]);
 const activeNode=useMemo(()=>nexusNodes.find(n=>n.id===active)??nexusNodes[0],[active]); const ActiveIcon=activeNode.icon;
 if(!entered)return <main className="amar-portal"><div className="portal-vortex"/><div className="portal-lines"/><div className="portal-center"><div className="portal-emblem"><span>A</span></div><small>AMAR INTELLIGENT TRADING SYSTEM</small><h1>THE <span>FIELD</span></h1><p>A spatial command environment for an evolving trading intelligence.</p><button onClick={()=>setEntered(true)}>ENTER THE FIELD <ArrowUpRight size={17}/></button><div className="portal-status"><i/> CORE READY · GUARDIAN ARMED · DEMO SAFE</div></div><div className="portal-corner top">NEXUS / 0001</div><div className="portal-corner bottom">LOCAL SIMULATION · NO MARKET EXECUTION</div></main>;
 return <main className="amar-universe"><div className="universe-noise"/><div className="aurora-field aurora-one"/><div className="aurora-field aurora-two"/><header className="universe-header"><div className="brand-lockup"><div className="brand-mark">A</div><div><b>AMAR</b><span>INTELLIGENT TRADING OS</span></div></div><div className="telemetry-strip">{telemetry.map(([s,p,c])=><div key={s}><small>{s}</small><strong>{p}</strong><em>{c}</em></div>)}</div><div className="header-actions"><span className="safe-state"><i/> DEMO / SAFE</span><button><Bell size={16}/></button><button><Settings2 size={16}/></button></div></header><section className="universe-grid"><aside className="left-rail"><div className="rail-caption">SPATIAL CONTROL</div><div className="rail-orb"><Scan size={18}/><span>FIELD</span><b>01</b></div><div className="rail-orb active"><Crosshair size={18}/><span>FOCUS</span><b>LIVE</b></div><div className="rail-orb"><Radio size={18}/><span>STREAM</span><b>ON</b></div><div className="rail-orb"><LockKeyhole size={18}/><span>GUARD</span><b>ARMED</b></div><div className="rail-bottom"><span>AMAR</span><small>v∞.1</small></div></aside><section className="universe-stage"><div className="stage-command"><span>LIVE COGNITIVE FIELD</span><b>{activeNode.label}</b><em>/{String(activeNode.signal).padStart(2,'0')} SIGNAL</em></div><div className="nexus-wrap"><AmarSpatialNexus active={active} onSelect={setActive}/></div><div className="core-dock"><div className="core-visual"><AmarCore3D activity={pulse}/></div><div className="core-readout"><small>AMAR CORE ACTIVITY</small><strong>{Math.round(pulse*100)}%</strong><span><i/> EVENT STREAM NOMINAL</span></div><div className="core-readout"><small>DECISION CONFIDENCE</small><strong>{confidence}%</strong><span>BUY BIAS · XAUUSD</span></div></div></section><aside className="right-observatory"><div className="observatory-head"><span>OBSERVATORY</span><b>01 / 04</b></div><AnimatePresence mode="wait"><motion.div key={activeNode.id} className="focus-window" initial={{opacity:0,y:14}} animate={{opacity:1,y:0}} exit={{opacity:0,y:-10}}><div className="focus-icon"><ActiveIcon size={22}/></div><small>SELECTED NODE</small><h2>{activeNode.label}</h2><div className="signal-meter"><span style={{width:`${activeNode.signal}%`}}/></div><div className="focus-numbers"><b>{activeNode.signal}%</b><span>coherence</span></div></motion.div></AnimatePresence><div className="event-window"><div className="window-title">EVENT CURRENT <span>STREAMING</span></div>{events.slice(0,6).map((event,i)=><motion.div key={`${event.ts}-${i}`} initial={{opacity:0,x:18}} animate={{opacity:1,x:0}}><CircleDot size={9}/><b>{event.type.replaceAll('_',' ')}</b><small>{Object.values(event.payload).slice(0,1).map(String).join('')}</small></motion.div>)}</div><div className="integrity"><ShieldCheck size={15}/><span>GUARDIAN</span><b>ARMED</b></div></aside></section><section className="command-modules"><AmarChartWorld/><AmarDecisionStream events={events}/><AmarRiskPulse risk={5} drawdown={2.1} exposure={18}/></section><footer className="universe-footer"><div><span>MODE</span><b>DEMO / LOCAL / SIMULATED</b></div><div><span>EQUITY</span><b>${equity.toFixed(2)}</b></div><div><span>OPEN POSITIONS</span><b>06</b></div><div><span>RISK PRESET</span><b>5%</b></div><div className="footer-hint"><Sparkles size={13}/> SELECT A NODE TO RECONFIGURE THE FIELD</div><button><ArrowUpRight size={15}/></button></footer></main>;
}
