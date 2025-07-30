/**
 * config.ts - Application Configuration
 * 
 * This file contains configuration functions for the Scrum Poker application.
 * Currently focuses on WebSocket URL configuration for different environments.
 * 
 * The main challenge this solves is determining the correct WebSocket endpoint
 * based on whether the app is running locally or in production deployment.
 * 
 * Environment Detection:
 * - Local Development: Uses Vite dev server proxy to backend on localhost:8080
 * - Production: Uses the same domain as the frontend with appropriate protocol
 * 
 * Protocol Selection:
 * - HTTP sites use 'ws://' WebSocket protocol
 * - HTTPS sites use 'wss://' secure WebSocket protocol
 */

/**
 * Determines the appropriate WebSocket URL based on the current environment
 * 
 * This function automatically detects whether the application is running locally
 * or in production and returns the appropriate WebSocket endpoint URL.
 * 
 * Local Development Flow:
 * - Frontend runs on localhost:3000 (Vite dev server)
 * - Backend runs on localhost:8080 (Spring Boot)
 * - Vite proxy configuration handles WebSocket forwarding
 * 
 * Production Flow:
 * - Frontend and backend are served from the same domain
 * - WebSocket connections use the same host as the web page
 * - Protocol matches the page protocol (ws/wss)
 * 
 * @returns {string} The complete WebSocket URL including protocol and path
 * 
 * @example
 * // Local development
 * getWebSocketUrl() // Returns: "ws://localhost:3000/ws"
 * 
 * @example  
 * // Production HTTPS site
 * getWebSocketUrl() // Returns: "wss://myapp.railway.app/ws"
 */
export const getWebSocketUrl = () => {
  // Detect if running on local development environment
  const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  
  // Select WebSocket protocol based on page protocol
  // HTTPS pages must use WSS (secure WebSocket), HTTP pages use WS
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  
  if (isLocal) {
    // For local development, Vite proxy handles the WebSocket connection
    // The frontend dev server (port 3000) proxies WebSocket requests
    // to the backend server (port 8080) automatically
    return `${protocol}//localhost:3000/ws`;
  } else {
    // For production, use the frontend server's proxy
    // Both frontend and backend are served from the same domain
    // so we use the current host with the WebSocket path
    return `${protocol}//${window.location.host}/ws`;
  }
}; 