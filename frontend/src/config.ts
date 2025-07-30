// WebSocket configuration
export const getWebSocketUrl = () => {
  // In production, use Railway's internal backend URL
  if (window.location.hostname !== 'localhost') {
    // Using Railway's internal networking (backend service can be accessed internally)
    return 'ws://scrum-poker.railway.internal:8080/ws';
  }
  // In development, use local backend
  return `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws`;
}; 