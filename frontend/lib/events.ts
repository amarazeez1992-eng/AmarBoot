export type AmarEventType =
  | 'MARKET_UPDATE'
  | 'SIGNAL_UPDATE'
  | 'RISK_UPDATE'
  | 'BOT_UPDATE'
  | 'DECISION_EVENT'
  | 'POSITION_UPDATE'
  | 'ACCOUNT_UPDATE'
  | 'ALERT_EVENT'
  | 'SYSTEM_STATUS';

export type AmarEvent = {
  type: AmarEventType;
  ts: number;
  payload: Record<string, string | number | boolean>;
};

export const demoEvent = (tick: number): AmarEvent[] => [
  { type: 'MARKET_UPDATE', ts: Date.now(), payload: { symbol: 'XAUUSD', bid: 3472.18 + Math.sin(tick / 5) * 1.7, ask: 3472.30 + Math.sin(tick / 5) * 1.7, spread: 0.12 } },
  { type: 'SIGNAL_UPDATE', ts: Date.now(), payload: { direction: tick % 11 < 8 ? 'BUY' : 'NEUTRAL', confidence: 78 + (tick % 9) } },
  { type: 'RISK_UPDATE', ts: Date.now(), payload: { mode: 'SAFE', riskPercent: 5, drawdown: 2.1 } },
  { type: 'BOT_UPDATE', ts: Date.now(), payload: { name: 'AMAR GRID', state: tick % 13 === 0 ? 'ANALYZING' : 'READY', positions: 6, pending: 18 } },
  { type: 'ACCOUNT_UPDATE', ts: Date.now(), payload: { balance: 1000, equity: 1012.84 + Math.sin(tick / 4) * 2.4, floating: 12.84 + Math.sin(tick / 3) * 1.5 } },
  { type: 'SYSTEM_STATUS', ts: Date.now(), payload: { mode: 'DEMO', transport: 'SIMULATED_WS', health: 'NOMINAL' } }
];

export class AmarDemoStream {
  private timer: ReturnType<typeof setInterval> | undefined;
  private listeners = new Set<(event: AmarEvent) => void>();
  private tick = 0;

  connect(listener: (event: AmarEvent) => void) {
    this.listeners.add(listener);
    this.timer ??= setInterval(() => {
      const events = demoEvent(this.tick++);
      events.forEach(event => this.listeners.forEach(fn => fn(event)));
    }, 900);
    return () => this.listeners.delete(listener);
  }

  disconnect() {
    if (this.listeners.size === 0 && this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }
}

export const amarDemoStream = new AmarDemoStream();
