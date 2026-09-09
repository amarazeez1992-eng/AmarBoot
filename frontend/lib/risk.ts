export type RiskLimits = { riskPercent: number; maxDrawdownPercent: number; maxPositions: number; maxExposureLots: number; maxSpread: number; emergencyStop: boolean };
export const DEMO_RISK_LIMITS: RiskLimits = { riskPercent: 5, maxDrawdownPercent: 10, maxPositions: 30, maxExposureLots: 1, maxSpread: 0.5, emergencyStop: false };
export function riskHealth(drawdown: number, limits = DEMO_RISK_LIMITS): number { return Math.max(0, Math.min(100, 100 - (drawdown / limits.maxDrawdownPercent) * 100)); }
export function riskGate(drawdown: number, limits = DEMO_RISK_LIMITS): 'ALLOW' | 'BLOCK' { return limits.emergencyStop || drawdown >= limits.maxDrawdownPercent ? 'BLOCK' : 'ALLOW'; }
