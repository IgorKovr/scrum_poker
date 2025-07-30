// WebSocket configuration
export const getWebSocketUrl = () => {
  // In production, use the Railway backend URL
  if (window.location.hostname !== 'localhost') {
    return 'wss://scrum-poker-production.up.railway.app/ws';
  }
  // In development, use local backend
  return `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws`;
}; 