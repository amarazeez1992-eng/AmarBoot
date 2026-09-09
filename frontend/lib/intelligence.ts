export type AmarDecision = { direction: 'BUY' | 'SELL' | 'NEUTRAL'; confidence: number; rationale: string[]; riskState: 'SAFE' | 'WARNING' | 'EMERGENCY' };

export type AmarIntelligenceSnapshot = {
  memoryReady: boolean;
  knowledgeReady: boolean;
  analyticsReady: boolean;
  decisionReady: boolean;
  confidence: number;
  explainability: string[];
  ethicsGate: 'PASS' | 'BLOCK';
};

export const DEMO_INTELLIGENCE: AmarIntelligenceSnapshot = {
  memoryReady: true,
  knowledgeReady: true,
  analyticsReady: true,
  decisionReady: true,
  confidence: 82,
  explainability: ['Market structure aligned', 'Momentum positive', 'Risk within demo limit'],
  ethicsGate: 'PASS',
};

export function demoDecision(): AmarDecision {
  return { direction: 'BUY', confidence: DEMO_INTELLIGENCE.confidence, rationale: DEMO_INTELLIGENCE.explainability, riskState: 'SAFE' };
}
