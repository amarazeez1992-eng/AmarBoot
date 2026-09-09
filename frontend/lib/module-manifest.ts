export type AmarModuleKind = 'ROOM' | 'INDICATOR' | 'STRATEGY' | 'BOT' | 'RISK' | 'ANALYTICS' | 'CHART' | 'TRADING' | 'UTILITY' | 'INTELLIGENCE';

export type AmarModuleManifest = {
  id: string;
  name: string;
  kind: AmarModuleKind;
  version: string;
  status: 'ACTIVE' | 'PLANNED' | 'DISABLED';
  demoOnly: boolean;
  dependencies: string[];
};

export const AMAR_MODULES: AmarModuleManifest[] = [
  { id: 'command-center', name: 'Command Center', kind: 'ROOM', version: '1.0.0', status: 'ACTIVE', demoOnly: true, dependencies: [] },
  { id: 'market', name: 'Market Room', kind: 'ROOM', version: '1.0.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events'] },
  { id: 'chart', name: 'Chart Room', kind: 'ROOM', version: '1.0.0', status: 'ACTIVE', demoOnly: true, dependencies: ['lightweight-charts'] },
  { id: 'bot-lab', name: 'Bot Lab', kind: 'ROOM', version: '1.0.0', status: 'ACTIVE', demoOnly: true, dependencies: ['amar-grid'] },
  { id: 'amar-grid', name: 'AMAR GRID', kind: 'BOT', version: '1.0.0', status: 'ACTIVE', demoOnly: true, dependencies: ['grid-engine'] },
  { id: 'indicators', name: 'Indicators', kind: 'INDICATOR', version: '1.0.0', status: 'ACTIVE', demoOnly: true, dependencies: [] },
  { id: 'analysis', name: 'Analysis', kind: 'INTELLIGENCE', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['indicators', 'events'] },
  { id: 'account', name: 'Account', kind: 'TRADING', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events'] },
  { id: 'risk', name: 'Risk Guardian', kind: 'RISK', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events'] },
  { id: 'positions', name: 'Positions & Orders', kind: 'TRADING', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events'] },
  { id: 'performance', name: 'Performance', kind: 'ANALYTICS', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events'] },
  { id: 'testing', name: 'Testing & Simulation', kind: 'ANALYTICS', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events'] },
  { id: 'tools', name: 'Trading Tools', kind: 'UTILITY', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: [] },
  { id: 'alerts', name: 'Alerts', kind: 'UTILITY', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events'] },
  { id: 'settings', name: 'Settings & Theme Studio', kind: 'ROOM', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['motion'] },
  { id: 'library', name: 'AMAR Library', kind: 'ROOM', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['extensions'] },
  { id: 'intelligence', name: 'AMAR Intelligence', kind: 'INTELLIGENCE', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: ['events', 'risk', 'analytics'] },
  { id: 'websocket-transport', name: 'WebSocket Transport', kind: 'TRADING', version: '0.1.0', status: 'ACTIVE', demoOnly: true, dependencies: [] },
  { id: 'broker-adapter', name: 'Broker Adapter Boundary', kind: 'TRADING', version: '0.1.0', status: 'PLANNED', demoOnly: false, dependencies: ['trading-api'] },
  { id: 'trading-api', name: 'AMAR Trading API', kind: 'TRADING', version: '0.1.0', status: 'PLANNED', demoOnly: false, dependencies: [] },
  { id: 'mt5-connector', name: 'MT5 Connector', kind: 'TRADING', version: '0.1.0', status: 'PLANNED', demoOnly: false, dependencies: ['broker-adapter'] },
];
