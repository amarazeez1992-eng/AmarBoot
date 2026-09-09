export type AmarSecurityState = { role: 'GUEST' | 'OPERATOR' | 'OWNER' | 'ADMIN'; mode: 'DEMO' | 'LOCAL' | 'LIVE'; sessionLocked: boolean; emergencyLock: boolean; auditEnabled: boolean };
export const DEMO_SECURITY: AmarSecurityState = { role: 'OWNER', mode: 'DEMO', sessionLocked: false, emergencyLock: false, auditEnabled: true };
export function isActionAllowed(state: AmarSecurityState, action: 'VIEW' | 'DEMO_CONTROL' | 'LIVE_TRADE' | 'SYSTEM'): boolean {
  if (state.sessionLocked || state.emergencyLock) return action === 'VIEW';
  if (action === 'VIEW') return true;
  if (action === 'DEMO_CONTROL') return state.role !== 'GUEST';
  if (action === 'SYSTEM') return state.role === 'ADMIN';
  return false;
}
