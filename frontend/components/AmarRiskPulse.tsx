'use client';

import { motion } from 'framer-motion';

export default function AmarRiskPulse({ risk = 5, drawdown = 2.1, exposure = 18 }: { risk?: number; drawdown?: number; exposure?: number }) {
  const health = Math.max(0, Math.min(100, 100 - drawdown * 8 - risk * 2));
  return <section className="risk-pulse">
    <div className="risk-pulse-ring"><motion.span animate={{ scale: [1, 1.08, 1], opacity: [0.45, 0.9, 0.45] }} transition={{ repeat: Infinity, duration: 2.4 }} /></div>
    <div className="risk-pulse-data"><small>RISK GUARDIAN</small><strong>{health.toFixed(0)}%</strong><span>HEALTH / SAFE</span></div>
    <div className="risk-pulse-metrics"><div><b>{risk}%</b><small>risk</small></div><div><b>{drawdown}%</b><small>drawdown</small></div><div><b>{exposure}</b><small>exposure</small></div></div>
  </section>;
}
