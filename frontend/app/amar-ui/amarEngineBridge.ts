import type { AmarAgentBridge, AmarAgentRequest, AmarAgentResponse, AmarSource, AmarAgentCapabilities } from './amarAgentBridge';

type AmarNativeReply = { requestId: string; ok: boolean; response?: AmarAgentResponse; sources?: AmarSource[]; error?: string };
type AmarNativeHost = { request: (payload: string) => void };
declare global { interface Window { AmarEngine?: AmarNativeHost } }

export const AMAR_ENGINE_IDS = ['reasoning','market','research','knowledge','source_mesh','backtest','validation','precision','uncertainty','evolution','champion_challenger','counterfactual','self_audit','camera_multimodal','screen_multimodal','conversation_memory','provider_gateway','update_engine'] as const;
const capabilities: AmarAgentCapabilities = { text:true,image:false,video:false,screenShare:false,searchRestricted:true,searchOpen:true,evidence:true,progress:true };

function nativeHost(): AmarNativeHost | null {
  if (typeof window === 'undefined') return null;
  return window.AmarEngine?.request ? window.AmarEngine : null;
}

function requestNative<T>(payload: Record<string, unknown>): Promise<T> {
  const host = nativeHost();
  if (!host) return Promise.reject(new Error('AMAR engine host is not connected'));
  const requestId = crypto.randomUUID();
  return new Promise<T>((resolve,reject) => {
    const onMessage = (event: MessageEvent<AmarNativeReply>) => {
      const data = event.data;
      if (!data || data.requestId !== requestId) return;
      window.clearTimeout(timeout); window.removeEventListener('message', onMessage);
      if (!data.ok) reject(new Error(data.error || 'AMAR engine request failed')); else resolve((data.response ?? data.sources) as T);
    };
    const timeout = window.setTimeout(() => { window.removeEventListener('message', onMessage); reject(new Error('AMAR engine response timeout')); }, 15000);
    window.addEventListener('message', onMessage);
    host.request(JSON.stringify({ version:'1', requestId, ...payload }));
  });
}

export function createAmarEngineBridge(): AmarAgentBridge {
  return {
    capabilities,
    send: async (request: AmarAgentRequest) => {
      const response = await requestNative<AmarAgentResponse>({ action:'agent.request', request, engines:AMAR_ENGINE_IDS });
      if (!response?.id || !response.text) throw new Error('Invalid AMAR engine response');
      return response;
    },
    search: async (query, searchMode) => {
      const sources = await requestNative<AmarSource[]>({ action:'research.search', query, searchMode, engines:['research','source_mesh','knowledge'] });
      if (!Array.isArray(sources)) throw new Error('Invalid AMAR source response');
      return sources;
    },
    analyzeFile: async () => { throw new Error('MULTIMODAL_NATIVE_TRANSFER_REQUIRES_SECURE_MEDIA_CHANNEL'); },
    startScreenShare: async () => { throw new Error('SCREEN_SHARE_REQUIRES_ANDROID_MEDIA_PROJECTION'); },
    stopScreenShare: () => undefined,
  };
}
