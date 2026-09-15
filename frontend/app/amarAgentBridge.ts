export type AmarAgentStatus = 'ready' | 'thinking' | 'searching' | 'analyzing' | 'learning' | 'complete' | 'error';

export type AmarSource = {
  id: string;
  title: string;
  url?: string;
  status?: 'verified' | 'checking' | 'conflict' | 'failed';
  confidence?: number;
  excerpt?: string;
};

export type AmarAgentResponse = {
  id: string;
  text: string;
  status?: AmarAgentStatus;
  sources?: AmarSource[];
  confidence?: number;
  durationMs?: number;
};

export type AmarAgentRequest = {
  id: string;
  prompt: string;
  attachments?: string[];
  mode?: string;
};

/** UI-only contract. Any future agent can implement this without changing the workspace UI. */
export type AmarAgentBridge = {
  send?: (request: AmarAgentRequest) => Promise<AmarAgentResponse>;
  stop?: () => void;
  search?: (query: string) => Promise<AmarSource[]>;
  analyzeFile?: (file: File) => Promise<AmarAgentResponse>;
  onStatus?: (listener: (status: AmarAgentStatus) => void) => () => void;
};
