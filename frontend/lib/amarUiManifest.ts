export type AmarEngine = 'android-compose' | 'godot-3d' | 'web-three' | 'future-native';

export type AmarRoom =
  | 'home' | 'market' | 'chart' | 'bot' | 'risk' | 'analysis'
  | 'performance' | 'testing' | 'tools' | 'alerts' | 'settings' | 'library';

export interface AmarUiManifest {
  version: string;
  mode: 'demo' | 'live';
  engines: AmarEngine[];
  rooms: AmarRoom[];
  motion: 'off' | 'low' | 'medium' | 'high' | 'cinematic';
  tradingDecisionSource: 'engine-only';
}

export const AMAR_UI_MANIFEST: AmarUiManifest = {
  version: '1.0',
  mode: 'demo',
  engines: ['android-compose', 'godot-3d', 'web-three'],
  rooms: [
    'home', 'market', 'chart', 'bot', 'risk', 'analysis',
    'performance', 'testing', 'tools', 'alerts', 'settings', 'library'
  ],
  motion: 'high',
  tradingDecisionSource: 'engine-only',
};

export const AMAR_UI_RULES = Object.freeze({
  demoMustNotTrade: true,
  uiMustNotChooseBuySell: true,
  visualModulesMustBeReplaceable: true,
  themeMustBeIndependent: true,
  liveConnectionMustBeExplicit: true,
});
