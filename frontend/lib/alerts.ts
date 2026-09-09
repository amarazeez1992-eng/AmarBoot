export type AmarAlertType = 'BOT' | 'TRADE' | 'RISK' | 'ACCOUNT' | 'CONNECTION' | 'GRID' | 'TRAILING' | 'NEWS';
export type AmarAlertRule = { id: string; type: AmarAlertType; enabled: boolean; threshold?: number; message: string };

export const DEMO_ALERT_RULES: AmarAlertRule[] = [
  { id: 'bot-start', type: 'BOT', enabled: true, message: 'Bot started' },
  { id: 'bot-stop', type: 'BOT', enabled: true, message: 'Bot stopped' },
  { id: 'trade-tp', type: 'TRADE', enabled: true, message: 'Take profit reached' },
  { id: 'trade-sl', type: 'TRADE', enabled: true, message: 'Stop loss reached' },
  { id: 'basket-tp', type: 'TRADE', enabled: true, threshold: 100, message: 'Basket TP reached' },
  { id: 'basket-sl', type: 'TRADE', enabled: true, threshold: -100, message: 'Basket SL reached' },
  { id: 'risk-limit', type: 'RISK', enabled: true, threshold: 5, message: 'Risk limit reached' },
  { id: 'drawdown-limit', type: 'RISK', enabled: true, threshold: 10, message: 'Drawdown limit reached' },
  { id: 'margin-warning', type: 'RISK', enabled: true, threshold: 30, message: 'Free-margin warning' },
  { id: 'account-update', type: 'ACCOUNT', enabled: true, message: 'Account state updated' },
  { id: 'connection-lost', type: 'CONNECTION', enabled: true, message: 'Trading connection lost' },
  { id: 'connection-restored', type: 'CONNECTION', enabled: true, message: 'Trading connection restored' },
  { id: 'grid-rebuild', type: 'GRID', enabled: true, message: 'Grid rebuild event' },
  { id: 'trailing', type: 'TRAILING', enabled: true, message: 'Trailing protection updated' },
  { id: 'news-risk', type: 'NEWS', enabled: true, message: 'High-impact news risk detected' },
];
