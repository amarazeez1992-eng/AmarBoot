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
  { id: 'chart', name: 'Chart World', kind: 'CHART', version: '0.1.0', status: 'PLANNED', demoOnly: true, dependencies: [] },
  { id: 'amar-grid', name: 'AMAR GRID', kind: 'BOT', version: '1.0.0', status: 'ACTIVE', demoOnly: true, dependencies: ['grid-engine'] },
  { id: 'risk', name: 'Risk Engine', kind: 'RISK', version: '0.1.0', status: 'PLANNED', demoOnly: true, dependencies: ['events'] },
  { id: 'analytics', name: 'Analytics Engine', kind: 'ANALYTICS', version: '0.1.0', status: 'PLANNED', demoOnly: true, dependencies: ['events'] },
  { id: 'intelligence', name: 'AMAR Intelligence', kind: 'INTELLIGENCE', version: '0.1.0', status: 'PLANNED', demoOnly: true, dependencies: ['events'] },
];
