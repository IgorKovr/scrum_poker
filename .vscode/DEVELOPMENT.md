# 💻 VS Code Development Guide

## 🚀 Quick Start Guide

### First Time Setup
1. Open VS Code/Cursor
2. File → Open Workspace → Select `.vscode/scrum-poker.code-workspace`
3. Install recommended extensions when prompted
4. Press `Ctrl+Shift+P` → Type "Tasks: Run Task" → "🚀 Start Full App"

### Daily Development

#### Option 1: Full Stack Development
- **Press `F5`** → Select "🚀 Debug Full Stack"
- Both frontend and backend will start with debugging enabled
- Frontend: http://localhost:3000
- Backend: http://localhost:8080

#### Option 2: Frontend Only
- **Press `F5`** → Select "⚛️ Debug Frontend"
- Only starts React/Vite dev server
- Assumes backend is running elsewhere

#### Option 3: Backend Only
- **Press `F5`** → Select "☕ Debug Backend"
- Only starts Spring Boot server
- Assumes frontend is running elsewhere

## 🧪 Testing Workflows

### Run All Tests
```
Ctrl+Shift+P → "🧪 Run All Tests"
```

### Frontend Tests Only
```
Ctrl+Shift+P → "🧪 Run Frontend Tests"
```

### Backend Tests Only
```
Ctrl+Shift+P → "🧪 Run Backend Tests"
```

### Debug Tests
- **Press `F5`** → Select "🧪 Debug Frontend Tests" or "🧪 Debug Backend Tests"
- Set breakpoints in your test files
- Step through test execution

## 🏗️ Build & Clean

### Build Everything
```
Ctrl+Shift+P → "🏗️ Build All"
```

### Clean & Reinstall
```
Ctrl+Shift+P → "🧹 Clean All"
```

## 🔧 Common Development Scenarios

### 1. Starting a New Feature
```bash
# Option A: Use VS Code tasks (recommended for full app)
Ctrl+Shift+P → "Tasks: Run Task" → "🚀 Start Full App"

# Option B: Use debug configuration (for debugging)
F5 → "🚀 Debug Full Stack"
```

### 2. Frontend Development (React/TypeScript)
```bash
F5 → "⚛️ Debug Frontend"
```
- Hot reload enabled
- TypeScript errors in Problems panel
- ESLint auto-fixes on save
- Tailwind CSS IntelliSense

### 3. Backend Development (Kotlin/Spring Boot)
```bash
F5 → "☕ Debug Backend"
```
- Spring Boot DevTools enabled
- Automatic restarts on file changes
- Kotlin syntax highlighting
- Gradle integration

### 4. Debugging WebSocket Issues
```bash
F5 → "🚀 Debug Full Stack"
```
- Set breakpoints in both frontend and backend
- Use REST Client extension to test endpoints
- Monitor WebSocket connections in browser dev tools

### 5. Running Tests in Watch Mode
```bash
# Frontend (Vitest)
Ctrl+Shift+P → "⚛️ Start Frontend Only"
# Then in terminal: npm test

# Backend (JUnit)
F5 → "🧪 Debug Backend Tests"
```

## 📁 Project Structure Navigation

### Multi-Folder Workspace
The workspace is organized into logical folders:
- **🎯 Scrum Poker (Root)** - Main project files, scripts
- **⚛️ Frontend** - React/TypeScript application
- **☕ Backend** - Kotlin/Spring Boot application

### Quick File Navigation
- `Ctrl+P` - Quick file search across all folders
- `Ctrl+Shift+E` - Toggle file explorer
- `Ctrl+Shift+F` - Global search across project

## 🎯 Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `F5` | Start debugging |
| `Ctrl+F5` | Run without debugging |
| `Ctrl+Shift+P` | Command palette |
| `Ctrl+`` ` | Toggle terminal |
| `Ctrl+Shift+`` ` | New terminal |
| `Ctrl+B` | Toggle sidebar |
| `Ctrl+J` | Toggle panel |

## 🔍 Troubleshooting

### Extensions Not Loading
1. Check `.vscode/extensions.json`
2. Reload VS Code: `Ctrl+Shift+P` → "Developer: Reload Window"
3. Manually install recommended extensions

### Java/Kotlin Issues
1. Ensure Java 17+ is installed
2. Check Java extension pack is installed
3. Reload VS Code workspace

### Frontend Build Issues
1. Check Node.js version (18+)
2. Delete `node_modules` and reinstall: `🧹 Clean All`
3. Check TypeScript errors in Problems panel

### Backend Build Issues
1. Check Gradle wrapper: `./gradlew --version`
2. Clean build: `./gradlew clean build`
3. Check Spring Boot logs in terminal

### Port Conflicts
- Frontend default: 3000
- Backend default: 8080
- Change ports in `vite.config.ts` and `application.yml`

## 📊 Performance Tips

1. **Use workspace folders** for better IntelliSense
2. **Enable format on save** (already configured)
3. **Use integrated terminal** with proper working directories
4. **Exclude build folders** from search (already configured)
5. **Use GitLens** for better git integration

## 🔗 Useful Resources

- [Vite Documentation](https://vitejs.dev/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Kotlin Language Guide](https://kotlinlang.org/docs/)
- [React Documentation](https://react.dev/)
- [VS Code Java Guide](https://code.visualstudio.com/docs/java/java-tutorial) 