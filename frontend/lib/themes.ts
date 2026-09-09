export type AmarThemeId = 'AURORA' | 'CYBER' | 'OCEAN' | 'EMERALD' | 'VIOLET' | 'GOLD' | 'PLASMA' | 'MIDNIGHT' | 'GLASS' | 'CARBON';

export type AmarTheme = { id: AmarThemeId; label: string; primary: string; secondary: string; accent: string };

export const AMAR_THEMES: AmarTheme[] = [
  { id: 'AURORA', label: 'Aurora', primary: '#67f5ff', secondary: '#8b5cf6', accent: '#22d3ee' },
  { id: 'CYBER', label: 'Cyber', primary: '#00e5ff', secondary: '#ff00cc', accent: '#7c3aed' },
  { id: 'OCEAN', label: 'Ocean', primary: '#38bdf8', secondary: '#2563eb', accent: '#14b8a6' },
  { id: 'EMERALD', label: 'Emerald', primary: '#34d399', secondary: '#059669', accent: '#a3e635' },
  { id: 'VIOLET', label: 'Violet', primary: '#a78bfa', secondary: '#7c3aed', accent: '#e879f9' },
  { id: 'GOLD', label: 'Gold', primary: '#facc15', secondary: '#f59e0b', accent: '#fde68a' },
  { id: 'PLASMA', label: 'Plasma', primary: '#fb7185', secondary: '#c026d3', accent: '#22d3ee' },
  { id: 'MIDNIGHT', label: 'Midnight', primary: '#94a3b8', secondary: '#334155', accent: '#38bdf8' },
  { id: 'GLASS', label: 'Glass', primary: '#e2e8f0', secondary: '#94a3b8', accent: '#67e8f9' },
  { id: 'CARBON', label: 'Carbon', primary: '#f8fafc', secondary: '#475569', accent: '#f59e0b' },
];
