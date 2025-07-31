# Scrum Poker Application

[![🧪 Test Suite](https://github.com/IgorKovr/scrum_poker/actions/workflows/test.yml/badge.svg)](https://github.com/IgorKovr/scrum_poker/actions/workflows/test.yml)

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
- **Testing**: JUnit 5, Mockito, Vitest, React Testing Library
- **CI/CD**: GitHub Actions
- **Deployment**: Railway

## 🧪 Testing

Comprehensive test suite with ~100 tests covering all business logic:

### Backend Tests (Kotlin/Spring Boot)
```bash
cd backend
./gradlew test
```

- **RoomServiceTest**: 26 tests covering room management, voting, user lifecycle
- **HealthControllerTest**: 13 tests for REST API endpoints
- **WebSocketHandlerIntegrationTest**: 3 integration tests for real-time communication

### Frontend Tests (TypeScript/React)
```bash
cd frontend
npm test
```

- **Component Tests**: 24 tests for React components (rendering, interactions, accessibility)
- **Service Tests**: 11 tests for WebSocket service and API contracts  
- **Utility Tests**: 23 tests for configuration and environment detection

### Continuous Integration

GitHub Actions automatically runs all tests on:
- Push to main branch
- Pull requests
- Manual workflow dispatch

## 💻 VS Code (Cursor) Setup

This project includes comprehensive VS Code/Cursor configuration for optimal development experience.

### 🛠️ Quick Setup

1. **Open the project in VS Code/Cursor**
2. **Install recommended extensions** (VS Code will prompt you automatically)
3. **Choose a launch configuration** from the debug panel:

### 🚀 Available Launch Configurations

| Configuration | Description | Use Case |
|---------------|-------------|----------|
| **🚀 Launch Full App** | Starts both backend + frontend | Complete development |
| **☕ Debug Backend** | Spring Boot with debugging | Backend development |
| **⚛️ Debug Frontend** | React/Vite with hot reload | Frontend development |
| **🧪 Debug Tests** | Run tests with debugging | Test development |
| **🚀 Debug Full Stack** | Both services with debugging | Full-stack debugging |

### ⚡ Available Tasks (Ctrl+Shift+P → "Tasks: Run Task")

| Task | Description |
|------|-------------|
| **🚀 Start Full App** | Runs `run-local.sh` script |
| **☕ Start Backend Only** | Starts Spring Boot server |
| **⚛️ Start Frontend Only** | Starts Vite dev server |
| **🧪 Run All Tests** | Executes frontend + backend tests |
| **🏗️ Build All** | Builds both applications |
| **🧹 Clean All** | Cleans and reinstalls dependencies |

### 📁 Configuration Files

- **`.vscode/launch.json`** - Debug configurations
- **`.vscode/tasks.json`** - Build and run tasks  
- **`.vscode/settings.json`** - Workspace settings
- **`.vscode/extensions.json`** - Recommended extensions

### 🎯 Recommended Extensions

The setup automatically recommends essential extensions:
- **Frontend**: Prettier, ESLint, Tailwind CSS, React snippets
- **Backend**: Kotlin, Java Extension Pack, Spring Boot tools
- **Testing**: Vitest Explorer, Test adapters
- **General**: GitLens, REST Client, Live Share

### 🔥 Pro Tips

1. **Use `Ctrl+Shift+P` → "🚀 Start Full App"** for instant startup
2. **Press `F5`** to start debugging with your last selected configuration
3. **Use the integrated terminal** with pre-configured paths
4. **Enable auto-formatting on save** (already configured)
5. **Use GitLens** for enhanced git visualization

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