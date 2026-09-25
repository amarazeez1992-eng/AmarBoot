# Stage 2 — Cognitive Implementation Registry

Status: Infrastructure Prepared — Implementation Pending

| Capability | الحكم | الملف | الحالة |
|---:|---|---|---|
| 1 Intent Understanding | Extend | `agent/AmarIntentUnderstanding.kt` | ⏸ |
| 2 Semantic & Literal Understanding | New | `cognitive/new/Capability02SemanticLiteralUnderstanding.kt` | ⏸ |
| 3 Context Awareness | Partial | `agent/AmarAgentOrchestrator.kt; agent/AmarAgentSession.kt` | ⏸ |
| 4 Temporal Awareness | Partial | `agent/AmarAgentOrchestrator.kt; evidence freshness` | ⏸ |
| 5 Goal Awareness | Partial | `agent/AmarAgentPlanner.kt` | ⏸ |
| 6 Knowledge-State Awareness | Partial | `agent/AmarKnowledgeEngine.kt; agent/AmarReasoning.kt` | ⏸ |
| 7 Knowledge-Gap Detection | Partial | `agent/AmarAgentCore.kt; agent/AmarSourceVerifier.kt` | ⏸ |
| 8 Uncertainty Awareness | Partial | `agent/AmarReasoning.kt; agent/AmarAgentCore.kt` | ⏸ |
| 9 Evidence-Need Awareness | Partial | `agent/AmarAgentPlanner.kt; Stage2 hardening` | ⏸ |
| 10 Structural Analysis | Partial | `agent/AmarAgentPlanner.kt; agent/AmarAgentOrchestrator.kt` | ⏸ |
| 11 Pattern Analysis | Partial | `agent/AmarReasoning.kt; evidence utilities` | ⏸ |
| 12 Causal Candidate Analysis | New | `cognitive/new/Capability12CausalCandidateAnalysis.kt` | ⏸ |
| 13 Consequence Analysis | New | `cognitive/new/Capability13ConsequenceAnalysis.kt` | ⏸ |
| 14 Information Sufficiency Analysis | Partial | `agent/AmarSourceVerifier.kt; agent/AmarAgentCore.kt` | ⏸ |
| 15 Hypothesis & Counter-Hypothesis Reasoning | Partial | `agent/AmarReasoning.kt; agent/AmarAgentCritic.kt` | ⏸ |
| 16 Hypothesis Falsification | New | `cognitive/new/Capability16HypothesisFalsification.kt` | ⏸ |
| 17 Counterfactual Reasoning | New | `cognitive/new/Capability17CounterfactualReasoning.kt` | ⏸ |
| 18 Assumption Detection | Partial | `agent/AmarAgentCritic.kt; agent/AmarReasoning.kt` | ⏸ |
| 19 Proof-Bound Cognition | Partial | `agent/AmarReasoning.kt; Stage2 hardening` | ⏸ |
| 20 Contradiction Detection & Handling | Unproven | `agent/AmarReasoning.kt; verification` | ⏸ |
| 21 Reasoning Chain Validation | Partial | `agent/AmarAgentCritic.kt; agent/AmarReasoning.kt` | ⏸ |
| 22 Cross-Reference Resolution | New | `cognitive/new/Capability22CrossReferenceResolution.kt` | ⏸ |
| 23 Market Context Understanding | Partial | `MarketAnalyzer.kt; AmarStageTwoEngine.kt` | ⏸ |
| 24 Risk Language Detection | Partial | `AmarIntentUnderstanding.kt; AmarRiskEngine.kt` | ⏸ |
| 25 Strategy Reference Resolution | Partial | `AmarAgentPlanner.kt` | ⏸ |
| 26 Position State Awareness | Partial | `trading/MT5 context components` | ⏸ |
| 27 Source Credibility Assessment | Unproven | `AmarSourceVerifier.kt; source quality` | ⏸ |
| 28 Input Bias Detection | New | `cognitive/new/Capability28InputBiasDetection.kt` | ⏸ |
| 29 User-State Modeling | New | `cognitive/new/Capability29UserStateModeling.kt` | ⏸ |
| 30 Emotional Signal Interpretation | New | `cognitive/new/Capability30EmotionalSignalInterpretation.kt` | ⏸ |
| 31 Tone Shift Detection | New | `cognitive/new/Capability31ToneShiftDetection.kt` | ⏸ |
| 32 Emoji & Symbol Context Interpretation | New | `cognitive/new/Capability32EmojiSymbolContextInterpretation.kt` | ⏸ |
| 33 Implicit Meaning Detection | Partial | `AmarIntentUnderstanding.kt; AmarReasoning.kt` | ⏸ |
| 34 Hint Interpretation | New | `cognitive/new/Capability34HintInterpretation.kt` | ⏸ |
| 35 Silence Interpretation | New | `cognitive/new/Capability35SilenceInterpretation.kt` | ⏸ |
| 36 Attention & Relevance Awareness | Partial | `AmarReasoning.kt; retrieval relevance` | ⏸ |
| 37 Relationship Awareness | New | `cognitive/new/Capability37RelationshipAwareness.kt` | ⏸ |
| 38 Continuity Awareness | Partial | `AmarAgentSession.kt; AmarAgentOrchestrator.kt` | ⏸ |
| 39 Cognitive State Persistence | Partial | `AmarAgentSession.kt; memory components` | ⏸ |
| 40 Cognitive Position Revision | Partial | `session/reasoning state` | ⏸ |
| 41 Theory-of-Mind Modeling | New | `cognitive/new/Capability41TheoryOfMindModeling.kt` | ⏸ |
| 42 Communication-Style Awareness | New | `cognitive/new/Capability42CommunicationStyleAwareness.kt` | ⏸ |
| 43 User-Context Modeling | Partial | `AmarAgentContext; AmarAgentSession.kt` | ⏸ |
| 44 Empathy Modeling | New | `cognitive/new/Capability44EmpathyModeling.kt` | ⏸ |
| 45 Multi-Perspective Analysis | Partial | `AmarStageTwoEngine.kt` | ⏸ |
| 46 Knowledge Synthesis | Partial | `AmarKnowledgeEngine.kt; AmarReasoning.kt` | ⏸ |
| 47 Mental Model Construction | New | `cognitive/new/Capability47MentalModelConstruction.kt` | ⏸ |
| 48 Sarcasm Detection | New | `cognitive/new/Capability48SarcasmDetection.kt` | ⏸ |
| 49 Irony Detection | New | `cognitive/new/Capability49IronyDetection.kt` | ⏸ |
| 50 Humor Recognition | New | `cognitive/new/Capability50HumorRecognition.kt` | ⏸ |
| 51 Cultural Context Interpretation | New | `cognitive/new/Capability51CulturalContextInterpretation.kt` | ⏸ |
| 52 Multi-Language Switching | New | `cognitive/new/Capability52MultiLanguageSwitching.kt` | ⏸ |
| 53 Code-Switching Detection | New | `cognitive/new/Capability53CodeSwitchingDetection.kt` | ⏸ |
| 54 Pause Detection | New | `cognitive/new/Capability54PauseDetection.kt` | ⏸ |
| 55 Hesitation Detection | New | `cognitive/new/Capability55HesitationDetection.kt` | ⏸ |
| 56 Confirmation Signal Detection | Partial | `orchestration/session components` | ⏸ |
| 57 Correction Detection | Partial | `session/state update components` | ⏸ |
| 58 Meta-Communication Understanding | New | `cognitive/new/Capability58MetaCommunicationUnderstanding.kt` | ⏸ |
| 59 Recursive Deep Thinking | Partial | `AmarAgentCritic.kt; AmarReasoning.kt` | ⏸ |
| 60 Second-Order Thinking | Partial | `reasoning/consequence basis` | ⏸ |
| 61 Internal Critique | Extend | `AmarAgentCritic.kt` | ⏸ |
| 62 Blind-Spot Detection | New | `cognitive/new/Capability62BlindSpotDetection.kt` | ⏸ |
| 63 Cognitive Bias Guarding | Partial | `AmarAgentCritic.kt; reasoning` | ⏸ |
| 64 Scenario Branching & Mental Simulation | Partial | `Stage2 planning/simulation boundaries` | ⏸ |
| 65 Failure & Recovery Simulation | Partial | `recovery components` | ⏸ |
| 66 Forecasting & Prediction Awareness | New | `cognitive/new/Capability66ForecastingPredictionAwareness.kt` | ⏸ |
| 67 Meta-Cognition | Partial | `AmarAgentCritic.kt` | ⏸ |
| 68 Awareness Quality & Depth Assessment | New | `cognitive/new/Capability68AwarenessQualityDepthAssessment.kt` | ⏸ |
| 69 Ignorance & Knowledge-Boundary Awareness | Partial | `AmarAgentCore.kt; AmarReasoning.kt` | ⏸ |
| 70 Cognitive Provenance & Audit Continuity | Partial | `AmarAgentSession.kt; provenance components` | ⏸ |
| 71 Concept Mapping | New | `cognitive/new/Capability71ConceptMapping.kt` | ⏸ |
| 72 Contradiction Synthesis | Partial | `AmarReasoning.kt; contradiction handling` | ⏸ |

## Rules

- ⏸ = implementation not started.
- Partial and Extend remain in existing files; no duplicate stubs.
- New receives only the empty placeholders listed above.
- Capabilities 20 and 27 remain Unproven pending independent source-level Gap Audit.
- This registry does not authorize implementation or alter Stage 2 authority boundaries.
