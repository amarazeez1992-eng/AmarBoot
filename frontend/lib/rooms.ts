export type AmarRoom = {
  id: string;
  label: string;
  sections: string[];
};

export const AMAR_ROOMS: AmarRoom[] = [
  { id: 'command-center', label: 'Command Center', sections: ['Overview', 'Live Telemetry', 'Decision Feed', 'System Status'] },
  { id: 'market', label: 'Market Room', sections: ['Watchlist', 'Symbols', 'Sessions', 'Spread', 'Range', 'Market Status'] },
  { id: 'chart', label: 'Chart Room', sections: ['Workspace', 'Indicators', 'Structure', 'Orders', 'Drawings', 'Layouts'] },
  { id: 'bot-lab', label: 'Bot Lab', sections: ['Overview', 'Grid Settings', 'Individual Trade', 'Basket', 'Risk', 'Trailing', 'Enhancement', 'Rebuild', 'Manual Control', 'Orders', 'Performance', 'Simulation', 'Templates', 'Advanced'] },
  { id: 'indicators', label: 'Indicators', sections: ['Trend', 'Momentum', 'Volatility', 'Volume', 'Market Structure'] },
  { id: 'analysis', label: 'Analysis', sections: ['Trend', 'Momentum', 'Volatility', 'Structure', 'Volume', 'Multi-Timeframe', 'Sessions', 'Market Regime', 'AMAR Score'] },
  { id: 'account', label: 'Account', sections: ['Balance', 'Equity', 'Margin', 'Broker', 'Connection', 'Permissions'] },
  { id: 'risk', label: 'Risk', sections: ['Risk %', 'Daily Loss', 'Drawdown', 'Exposure', 'Margin', 'Protections', 'Emergency Stop'] },
  { id: 'positions', label: 'Positions & Orders', sections: ['Positions', 'Pending Orders', 'History', 'Basket', 'Manual Actions'] },
  { id: 'performance', label: 'Performance', sections: ['Daily', 'Weekly', 'Monthly', 'Yearly', 'Total', 'Equity Curve', 'Trade Distribution'] },
  { id: 'testing', label: 'Testing', sections: ['Backtest', 'Forward Test', 'Demo', 'Monte Carlo', 'Parameters', 'Optimization', 'Drawdown Analysis'] },
  { id: 'tools', label: 'Tools', sections: ['Position Size', 'Risk', 'Lot', 'Pip', 'Margin', 'TP/SL', 'Profit', 'Drawdown', 'Compound', 'Grid', 'Martingale', 'Session Clock'] },
  { id: 'alerts', label: 'Alerts', sections: ['Rules', 'Trade', 'Risk', 'Connection', 'Grid', 'Trailing', 'News', 'History'] },
  { id: 'settings', label: 'Settings', sections: ['Appearance', 'Motion', 'Themes', 'Security', 'Permissions', 'Notifications', 'Data'] },
  { id: 'library', label: 'AMAR Library', sections: ['Indicators', 'Strategies', 'Bots', 'Risk', 'Charts', 'Analytics', 'Utilities', 'Extensions'] },
  { id: 'intelligence', label: 'AMAR Intelligence', sections: ['Memory', 'Knowledge', 'Analytics', 'Decision Engine', 'Confidence', 'Explainability', 'Guardian', 'Ethics', 'Evolution'] },
];
