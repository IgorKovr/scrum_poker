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

// Health check endpoint
app.get('/health', async (req, res) => {
  let backendStatus = 'unknown';
  let backendError = null;
  
  // Try to check backend health
  try {
    const http = require('http');
    await new Promise((resolve, reject) => {
      const req = http.get('http://scrum-poker.railway.internal:8080/health', (response) => {
        if (response.statusCode === 200) {
          backendStatus = 'healthy';
          resolve();
        } else {
          backendStatus = 'unhealthy';
          backendError = `Status code: ${response.statusCode}`;
          resolve();
        }
      });
      req.on('error', (err) => {
        backendStatus = 'unreachable';
        backendError = err.message;
        resolve();
      });
      req.setTimeout(5000, () => {
        backendStatus = 'timeout';
        backendError = 'Backend health check timed out';
        req.destroy();
        resolve();
      });
    });
  } catch (err) {
    backendStatus = 'error';
    backendError = err.message;
  }
  
  res.json({
    status: 'healthy',
    service: 'scrum-poker-frontend',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    memory: {
      used: Math.round(process.memoryUsage().heapUsed / 1024 / 1024),
      total: Math.round(process.memoryUsage().heapTotal / 1024 / 1024)
    },
    backend: {
      status: backendStatus,
      error: backendError,
      url: 'http://scrum-poker.railway.internal:8080'
    }
  });
});

// Serve static files from the dist directory
app.use(express.static(path.join(__dirname, 'dist')));

// Handle all routes by serving index.html
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log('='.repeat(60));
  console.log('🚀 SCRUM POKER FRONTEND STARTED');
  console.log('='.repeat(60));
  console.log(`✅ Server is running on port: ${PORT}`);
  console.log(`✅ Environment: ${process.env.NODE_ENV || 'production'}`);
  console.log(`✅ Backend URL: http://scrum-poker.railway.internal:8080`);
  console.log(`✅ Process ID: ${process.pid}`);
  console.log(`✅ Node Version: ${process.version}`);
  console.log(`✅ Memory Usage: ${Math.round(process.memoryUsage().heapUsed / 1024 / 1024)} MB`);
  console.log('='.repeat(60));
  console.log('📝 WebSocket proxy is active at /ws');
  console.log('='.repeat(60));
}); 