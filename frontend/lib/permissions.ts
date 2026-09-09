export type AmarRole = 'GUEST' | 'OPERATOR' | 'OWNER' | 'ADMIN';
export type AmarOperatingMode = 'DEMO' | 'LOCAL' | 'LIVE';

export const ROLE_PERMISSIONS: Record<AmarRole, readonly string[]> = {
  GUEST: ['view'],
  OPERATOR: ['view', 'demo-control'],
  OWNER: ['view', 'demo-control', 'configuration', 'risk-control', 'live-control'],
  ADMIN: ['view', 'demo-control', 'configuration', 'risk-control', 'live-control', 'system', 'security'],
};

export function can(role: AmarRole, permission: string): boolean {
  return ROLE_PERMISSIONS[role].includes(permission);
}

export function isLiveAllowed(role: AmarRole, mode: AmarOperatingMode): boolean {
  return mode === 'LIVE' && (role === 'OWNER' || role === 'ADMIN') && can(role, 'live-control');
}
