export type AmarExtension = {
  id: string;
  name: string;
  kind: 'INDICATOR' | 'STRATEGY' | 'BOT' | 'RISK' | 'CHART' | 'UTILITY' | 'ANALYTICS' | 'TRADING';
  version: string;
  enabled: boolean;
};

export const AMAR_EXTENSIONS: AmarExtension[] = [
  { id: 'ema', name: 'EMA', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'sma', name: 'SMA', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'rsi', name: 'RSI', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'macd', name: 'MACD', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'bollinger', name: 'Bollinger Bands', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'atr', name: 'ATR', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'vwap', name: 'VWAP', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'donchian', name: 'Donchian', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'bos', name: 'BOS', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'choch', name: 'CHoCH', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'fvg', name: 'FVG', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'order-blocks', name: 'Order Blocks', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'liquidity', name: 'Liquidity', kind: 'INDICATOR', version: '1.0.0', enabled: true },
  { id: 'strategy-smc', name: 'SMC Analysis', kind: 'STRATEGY', version: '0.1.0', enabled: true },
  { id: 'strategy-momentum', name: 'Momentum Scalping', kind: 'STRATEGY', version: '0.1.0', enabled: true },
  { id: 'strategy-mean-reversion', name: 'Mean Reversion', kind: 'STRATEGY', version: '0.1.0', enabled: true },
  { id: 'strategy-breakout', name: 'Breakout', kind: 'STRATEGY', version: '0.1.0', enabled: true },
  { id: 'amar-grid', name: 'AMAR GRID', kind: 'BOT', version: '1.0.0', enabled: true },
  { id: 'risk-guardian', name: 'Risk Guardian', kind: 'RISK', version: '0.1.0', enabled: true },
  { id: 'chart-tools', name: 'Chart Drawing Tools', kind: 'CHART', version: '0.1.0', enabled: true },
  { id: 'position-size', name: 'Position Size Calculator', kind: 'UTILITY', version: '0.1.0', enabled: true },
  { id: 'risk-calculator', name: 'Risk Calculator', kind: 'UTILITY', version: '0.1.0', enabled: true },
  { id: 'grid-calculator', name: 'Grid Calculator', kind: 'UTILITY', version: '0.1.0', enabled: true },
  { id: 'analytics-core', name: 'Performance Analytics', kind: 'ANALYTICS', version: '0.1.0', enabled: true },
  { id: 'trading-api', name: 'AMAR Trading API Boundary', kind: 'TRADING', version: '0.1.0', enabled: true },
];
