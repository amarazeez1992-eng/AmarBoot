import type { AmarAgentBridge, AmarAgentRequest, AmarAgentResponse, AmarSource, AmarAgentCapabilities } from './amarAgentBridge';

type AmarNativeReply = {
  requestId: string;
  ok: boolean;
  response?: AmarAgentResponse;
  sources?: AmarSource[];
  error?: string;
};

type AmarNativeHost = {
  request: (payload: string) => void;
};

declare global {
  interface Window {
    AmarAI?: { engine?: AmarNativeHost };
  }
}

const ENGINE_IDS = [
  'reasoning','market','research','knowledge','source_mesh','backtest','validation',
  'precision','uncertainty','evolution','champion_challenger','counterfactual',
  'self_audit','camera_multimodal','screen_multimodal','conversation_memory',
  'provider_gateway','update_engine',
] as const;

const capabilities: AmarAgentCapabilities = {
  text: true,
  image: true,
  video: true,
  screenShare: false,
  searchRestricted: true,
  searchOpen: true,
  evidence: true,
  progress: true,
};

function requestNative<T>(payload: Record<string, unknown>): Promise<T> {
  if (typeof window === 'undefined' || !window.AmarAI?.engine?.request) {
    return Promise.reject(new Error('AMAR engine host is not connected'));
  }
  const requestId = crypto.randomUUID();
  return new Promise<T>((resolve, reject) => {
    const timeout = window.setTimeout(() => {
      window.removeEventListener('message', onMessage);
      reject(new Error('AMAR engine response timeout'));
    }, 15000);
    function onMessage(event: MessageEvent<AmarNativeReply>) {
      const data = event.data;
      if (!data || data.requestId !== requestId) return;
      window.clearTimeout(timeout);
      window.removeEventListener('message', onMessage);
      if (!data.ok) reject(new Error(data.error || 'AMAR engine request failed'));
      else resolve((data.response ?? data.sources) as T);
    }
    window.addEventListener('message', onMessage);
    window.AmarAI!.engine!.request(JSON.stringify({ version: '1', requestId, ...payload }));
  });
}

export function createAmarEngineBridge(): AmarAgentBridge {
  return {
    capabilities,
    async send(request: AmarAgentRequest) {
      const response = await requestNative<AmarAgentResponse>({ action: 'agent.request', request });
      if (!response || !response.id || !response.text) throw new Error('Invalid AMAR engine response');
      return response;
    },
    async search(query, searchMode) {
      const sources = await requestNative<AmarSource[]>({ action: 'research.search', query, searchMode, engines: ['research','source_mesh','knowledge'] });
      if (!Array.isArray(sources)) throw new Error('Invalid AMAR source response');
      return sources;
    },
    async analyzeFile(file) {
      const buffer = await file.arrayBuffer();
      const bytes = Array.from(new Uint8Array(buffer));
      return requestNative<AmarAgentResponse>({
        action: 'multimodal.analyze',
        media: { name: file.name, type: file.type, bytes },
        engines: ['camera_multimodal','screen_multimodal','reasoning','source_mesh'],
      });
    },
    startScreenShare: async () => {
      await requestNative({ action: 'screen.share.start', engines: ['screen_multimodal'] });
    },
    stopScreenShare: () => {
      if (typeof window !== 'undefined' && window.AmarAI?.engine?.request) {
        window.AmarAI.engine.request(JSON.stringify({ version: '1', requestId: crypto.randomUUID(), action: 'screen.share.stop' }));
      }
    },
  };
}

export { ENGINE_IDS };
