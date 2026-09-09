export type AmarIndicator = { id: string; name: string; family: 'TREND' | 'MOMENTUM' | 'VOLATILITY' | 'VOLUME' | 'STRUCTURE'; enabled: boolean };
export const AMAR_INDICATORS: AmarIndicator[] = [
  { id:'ema', name:'EMA', family:'TREND', enabled:true }, { id:'sma', name:'SMA', family:'TREND', enabled:true }, { id:'donchian', name:'Donchian', family:'TREND', enabled:true },
  { id:'rsi', name:'RSI', family:'MOMENTUM', enabled:true }, { id:'macd', name:'MACD', family:'MOMENTUM', enabled:true }, { id:'cci', name:'CCI', family:'MOMENTUM', enabled:true }, { id:'stochastic', name:'Stochastic', family:'MOMENTUM', enabled:true },
  { id:'atr', name:'ATR', family:'VOLATILITY', enabled:true }, { id:'bollinger', name:'Bollinger', family:'VOLATILITY', enabled:true }, { id:'vwap', name:'VWAP', family:'VOLUME', enabled:true },
  { id:'bos', name:'BOS', family:'STRUCTURE', enabled:true }, { id:'choch', name:'CHoCH', family:'STRUCTURE', enabled:true }, { id:'fvg', name:'FVG', family:'STRUCTURE', enabled:true }, { id:'order-blocks', name:'Order Blocks', family:'STRUCTURE', enabled:true }, { id:'liquidity', name:'Liquidity', family:'STRUCTURE', enabled:true }
];
