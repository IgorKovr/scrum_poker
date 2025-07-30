# Scrum Poker Application

A real-time collaborative estimation tool for distributed software teams using the Planning Poker technique.

## 🚀 Features

- Real-time WebSocket communication
- Multiple concurrent rooms
- Fibonacci sequence cards (0.5, 1, 2, 3, 5, 8)
- Show/Hide estimates functionality
- Clean, modern UI with Tailwind CSS

## 🛠️ Tech Stack

- **Backend**: Kotlin, Spring Boot, WebSockets
- **Frontend**: React, TypeScript, Vite, Tailwind CSS
- **Deployment**: Railway

## 📋 Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- npm or yarn

## 🏃‍♂️ Local Development

### Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/IgorKovr/scrum_poker.git
   cd scrum_poker
   ```

2. Run the setup script:
   ```bash
   ./setup-local.sh
   ```

3. Start both services:
   ```bash
   ./run-local.sh
   ```

4. Open your browser:
   - Frontend: http://localhost:3000
   - Backend Health: http://localhost:8080/health

### Manual Setup

#### Backend
```bash
cd backend
./gradlew bootRun
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## 📝 How to Use

1. Enter your name on the welcome screen
2. Enter a room name (e.g., "sprint-planning")
3. Select your estimate card
4. Wait for all team members to vote
5. Click "Show" to reveal all estimates
6. Click "Delete Estimates" to start a new round

## 🔍 Troubleshooting

### Backend Issues
- Check Java version: `java -version`
- Check logs: Look for "🚀 SCRUM POKER BACKEND IS READY!"
- Verify port 8080 is free: `lsof -i :8080`

### Frontend Issues
- Check Node version: `node --version`
- Clear node_modules: `rm -rf node_modules && npm install`
- Check browser console for WebSocket errors

### WebSocket Connection Issues
- Ensure backend is running before frontend
- Check browser console for connection errors
- Verify WebSocket URL in browser logs

## 🚀 Deployment

The application is configured for Railway deployment:

- Backend: Runs as a Spring Boot JAR
- Frontend: Served via Express.js with WebSocket proxy

## 📊 Monitoring

### Health Endpoints
- Frontend: `/health`
- Backend: `/health` and `/heartbeat`

### Logs
- Backend logs show WebSocket connections and heartbeat info
- Frontend logs show proxy activity
- Browser console shows detailed connection logs

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request 