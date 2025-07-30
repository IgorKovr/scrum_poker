# Setup Guide for Scrum Poker Application

## Current Issues

Based on the system check, the following tools need to be installed:

### Required Software
1. **Node.js** (version 18 or higher) - Required for the frontend
2. **Java JDK** (version 17 or higher) - Required for the backend
3. **npm** - Comes with Node.js

## Installation Instructions

### macOS Installation

#### 1. Install Homebrew (if not already installed)
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

#### 2. Install Node.js and npm
```bash
brew install node@18
```

#### 3. Install Java JDK 17
```bash
brew install openjdk@17
```

After installation, you may need to add Java to your PATH:
```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Alternative: Using Official Installers

#### Node.js
Download from: https://nodejs.org/en/download/

#### Java JDK 17
Download from: https://www.oracle.com/java/technologies/downloads/#java17

## Verification

After installation, verify the installations:

```bash
node --version  # Should show v18.x.x or higher
npm --version   # Should show 9.x.x or higher
java --version  # Should show version 17 or higher
```

## Running the Application

### Backend
```bash
cd backend
./gradlew wrapper  # This will create the Gradle wrapper
./gradlew bootRun
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Known TypeScript Errors

The TypeScript errors shown in the IDE are due to missing node_modules. These will be resolved after running `npm install`.

## Troubleshooting

### If Gradle wrapper is missing:
The project needs the Gradle wrapper files. You can either:
1. Install Gradle globally: `brew install gradle`
2. Then run: `gradle wrapper` in the backend directory

### If ports are in use:
- Backend runs on port 8080
- Frontend runs on port 3000

You can change these in:
- Backend: `backend/src/main/resources/application.yml`
- Frontend: `frontend/vite.config.ts` 