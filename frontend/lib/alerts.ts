export type AmarAlertType = 'BOT' | 'TRADE' | 'RISK' | 'ACCOUNT' | 'CONNECTION' | 'GRID' | 'TRAILING' | 'NEWS';
export type AmarAlertRule = { id: string; type: AmarAlertType; enabled: boolean; threshold?: number; message: string };
export const DEMO_ALERT_RULES: AmarAlertRule[] = [
  { id: 'risk-limit', type: 'RISK', enabled: true, threshold: 5, message: 'Risk limit reached' },
  { id: 'basket-tp', type: 'TRADE', enabled: true, threshold: 100, message: 'Basket TP reached' },
  { id: 'basket-sl', type: 'TRADE', enabled: true, threshold: -100, message: 'Basket SL reached' },
  { id: 'grid-rebuild', type: 'GRID', enabled: true, message: 'Grid rebuild event' },
];
