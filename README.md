# Scrum Poker Application

[![🧪 Test Suite](https://github.com/IgorKovr/scrum_poker/actions/workflows/test.yml/badge.svg)](https://github.com/IgorKovr/scrum_poker/actions/workflows/test.yml)

A real-time collaborative estimation tool for distributed software teams using the Planning Poker technique.

## 🚀 Features

- Real-time WebSocket communication
- Multiple concurrent rooms
- Fibonacci sequence cards (0.5, 1, 2, 3, 5, 8, 13, 20, 40)
- Show/Hide estimates functionality
- Clean, modern UI with Tailwind CSS
- **Dark mode support** - automatically responds to system/browser preferences
- **Comprehensive user action logging** - complete audit trail of all user interactions

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
3. **Start the application using tasks or debug configurations**:

**For Running the Full App** (multiple easy options):

- **Option 1**: Press `F5` → Select "🚀 Run Full App" (easiest!)
- **Option 2**: Press `Ctrl+Shift+P` → "Tasks: Run Task" → "🚀 Start Full App"
- **Option 3**: Open `run.js` and click the ▶️ "Run" button in Cursor
- **Option 4**: Terminal: `npm start` or `node run.js`

**For Debugging Individual Services**:

### 🚀 Available Launch Configurations

| Configuration           | Description                        | Use Case             |
| ----------------------- | ---------------------------------- | -------------------- |
| **🚀 Run Full App**     | Start both services (no debugging) | Quick development    |
| **☕ Debug Backend**    | Spring Boot with debugging         | Backend development  |
| **⚛️ Debug Frontend**   | React/Vite with hot reload         | Frontend development |
| **🧪 Debug Tests**      | Run tests with debugging           | Test development     |
| **🚀 Debug Full Stack** | Both services with debugging       | Full-stack debugging |

### ⚡ Available Tasks (Ctrl+Shift+P → "Tasks: Run Task")

| Task                          | Description                         |
| ----------------------------- | ----------------------------------- |
| **🚀 Start Full App**         | Runs `run-local.sh` script          |
| **☕ Start Backend Only**     | Starts Spring Boot server           |
| **⚛️ Start Frontend Only**    | Starts Vite dev server              |
| **🧪 Run All Tests**          | Executes frontend + backend tests   |
| **🏗️ Build All**              | Builds both applications            |
| **🧹 Clean All**              | Cleans and reinstalls dependencies  |
| **🌐 Open App in Browser**    | Opens app in default browser        |
| **🏥 Open Backend Health**    | Opens health endpoint in browser    |
| **🌐 Open in Simple Browser** | Opens app in VS Code Simple Browser |

### 🌐 Browser Options & Hotkeys

| Hotkey         | Action                        | Description                      |
| -------------- | ----------------------------- | -------------------------------- |
| `Ctrl+Shift+O` | **Simple Browser (App)**      | Opens app inside VS Code/Cursor  |
| `Ctrl+Alt+O`   | **External Browser (App)**    | Opens app in default browser     |
| `Ctrl+Shift+H` | **Simple Browser (Health)**   | Opens health check inside editor |
| `Ctrl+Alt+H`   | **External Browser (Health)** | Opens health check in browser    |

**Command Palette Options:**

- `Ctrl+Shift+P` → "Simple Browser: Show" → Enter URL
- `Ctrl+Shift+P` → "🌐 Open App in Browser"
- `Ctrl+Shift+P` → "🏥 Open Backend Health"

**Dark Mode:** The app automatically detects and responds to your system's dark mode preference. No manual toggle needed!

**User Action Logging:** All user interactions are automatically logged with detailed information:

- 📋 User joining: "New user joined with name 'Alice' in room 'room-123'"
- 🎯 Card selection: "User 'Bob' selected card '5' in room 'room-123'"
- 👁️ Show estimates: "User 'Charlie' pressed Show estimates in room 'room-123'"
- 🗑️ Delete estimates: "User 'Dave' pressed Delete Estimations in room 'room-123'"
- 👋 User leaving: "User 'Eve' left room 'room-123'"
- 🔌 Connection events: WebSocket connections and disconnections with remote addresses
- 🧹 Room cleanup: Automatic cleanup when rooms become empty

All logs include user names, room IDs, user IDs, and timestamps for complete audit trails.

## 📖 Documentation

### 🏗️ Architecture & Technical Documentation

- **[TECHNICAL_ARCHITECTURE.md](TECHNICAL_ARCHITECTURE.md)** - Comprehensive technical documentation covering system architecture, modules, technologies, and their interactions
- **[LOGGING.md](LOGGING.md)** - Detailed user action logging system documentation
- **[MEMORY_LEAK_FIXES.md](MEMORY_LEAK_FIXES.md)** - Memory leak prevention and fixes documentation
- **[DARK_MODE.md](DARK_MODE.md)** - Dark mode implementation guide

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

1. **Use `Ctrl+Shift+P` → "Tasks: Run Task" → "🚀 Start Full App"** for instant startup
2. **Press `F5`** to start debugging with your last selected configuration
3. **Use the integrated terminal** with pre-configured paths
4. **Browser shortcuts**: `Ctrl+Shift+O` (Simple Browser), `Ctrl+Alt+O` (External Browser)
5. **Health check**: `Ctrl+Shift+H` (Simple Browser), `Ctrl+Alt+H` (External Browser)
6. **Enable auto-formatting on save** (already configured)
7. **Use GitLens** for enhanced git visualization

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
