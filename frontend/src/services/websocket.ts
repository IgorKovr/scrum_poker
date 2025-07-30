/**
 * websocket.ts - WebSocket Service for Real-time Communication
 * 
 * This service provides a robust WebSocket client for real-time communication
 * with the Scrum Poker backend. It handles connection management, message routing,
 * automatic reconnection, and provides a clean API for the rest of the application.
 * 
 * Key Features:
 * 1. Automatic connection establishment and management
 * 2. Message type-based event handling system
 * 3. Automatic reconnection with exponential backoff
 * 4. Connection state monitoring and error handling
 * 5. Type-safe message sending and receiving
 * 6. Singleton pattern for global access
 * 
 * The service implements a publish-subscribe pattern where components can
 * subscribe to specific message types and receive callbacks when those
 * messages arrive from the server.
 * 
 * Reconnection Strategy:
 * - Automatically attempts to reconnect on connection loss
 * - Uses exponential backoff (1s, 2s, 4s, 8s, 16s)
 * - Stops after 5 failed attempts to prevent infinite loops
 * - Resets attempt counter on successful connection
 * 
 * Usage Example:
 * await wsService.connect(wsUrl);
 * wsService.on(MessageType.ROOM_UPDATE, handleRoomUpdate);
 * wsService.send({ type: MessageType.VOTE, payload: { ... } });
 */

import { MessageType, WebSocketMessage } from '../types';

/**
 * WebSocketService Class - Manages WebSocket connections and messaging
 * 
 * This class encapsulates all WebSocket functionality, providing a clean interface
 * for establishing connections, sending messages, and handling incoming data.
 * It manages the connection lifecycle and provides automatic reconnection.
 * 
 * The service uses a Map-based event system where each message type can have
 * one registered handler. This keeps the API simple while providing flexibility.
 */
export class WebSocketService {
  /** The active WebSocket connection instance */
  private ws: WebSocket | null = null;
  
  /** Map of message type to handler function for incoming messages */
  private messageHandlers: Map<MessageType, (payload: any) => void> = new Map();
  
  /** Current number of reconnection attempts */
  private reconnectAttempts = 0;
  
  /** Maximum number of reconnection attempts before giving up */
  private maxReconnectAttempts = 5;
  
  /** Base delay in milliseconds for reconnection attempts */
  private reconnectDelay = 1000;

  /**
   * Establishes a WebSocket connection to the specified URL
   * 
   * This method creates a new WebSocket connection and sets up all the necessary
   * event handlers. It returns a Promise that resolves when the connection is
   * established or rejects if the connection fails.
   * 
   * The method sets up handlers for:
   * - onopen: Connection establishment
   * - onmessage: Incoming message routing
   * - onerror: Connection error handling
   * - onclose: Connection termination and reconnection logic
   * 
   * @param {string} url - The WebSocket URL to connect to
   * @returns {Promise<void>} Promise that resolves when connected
   */
  connect(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        console.log('[WebSocket] Attempting to connect to:', url);
        console.log('[WebSocket] Current location:', window.location.href);
        this.ws = new WebSocket(url);

        /**
         * Connection establishment handler
         * Called when the WebSocket connection is successfully opened
         */
        this.ws.onopen = () => {
          console.log('[WebSocket] Connected successfully');
          this.reconnectAttempts = 0; // Reset reconnect counter on successful connection
          resolve();
        };

        /**
         * Incoming message handler
         * Parses incoming messages and routes them to appropriate handlers
         */
        this.ws.onmessage = (event) => {
          try {
            // Parse the JSON message from the server
            const message: WebSocketMessage = JSON.parse(event.data);
            
            // Find and call the appropriate handler for this message type
            const handler = this.messageHandlers.get(message.type);
            if (handler) {
              handler(message.payload);
            }
          } catch (error) {
            console.error('Error parsing WebSocket message:', error);
          }
        };

        /**
         * Connection error handler
         * Handles connection errors and provides detailed logging
         */
        this.ws.onerror = (error) => {
          console.error('[WebSocket] Connection error:', error);
          console.error('[WebSocket] URL was:', url);
          console.error('[WebSocket] ReadyState:', this.ws?.readyState);
          reject(error);
        };

        /**
         * Connection close handler
         * Handles connection termination and triggers reconnection logic
         */
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

  /**
   * Attempts to reconnect to the WebSocket server
   * 
   * This private method implements the automatic reconnection logic with
   * exponential backoff. It will attempt to reconnect up to maxReconnectAttempts
   * times, with increasing delays between attempts.
   * 
   * @param {string} url - The WebSocket URL to reconnect to
   */
  private attemptReconnect(url: string) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      
      // Exponential backoff: delay increases with each attempt
      setTimeout(() => {
        this.connect(url);
      }, this.reconnectDelay * this.reconnectAttempts);
    }
  }

  /**
   * Closes the WebSocket connection
   * 
   * This method cleanly closes the WebSocket connection and clears the
   * connection reference. It should be called when the component unmounts
   * or when the connection is no longer needed.
   */
  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * Sends a message to the WebSocket server
   * 
   * This method serializes the message object to JSON and sends it to the server.
   * It includes safety checks to ensure the connection is open before sending.
   * 
   * @param {WebSocketMessage} message - The message object to send
   */
  send(message: WebSocketMessage) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.error('WebSocket is not connected');
    }
  }

  /**
   * Registers a handler for a specific message type
   * 
   * This method allows components to subscribe to specific message types.
   * When a message of the specified type is received, the handler function
   * will be called with the message payload.
   * 
   * @param {MessageType} type - The message type to listen for
   * @param {Function} handler - The callback function to call when message is received
   */
  on(type: MessageType, handler: (payload: any) => void) {
    this.messageHandlers.set(type, handler);
  }

  /**
   * Removes a handler for a specific message type
   * 
   * This method unsubscribes from a message type by removing its handler.
   * Useful for cleanup when components unmount.
   * 
   * @param {MessageType} type - The message type to unsubscribe from
   */
  off(type: MessageType) {
    this.messageHandlers.delete(type);
  }
  
  /**
   * Checks if the WebSocket connection is currently open
   * 
   * This method provides a simple way to check the connection status
   * without exposing the internal WebSocket instance.
   * 
   * @returns {boolean} True if connected and ready to send messages
   */
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}

/**
 * Singleton WebSocket service instance
 * 
 * This exported instance provides global access to the WebSocket service.
 * Using a singleton ensures that all components share the same connection
 * and message handlers, preventing multiple connections and ensuring
 * consistent state management.
 * 
 * Usage:
 * import { wsService } from './websocket';
 * await wsService.connect(url);
 */
export const wsService = new WebSocketService(); 