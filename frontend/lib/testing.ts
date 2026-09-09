export type AmarTestMode = 'BACKTEST' | 'FORWARD' | 'DEMO' | 'MONTE_CARLO' | 'OPTIMIZATION';
export type AmarTestConfig = { mode: AmarTestMode; symbol: string; timeframe: string; from: string; to: string; startingBalance: number };
export const DEMO_TEST_CONFIG: AmarTestConfig = { mode: 'DEMO', symbol: 'XAUUSD', timeframe: 'M5', from: '2026-01-01', to: '2026-12-31', startingBalance: 1000 };
