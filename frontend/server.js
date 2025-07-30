const express = require('express');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Add proxy middleware for WebSocket
const { createProxyMiddleware } = require('http-proxy-middleware');

// Proxy WebSocket connections to backend
app.use('/ws', createProxyMiddleware({
  target: 'http://scrum-poker.railway.internal:8080',
  ws: true,
  changeOrigin: true,
  logLevel: 'debug',
  onError: (err, req, res) => {
    console.error('Proxy error:', err);
  },
  onProxyReqWs: (proxyReq, req, socket, options, head) => {
    console.log('WebSocket proxy request to backend');
  }
}));

// Serve static files from the dist directory
app.use(express.static(path.join(__dirname, 'dist')));

// Handle all routes by serving index.html
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Frontend server is running on port ${PORT}`);
}); 