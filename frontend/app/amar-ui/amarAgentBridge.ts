export type AmarAgentStatus =
  | 'ready'
  | 'thinking'
  | 'searching'
  | 'analyzing'
  | 'learning'
  | 'complete'
  | 'error'
  | 'uncertain'
  | 'blocked';

export type AmarSource = {
  id: string;
  title: string;
  url?: string;
  status?: 'verified' | 'checking' | 'conflict' | 'failed';
  confidence?: number;
  excerpt?: string;
};

export type AmarVerificationState = 'confirmed' | 'uncertain' | 'blocked';

export type AmarProgressStage =
  | 'understanding'
  | 'planning'
  | 'searching'
  | 'source_analysis'
  | 'evidence_comparison'
  | 'conflict_check'
  | 'verification'
  | 'preparing_result';

export type AmarAgentResponse = {
  id: string;
  text: string;
  status?: AmarAgentStatus;
  verification?: AmarVerificationState;
  sources?: AmarSource[];
  confidence?: number;
  durationMs?: number;
  uncertaintyReason?: string;
  progressStage?: AmarProgressStage;
};

export type AmarAgentRequest = {
  id: string;
  prompt: string;
  attachments?: string[];
  mode?: string;
  searchMode?: 'restricted' | 'open';
};

export type AmarAgentCapabilities = {
  text?: boolean;
  image?: boolean;
  video?: boolean;
  screenShare?: boolean;
  searchRestricted?: boolean;
  searchOpen?: boolean;
  evidence?: boolean;
  progress?: boolean;
};

/**
 * UI-only adapter contract.
 * Item 10 owns authorization, execution, verification, evidence and fail-closed decisions.
 * The UI must never infer success when a capability or verified response is absent.
 */
export type AmarAgentBridge = {
  capabilities?: AmarAgentCapabilities;
  send?: (request: AmarAgentRequest) => Promise<AmarAgentResponse>;
  stop?: () => void;
  search?: (query: string, searchMode: 'restricted' | 'open') => Promise<AmarSource[]>;
  analyzeFile?: (file: File) => Promise<AmarAgentResponse>;
  startScreenShare?: () => Promise<void>;
  stopScreenShare?: () => void;
  onStatus?: (listener: (status: AmarAgentStatus) => void) => () => void;
  onProgress?: (listener: (stage: AmarProgressStage) => void) => () => void;
};
