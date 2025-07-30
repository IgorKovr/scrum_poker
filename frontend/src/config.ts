// WebSocket configuration
export const getWebSocketUrl = () => {
  const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  
  if (isLocal) {
    // For local development, Vite proxy handles the WebSocket connection
    return `${protocol}//localhost:3000/ws`;
  } else {
    // For production, use the frontend server's proxy
    return `${protocol}//${window.location.host}/ws`;
  }
}; 