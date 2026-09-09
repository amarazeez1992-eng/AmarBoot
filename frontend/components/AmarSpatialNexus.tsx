'use client';

import { motion, useMotionValue, useTransform } from 'framer-motion';
import type { LucideIcon } from 'lucide-react';
import { Activity, Bot, BrainCircuit, ChartNoAxesCombined, Gauge, ShieldCheck, Sparkles, TestTube2, Wrench } from 'lucide-react';

export type NexusNode = {
  id: string;
  label: string;
  icon: LucideIcon;
  orbit: number;
  angle: number;
  signal: number;
};

export const nexusNodes: NexusNode[] = [
  { id: 'intelligence', label: 'INTELLIGENCE', icon: BrainCircuit, orbit: 1, angle: -82, signal: 94 },
  { id: 'market', label: 'MARKET', icon: Activity, orbit: 2, angle: -42, signal: 88 },
  { id: 'chart', label: 'CHART', icon: ChartNoAxesCombined, orbit: 1, angle: -8, signal: 91 },
  { id: 'trading', label: 'TRADING', icon: Bot, orbit: 2, angle: 26, signal: 82 },
  { id: 'risk', label: 'RISK', icon: ShieldCheck, orbit: 1, angle: 58, signal: 97 },
  { id: 'analytics', label: 'ANALYTICS', icon: Gauge, orbit: 2, angle: 100, signal: 86 },
  { id: 'testing', label: 'TESTING', icon: TestTube2, orbit: 1, angle: 142, signal: 73 },
  { id: 'tools', label: 'TOOLS', icon: Wrench, orbit: 2, angle: 184, signal: 79 },
  { id: 'library', label: 'LIBRARY', icon: Sparkles, orbit: 1, angle: 222, signal: 90 },
];

export default function AmarSpatialNexus({ active, onSelect }: { active: string; onSelect: (id: string) => void }) {
  const x = useMotionValue(0);
  const y = useMotionValue(0);
  const rotateX = useTransform(y, [-180, 180], [8, -8]);
  const rotateY = useTransform(x, [-180, 180], [-10, 10]);

  return (
    <div className="spatial-nexus" onPointerMove={event => {
      const rect = event.currentTarget.getBoundingClientRect();
      x.set(event.clientX - rect.left - rect.width / 2);
      y.set(event.clientY - rect.top - rect.height / 2);
    }}>
      <motion.div className="nexus-plane" style={{ rotateX, rotateY }}>
        <div className="nexus-void" />
        <div className="nexus-ring ring-a" />
        <div className="nexus-ring ring-b" />
        <div className="nexus-ring ring-c" />
        <div className="nexus-vector vector-a" />
        <div className="nexus-vector vector-b" />
        {nexusNodes.map(node => {
          const Icon = node.icon;
          const radius = node.orbit === 1 ? 29 : 42;
          const radians = (node.angle * Math.PI) / 180;
          const left = 50 + Math.cos(radians) * radius;
          const top = 50 + Math.sin(radians) * radius;
          const selected = active === node.id;
          return (
            <motion.button
              key={node.id}
              className={`nexus-node ${selected ? 'selected' : ''}`}
              style={{ left: `${left}%`, top: `${top}%` }}
              onClick={() => onSelect(node.id)}
              whileHover={{ scale: 1.14, z: 30 }}
              whileTap={{ scale: .94 }}
              animate={{ y: [0, node.orbit === 1 ? -4 : 4, 0], rotate: [0, node.orbit === 1 ? 1 : -1, 0] }}
              transition={{ duration: 4 + node.orbit, repeat: Infinity, ease: 'easeInOut' }}
            >
              <span className="node-pulse" style={{ opacity: node.signal / 150 }} />
              <Icon size={17} strokeWidth={1.6} />
              <b>{node.label}</b>
              <small>{node.signal}%</small>
            </motion.button>
          );
        })}
        <motion.div className="nexus-core-anchor" animate={{ scale: [1, 1.035, 1] }} transition={{ duration: 2.8, repeat: Infinity }}>
          <div className="anchor-halo" />
          <div className="anchor-inner"><span>AMAR</span><strong>AI</strong><small>LIVE CORE</small></div>
        </motion.div>
      </motion.div>
      <div className="nexus-instruction">MOVE THROUGH THE FIELD · SELECT A NODE · THE SYSTEM FOLLOWS</div>
    </div>
  );
}
