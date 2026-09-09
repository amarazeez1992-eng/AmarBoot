export type AmarExtension = { id: string; name: string; kind: 'INDICATOR' | 'STRATEGY' | 'BOT' | 'RISK' | 'CHART' | 'UTILITY'; version: string; enabled: boolean };
export const AMAR_EXTENSIONS: AmarExtension[] = [
  { id: 'ema', name: 'EMA', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'rsi', name: 'RSI', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'vwap', name: 'VWAP', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'fvg', name: 'FVG', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'amar-grid', name: 'AMAR GRID', kind: 'BOT', version: '1.0.0', enabled: true },
];
