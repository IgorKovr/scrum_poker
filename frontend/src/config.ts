// WebSocket configuration
export const getWebSocketUrl = () => {
  // Always use relative path - the frontend server will proxy to backend
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/ws`;
}; 