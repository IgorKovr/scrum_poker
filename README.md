# Scrum Poker Application

A real-time Scrum Poker application for distributed software engineering teams to estimate user story complexity.

## Features

- Real-time collaboration using WebSockets
- Fibonacci sequence cards (0, 0.5, 1, 2, 3, 5, 8, 13, 20, 40, 100) plus special cards (?, ☕)
- Show/hide estimates functionality
- Reset estimates feature
- Responsive design with Tailwind CSS

## Tech Stack

### Backend
- JVM + Kotlin
- Spring Boot
- WebSocket support

### Frontend
- React + TypeScript
- Tailwind CSS
- React Hook Form
- React Query
- Vite

## Prerequisites

- JDK 17 or higher
- Node.js 18 or higher
- npm or yarn

## Setup and Running

### Backend

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build and run the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```
   
   Or if you don't have Gradle installed:
   ```bash
   java -jar build/libs/scrum-poker-backend-0.0.1-SNAPSHOT.jar
   ```

   The backend will start on http://localhost:8080

### Frontend

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

   The frontend will start on http://localhost:3000

## Usage

1. Open http://localhost:3000 in your browser
2. Enter your name or pseudonym
3. You'll automatically join the default room
4. Select a card to submit your estimate
5. Once all team members have voted, click "Show" to reveal estimates
6. Click "Delete Estimates" to reset and start a new estimation round

## Development

### Backend Development
- The backend uses Spring Boot with Kotlin
- WebSocket endpoint is available at `/ws`
- Main application class: `ScrumPokerApplication.kt`

### Frontend Development
- Uses Vite for fast development
- TypeScript for type safety
- Tailwind CSS for styling
- React Hook Form for form handling
- React Query for server state management

## Project Structure

```
scrum_poker/
├── backend/
│   ├── src/main/kotlin/com/scrumpoker/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── service/
│   │   └── websocket/
│   └── build.gradle.kts
└── frontend/
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   ├── services/
    │   └── types/
    ├── package.json
    └── vite.config.ts
```

## Future Enhancements

- Multiple room support with unique room IDs
- Persistent storage for estimation history
- User authentication
- Export estimation results
- Mobile app support
- Storybook for component development
- Comprehensive test suite with Vitest 