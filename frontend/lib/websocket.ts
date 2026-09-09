import type { AmarEvent } from './events';

type Listener = (event: AmarEvent) => void;

export class AmarWebSocketTransport {
  private socket?: WebSocket;
  private listeners = new Set<Listener>();
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private closed = false;

  constructor(private readonly url: string) {}

  connect(listener: Listener): void {
    this.listeners.add(listener);
    this.closed = false;
    if (typeof window === 'undefined' || this.socket) return;
    this.open();
  }

  disconnect(listener?: Listener): void {
    if (listener) this.listeners.delete(listener);
    if (!listener || this.listeners.size === 0) {
      this.closed = true;
      if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
      this.socket?.close();
      this.socket = undefined;
    }
  }

  private open(): void {
    if (this.closed || typeof window === 'undefined') return;
    try {
      const socket = new WebSocket(this.url);
      this.socket = socket;
      socket.onmessage = message => {
        try {
          const event = JSON.parse(message.data) as AmarEvent;
          this.listeners.forEach(listener => listener(event));
        } catch { /* invalid transport payload is ignored */ }
      };
      socket.onclose = () => {
        this.socket = undefined;
        if (!this.closed && this.listeners.size) this.reconnectTimer = setTimeout(() => this.open(), 1500);
      };
      socket.onerror = () => socket.close();
    } catch {
      this.reconnectTimer = setTimeout(() => this.open(), 1500);
    }
  }
}

export const amarLiveTransport = new AmarWebSocketTransport(process.env.NEXT_PUBLIC_AMAR_WS_URL ?? 'ws://localhost:8000/ws');
