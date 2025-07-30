import { MessageType, WebSocketMessage } from '../types';

export class WebSocketService {
  private ws: WebSocket | null = null;
  private messageHandlers: Map<MessageType, (payload: any) => void> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;

  connect(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        console.log('[WebSocket] Attempting to connect to:', url);
        console.log('[WebSocket] Current location:', window.location.href);
        this.ws = new WebSocket(url);

        this.ws.onopen = () => {
          console.log('[WebSocket] Connected successfully');
          this.reconnectAttempts = 0;
          resolve();
        };

        this.ws.onmessage = (event) => {
          try {
            const message: WebSocketMessage = JSON.parse(event.data);
            const handler = this.messageHandlers.get(message.type);
            if (handler) {
              handler(message.payload);
            }
          } catch (error) {
            console.error('Error parsing WebSocket message:', error);
          }
        };

        this.ws.onerror = (error) => {
          console.error('[WebSocket] Connection error:', error);
          console.error('[WebSocket] URL was:', url);
          console.error('[WebSocket] ReadyState:', this.ws?.readyState);
          reject(error);
        };

        this.ws.onclose = (event) => {
          console.log('[WebSocket] Disconnected - Code:', event.code, 'Reason:', event.reason);
          console.log('[WebSocket] Clean:', event.wasClean);
          this.attemptReconnect(url);
        };
      } catch (error) {
        reject(error);
      }
    });
  }

  private attemptReconnect(url: string) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      setTimeout(() => {
        this.connect(url);
      }, this.reconnectDelay * this.reconnectAttempts);
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  send(message: WebSocketMessage) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.error('WebSocket is not connected');
    }
  }

  on(type: MessageType, handler: (payload: any) => void) {
    this.messageHandlers.set(type, handler);
  }

  off(type: MessageType) {
    this.messageHandlers.delete(type);
  }
  
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}

export const wsService = new WebSocketService(); 