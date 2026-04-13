import { parseDmRealtimeEvent } from '../services/api';
import type { DmRealtimeEvent } from '../types';

interface MessageRealtimeClientOptions {
  token: string;
  onEvent: (event: DmRealtimeEvent) => void;
  onStateChange?: (state: 'connecting' | 'open' | 'closed') => void;
  WebSocketImpl?: typeof WebSocket;
}

function resolveRealtimeUrl(token: string) {
  const configuredBase = import.meta.env.VITE_WS_BASE_URL?.trim();
  const base = configuredBase
    ? configuredBase.replace(/\/$/, '')
    : `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}`;
  return `${base}/ws/messages?token=${encodeURIComponent(token)}`;
}

export function createMessageRealtimeClient({
  token,
  onEvent,
  onStateChange,
  WebSocketImpl = WebSocket,
}: MessageRealtimeClientOptions) {
  let socket: WebSocket | null = null;
  let reconnectTimer: number | null = null;
  let manuallyClosed = false;

  function clearReconnectTimer() {
    if (reconnectTimer !== null) {
      window.clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
  }

  function scheduleReconnect() {
    clearReconnectTimer();
    reconnectTimer = window.setTimeout(() => {
      connect();
    }, 1000);
  }

  function connect() {
    if (!token) {
      return;
    }
    clearReconnectTimer();
    onStateChange?.('connecting');
    socket = new WebSocketImpl(resolveRealtimeUrl(token));
    socket.onopen = () => {
      onStateChange?.('open');
    };
    socket.onmessage = (event) => {
      if (typeof event.data !== 'string') {
        return;
      }
      onEvent(parseDmRealtimeEvent(event.data));
    };
    socket.onclose = () => {
      onStateChange?.('closed');
      socket = null;
      if (!manuallyClosed) {
        scheduleReconnect();
      }
    };
  }

  function disconnect() {
    manuallyClosed = true;
    clearReconnectTimer();
    socket?.close();
    socket = null;
  }

  return {
    connect,
    disconnect,
  };
}
