'use client';

import { motion } from 'framer-motion';
import type { AmarEvent } from '../lib/events';

export default function AmarDecisionStream({ events }: { events: AmarEvent[] }) {
  const decisions = events.filter(e => e.type === 'DECISION_EVENT' || e.type === 'SIGNAL_UPDATE' || e.type === 'RISK_UPDATE').slice(0, 8);
  return <section className="decision-stream"><header><span>DECISION STREAM</span><b>LIVE / DEMO</b></header>{decisions.length === 0 ? <div className="decision-empty">Waiting for AMAR intelligence events…</div> : decisions.map((event, i) => <motion.article key={`${event.ts}-${i}`} initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }}><span className="decision-dot" /><div><strong>{event.type.replaceAll('_', ' ')}</strong><small>{Object.entries(event.payload).map(([k,v]) => `${k}: ${v}`).join(' · ')}</small></div></motion.article>)}</section>;
}
