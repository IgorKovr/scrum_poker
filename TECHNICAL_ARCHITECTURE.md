# 🏗️ Technical Architecture Documentation

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Technology Stack](#technology-stack)
4. [System Components](#system-components)
5. [Data Flow & Communication](#data-flow--communication)
6. [Module Architecture](#module-architecture)
7. [Development Environment](#development-environment)
8. [Deployment Architecture](#deployment-architecture)
9. [Security & Performance](#security--performance)
10. [Monitoring & Observability](#monitoring--observability)

---

## System Overview

Scrum Poker is a real-time collaborative estimation tool built with a modern web architecture. The system enables distributed teams to perform story point estimation sessions with live updates and interactive features.

### Key Characteristics

- **Real-time Communication**: WebSocket-based bi-directional communication
- **Scalable Architecture**: Stateless backend with in-memory session management
- **Modern Frontend**: React with TypeScript for type safety
- **Cloud-Native**: Designed for containerized deployment on platforms like Railway
- **Sleep-Mode Compatible**: No background tasks - allows cloud platforms to sleep when idle
- **Event-Driven Cleanup**: Memory leak prevention triggered by user disconnections

---

## Architecture Patterns

### 1. **Client-Server Architecture**

```
┌─────────────────┐    WebSocket/HTTP    ┌─────────────────┐
│                 │ ←─────────────────→ │                 │
│   Frontend      │                     │   Backend       │
│   (React SPA)   │                     │   (Spring Boot) │
│                 │                     │                 │
└─────────────────┘                     └─────────────────┘
```

### 2. **Event-Driven Communication**

- Real-time updates via WebSocket events
- Message-based communication protocol
- Event sourcing for state synchronization

### 3. **Component-Based Frontend**

- Modular React components
- Shared state management
- Responsive design patterns

### 4. **Service-Oriented Backend**

- Clear separation of concerns
- Dependency injection
- Business logic encapsulation

---

## Technology Stack

### **Frontend Technologies**

| Technology          | Version | Purpose         | Justification                                                   |
| ------------------- | ------- | --------------- | --------------------------------------------------------------- |
| **React**           | 18.x    | UI Framework    | Component-based, mature ecosystem, excellent TypeScript support |
| **TypeScript**      | 5.x     | Type System     | Type safety, better IDE support, reduced runtime errors         |
| **Vite**            | 4.x     | Build Tool      | Fast development server, optimized builds, modern tooling       |
| **Tailwind CSS**    | 3.x     | Styling         | Utility-first, responsive design, dark mode support             |
| **React Router**    | 6.x     | Navigation      | Client-side routing, URL management                             |
| **React Hook Form** | 7.x     | Form Management | Performance-optimized forms, validation                         |

### **Backend Technologies**

| Technology           | Version | Purpose                 | Justification                                             |
| -------------------- | ------- | ----------------------- | --------------------------------------------------------- |
| **Kotlin**           | 1.9.x   | Programming Language    | Concise syntax, null safety, Java interoperability        |
| **Spring Boot**      | 3.2.x   | Application Framework   | Production-ready, extensive ecosystem, auto-configuration |
| **Spring WebSocket** | 6.1.x   | Real-time Communication | Standardized WebSocket support, integration with Spring   |
| **Jackson**          | 2.15.x  | JSON Processing         | Fast serialization/deserialization, Spring integration    |
| **SLF4J + Logback**  | 2.x     | Logging                 | Structured logging, performance, Spring Boot default      |
| **Gradle**           | 8.5     | Build Tool              | Kotlin DSL, dependency management, multi-platform support |

### **Development & Testing**

| Technology                | Purpose           | Coverage                               |
| ------------------------- | ----------------- | -------------------------------------- |
| **Vitest**                | Frontend Testing  | Unit tests, component testing, mocking |
| **React Testing Library** | Component Testing | User-centric testing, accessibility    |
| **JUnit 5**               | Backend Testing   | Unit tests, integration tests          |
| **Mockito**               | Mocking Framework | Test isolation, behavior verification  |
| **GitHub Actions**        | CI/CD             | Automated testing, deployment          |

### **Deployment & Infrastructure**

| Technology  | Purpose          | Benefits                                                    |
| ----------- | ---------------- | ----------------------------------------------------------- |
| **Railway** | Cloud Platform   | Simple deployment, automatic scaling, integrated monitoring |
| **Docker**  | Containerization | Consistent environments, easy deployment                    |
| **Nginx**   | Reverse Proxy    | Static file serving, load balancing                         |
| **Git**     | Version Control  | Distributed development, branching strategies               |

---

## System Components

### **Frontend Architecture**

```
src/
├── components/           # Reusable UI components
│   ├── PokerCard.tsx    # Individual estimation card
│   └── UserTable.tsx    # User list and session controls
├── pages/               # Route-level components
│   ├── NameEntry.tsx    # User onboarding
│   └── PokerRoom.tsx    # Main estimation interface
├── services/            # External API integration
│   └── websocket.ts     # WebSocket client service
├── types/               # TypeScript definitions
│   └── index.ts         # Shared type definitions
├── hooks/               # Custom React hooks
├── App.tsx              # Root application component
└── main.tsx             # Application entry point
```

#### **Component Hierarchy**

```
App
├── Router
    ├── NameEntry
    │   └── Form Components
    └── PokerRoom
        ├── UserTable
        │   ├── User Rows
        │   └── Action Buttons
        └── PokerCard Grid
            └── Individual PokerCards
```

### **Backend Architecture**

```
src/main/kotlin/com/scrumpoker/
├── controller/          # REST API endpoints
│   └── HealthController.kt
├── service/             # Business logic layer
│   └── RoomService.kt   # Core room management & maintenance
├── websocket/           # WebSocket communication
│   ├── ScrumPokerWebSocketHandler.kt
│   └── WebSocketConfig.kt
├── model/               # Data models
│   └── Models.kt        # All data classes and enums
└── ScrumPokerApplication.kt # Main application class
```

#### **Service Layer Design**

```
┌─────────────────────┐
│   WebSocket Layer   │ ← User interactions
├─────────────────────┤
│   Service Layer     │ ← Business logic
├─────────────────────┤
│   Model Layer       │ ← Data structures
└─────────────────────┘
```

---

## Data Flow & Communication

### **WebSocket Communication Protocol**

#### **Message Structure**

```typescript
interface WebSocketMessage {
  type: MessageType;
  payload: any;
}

enum MessageType {
  JOIN = "JOIN",
  VOTE = "VOTE",
  SHOW_ESTIMATES = "SHOW_ESTIMATES",
  HIDE_ESTIMATES = "HIDE_ESTIMATES",
  DELETE_ESTIMATES = "DELETE_ESTIMATES",
  ROOM_UPDATE = "ROOM_UPDATE",
  USER_LEFT = "USER_LEFT",
  ERROR = "ERROR",
}
```

#### **Message Flow Diagram**

```
Client                    Server                    Other Clients
  │                         │                           │
  ├─ JOIN {name, roomId} ──→│                           │
  │                         ├─ Create User             │
  │                         ├─ Add to Room             │
  │                         ├─ Store Session           │
  │                         │                           │
  │←─ JOIN {userId} ────────┤                           │
  │                         │                           │
  │                         ├─ ROOM_UPDATE ───────────→│
  │                         │                           │
  ├─ VOTE {userId, estimate}→│                          │
  │                         ├─ Update User Vote        │
  │                         │                           │
  │←─ ROOM_UPDATE ──────────┼─ ROOM_UPDATE ───────────→│
  │                         │                           │
  ├─ SHOW_ESTIMATES ───────→│                           │
  │                         ├─ Reveal All Estimates    │
  │                         │                           │
  │←─ ROOM_UPDATE ──────────┼─ ROOM_UPDATE ───────────→│
```

### **State Management Flow**

#### **Frontend State**

```typescript
// Local Component State
├── User Input (name, room ID)
├── Selected Card Value
├── Connection Status
└── Error Messages

// Shared Application State
├── Current User Information
├── Room State (users, estimates, visibility)
├── WebSocket Connection
└── Navigation State
```

#### **Backend State**

```kotlin
// In-Memory Data Structures
├── ConcurrentHashMap<String, Room>     // rooms
├── ConcurrentHashMap<String, User>     // userSessions
├── ConcurrentHashMap<String, WebSocketSession> // sessions
└── ConcurrentHashMap<String, String>   // sessionToUser

// Memory Management
├── Resource Limits (MAX_ROOMS, MAX_USERS, etc.)
├── Cleanup Operations (orphaned data removal)
├── Monitoring Metrics (memory usage, active connections)
└── Maintenance Scheduling (automated cleanup)
```

---

## Module Architecture

### **Frontend Modules**

#### **1. User Interface Layer**

```typescript
// Component Responsibilities
PokerCard: {
  responsibilities: [
    "Render estimation values",
    "Handle user selection",
    "Visual feedback (selected state)",
    "Accessibility support"
  ],
  dependencies: ["React", "Tailwind CSS"],
  stateManagement: "Local state + callbacks"
}

UserTable: {
  responsibilities: [
    "Display room participants",
    "Show voting status",
    "Session control buttons",
    "Estimate visibility"
  ],
  dependencies: ["React", "UserState interface"],
  stateManagement: "Props from parent"
}
```

#### **2. Service Layer**

```typescript
WebSocketService: {
  responsibilities: [
    "WebSocket connection management",
    "Message serialization/deserialization",
    "Connection state handling",
    "Error recovery"
  ],
  patterns: ["Singleton", "Observer"],
  errorHandling: "Retry logic + user feedback"
}
```

#### **3. Type System**

```typescript
// Core Domain Types
interface User {
  id: string;
  name: string;
  roomId: string;
  estimate?: string;
  hasVoted: boolean;
}

interface Room {
  id: string;
  users: User[];
  showEstimates: boolean;
}

// Communication Types
interface JoinRoomRequest {
  name: string;
  roomId: string;
}

interface VoteRequest {
  userId: string;
  roomId: string;
  estimate: string;
}
```

### **Backend Modules**

#### **1. Business Logic Layer**

```kotlin
RoomService: {
  responsibilities: [
    "Room lifecycle management",
    "User session tracking",
    "Voting logic implementation",
    "Data consistency maintenance",
    "Memory leak prevention",
    "Event-driven maintenance cleanup"
  ],
  patterns: ["Service", "Repository"],
  concurrency: "ConcurrentHashMap for thread safety"
}

// HeartbeatService removed to allow Railway sleep mode
// Maintenance cleanup now happens during WebSocket disconnections
```

#### **2. Communication Layer**

```kotlin
ScrumPokerWebSocketHandler: {
  responsibilities: [
    "WebSocket lifecycle management",
    "Message routing and processing",
    "Session cleanup and recovery",
    "Error handling and logging"
  ],
  patterns: ["Handler", "Observer"],
  concurrency: "Thread-safe session management"
}

WebSocketConfig: {
  responsibilities: [
    "WebSocket endpoint configuration",
    "Handler registration",
    "CORS policy setup"
  ],
  framework: "Spring WebSocket"
}
```

#### **3. Data Layer**

```kotlin
// Domain Models
data class User(
  val id: String,
  val name: String,
  val roomId: String,
  var estimate: String? = null,
  var hasVoted: Boolean = false
)

data class Room(
  val id: String,
  val users: MutableList<User> = mutableListOf(),
  var showEstimates: Boolean = false
)

// Communication Models
enum class MessageType {
  JOIN, VOTE, SHOW_ESTIMATES,
  HIDE_ESTIMATES, DELETE_ESTIMATES,
  ROOM_UPDATE, USER_LEFT, ERROR
}
```

---

## Development Environment

### **Local Development Setup**

#### **Prerequisites**

```bash
# Required Software
- Node.js 16+ (Frontend)
- Java 17+ (Backend)
- Git (Version control)

# Optional Tools
- VS Code/Cursor (IDE)
- Docker (Containerization)
- Railway CLI (Deployment)
```

#### **Development Workflow**

```bash
# 1. Clone and Setup
git clone <repository>
cd scrum_poker
npm install                    # Install root dependencies

# 2. Frontend Development
cd frontend
npm install                    # Install frontend dependencies
npm run dev                    # Start development server (http://localhost:3000)
npm test                       # Run frontend tests
npm run build                  # Build for production

# 3. Backend Development
cd backend
./gradlew bootRun             # Start backend server (http://localhost:8080)
./gradlew test                # Run backend tests
./gradlew build               # Build for production

# 4. Full Application
npm start                     # Start both frontend and backend
```

#### **IDE Configuration (VS Code/Cursor)**

```json
// .vscode/launch.json - Debug configurations
{
  "configurations": [
    {
      "name": "🚀 Run Full App",
      "type": "node",
      "request": "launch",
      "program": "${workspaceFolder}/run.js"
    },
    {
      "name": "🎯 Debug Frontend",
      "type": "node",
      "request": "launch",
      "program": "${workspaceFolder}/frontend/node_modules/.bin/vite"
    }
  ]
}

// .vscode/tasks.json - Build and test tasks
{
  "tasks": [
    {
      "label": "🧪 Run All Tests",
      "type": "shell",
      "command": "npm run test"
    },
    {
      "label": "🌐 Open App in Browser",
      "type": "shell",
      "command": "open http://localhost:3000"
    }
  ]
}
```

### **Testing Strategy**

#### **Frontend Testing**

```typescript
// Unit Tests (Vitest + React Testing Library)
describe("PokerCard Component", () => {
  it("should render card value correctly", () => {
    render(<PokerCard value="5" isSelected={false} onClick={() => {}} />);
    expect(screen.getByText("5")).toBeInTheDocument();
  });

  it("should handle click events", () => {
    const handleClick = vi.fn();
    render(<PokerCard value="5" isSelected={false} onClick={handleClick} />);
    fireEvent.click(screen.getByRole("button"));
    expect(handleClick).toHaveBeenCalledWith("5");
  });
});

// Integration Tests
describe("WebSocket Service", () => {
  it("should connect and send messages", async () => {
    const service = new WebSocketService();
    await service.connect("ws://localhost:8080/ws");
    service.sendMessage({ type: "JOIN", payload: { name: "Test" } });
    expect(service.isConnected()).toBe(true);
  });
});
```

#### **Backend Testing**

```kotlin
// Unit Tests (JUnit 5 + Mockito)
@Test
fun `should create user and add to room`() {
    // Given
    val roomService = RoomService()

    // When
    val user = roomService.joinRoom("Alice", "test-room")

    // Then
    assertThat(user.name).isEqualTo("Alice")
    assertThat(user.roomId).isEqualTo("test-room")
    assertThat(roomService.getAllRooms()).hasSize(1)
}

// Integration Tests (Spring Boot Test)
@SpringBootTest
@TestMethodOrder(OrderAnnotation::class)
class ScrumPokerWebSocketHandlerIntegrationTest {

    @Test
    @Order(1)
    fun `should handle user join flow`() {
        // Test complete WebSocket interaction flow
    }
}
```

---

## Deployment Architecture

### **Cloud Deployment (Railway)**

#### **Application Structure**

```
Production Environment
├── Frontend (Static Files)
│   ├── Served via Nginx
│   ├── Build artifacts from Vite
│   └── Environment-specific config
├── Backend (Spring Boot App)
│   ├── Embedded Tomcat server
│   ├── WebSocket endpoints
│   └── REST API endpoints
└── Shared Infrastructure
    ├── Logging aggregation
    ├── Health monitoring
    └── Auto-scaling policies
```

#### **Build & Deployment Process**

```yaml
# .github/workflows/deploy.yml
name: Deploy to Railway

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: "18"
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: "17"

      - name: Run Frontend Tests
        run: |
          cd frontend
          npm ci
          npm test

      - name: Run Backend Tests
        run: |
          cd backend
          ./gradlew test

  deploy:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Railway
        uses: railway/cli@v2
        with:
          command: up
        env:
          RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
```

#### **Container Configuration**

```dockerfile
# Multi-stage Docker build
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM openjdk:17-jdk-slim AS backend-build
WORKDIR /app/backend
COPY backend/gradle* ./
COPY backend/src ./src
RUN ./gradlew build

FROM openjdk:17-jre-slim
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar
COPY --from=frontend-build /app/frontend/dist /static
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### **Environment Configuration**

#### **Development**

```properties
# application-dev.yml
server:
  port: 8080
logging:
  level:
    com.scrumpoker: DEBUG
    org.springframework.web.socket: DEBUG
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

#### **Production**

```properties
# application-prod.yml
server:
  port: ${PORT:8080}
logging:
  level:
    com.scrumpoker: INFO
  pattern:
    console: "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
management:
  endpoints:
    web:
      exposure:
        include: health
```

---

## Security & Performance

### **Security Measures**

#### **Frontend Security**

```typescript
// Input Validation
const validateRoomId = (roomId: string): boolean => {
  return /^[a-zA-Z0-9-_]{1,50}$/.test(roomId);
};

const validateUserName = (name: string): boolean => {
  return name.length >= 1 && name.length <= 50;
};

// XSS Prevention
const sanitizeInput = (input: string): string => {
  return input.replace(/[<>\"']/g, "");
};
```

#### **Backend Security**

```kotlin
// Input Validation
@Service
class ValidationService {
    fun validateJoinRequest(request: JoinRoomRequest): Boolean {
        return request.name.isNotBlank() &&
               request.name.length <= 50 &&
               request.roomId.matches(Regex("[a-zA-Z0-9-_]{1,50}"))
    }
}

// Rate Limiting (Future Enhancement)
@Component
class RateLimitingInterceptor {
    // Implement rate limiting logic
}
```

### **Performance Optimizations**

#### **Frontend Performance**

```typescript
// Component Memoization
const PokerCard = React.memo(({ value, isSelected, onClick }) => {
  // Component implementation
});

// Lazy Loading
const PokerRoom = React.lazy(() => import("./pages/PokerRoom"));

// Bundle Optimization (vite.config.ts)
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ["react", "react-dom"],
          router: ["react-router-dom"],
        },
      },
    },
  },
});
```

#### **Backend Performance**

```kotlin
// Memory Management
class RoomService {
    private val MAX_ROOMS = 1000
    private val MAX_USERS_PER_ROOM = 50
    private val MAX_TOTAL_USERS = 5000

    // Concurrent data structures for thread safety
    private val rooms = ConcurrentHashMap<String, Room>()
    private val userSessions = ConcurrentHashMap<String, User>()
}

// Connection Pooling
@Configuration
class WebSocketConfig {
    @Bean
    fun webSocketConfigurer(): WebSocketConfigurer {
        return WebSocketConfigurer { registry ->
            registry.addHandler(webSocketHandler(), "/ws")
                .setAllowedOrigins("*")
                .withSockJS()
        }
    }
}
```

---

## Monitoring & Observability

### **Application Metrics**

#### **System Health Monitoring**

```kotlin
// HeartbeatService removed to enable Railway sleep mode
// Maintenance cleanup now happens during WebSocket disconnections:

override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    // ... user cleanup logic ...

    // Perform maintenance cleanup to ensure data consistency and prevent memory leaks
    // This replaces the scheduled cleanup since we no longer use heartbeat service
    roomService.performMaintenanceCleanup()
}
```

#### **Error Tracking & Alerting**

```kotlin
// Memory Leak Detection
if (memoryUtilization > 80) {
    logger.warn("⚠️ HIGH MEMORY USAGE: {:.1f}%", memoryUtilization)
}

// Resource Limit Monitoring
if (totalRooms > MAX_ROOMS * 0.8) {
    logger.warn("⚠️ Room count approaching limit: {}/{}", totalRooms, MAX_ROOMS)
}

// Data Consistency Validation
private fun validateDataConsistency() {
    val usersInRooms = rooms.values.flatMap { it.users }.map { it.id }.toSet()
    val usersInSessions = userSessions.keys.toSet()

    val orphanedInSessions = usersInSessions - usersInRooms
    if (orphanedInSessions.isNotEmpty()) {
        logger.warn("🚨 Data inconsistency detected: {} orphaned users",
                   orphanedInSessions.size)
    }
}
```

### **Logging Strategy**

#### **Structured Logging Format**

```kotlin
// User Action Logging
logger.info("📋 New user joined with name '{}' in room '{}' (User ID: {})",
           name, roomId, user.id)

logger.info("🎯 User '{}' selected card '{}' in room '{}' (User ID: {})",
           user.name, estimate, user.roomId, userId)

logger.info("👁️ User '{}' pressed Show estimates in room '{}' (User ID: {})",
           triggerUser.name, roomId, triggerUserId)

// System Events
logger.info("🔌 New WebSocket connection established: {} from {}",
           session.id, session.remoteAddress)

logger.info("🧹 Cleaning up empty room '{}'", user.roomId)
```

#### **Log Analysis Commands**

```bash
# Monitor user activity
grep "📋 New user joined" scrum-poker.log | tail -10

# Track memory issues
grep "⚠️ HIGH MEMORY" scrum-poker.log

# Room usage analytics
grep "in room" scrum-poker.log | grep -o "room '[^']*'" | sort | uniq -c | sort -nr

# WebSocket connection monitoring
grep "🔌" scrum-poker.log | tail -20
```

---

## Future Enhancements

### **Scalability Improvements**

- **Database Integration**: Replace in-memory storage with PostgreSQL/Redis
- **Horizontal Scaling**: Multiple backend instances with shared state
- **Load Balancing**: Distribute WebSocket connections across instances
- **Caching Strategy**: Redis for session data and room state

### **Feature Extensions**

- **User Authentication**: OAuth2/JWT token-based authentication
- **Room Persistence**: Save and restore estimation sessions
- **Advanced Analytics**: Estimation history and team metrics
- **Mobile App**: React Native or Flutter mobile client

### **Operational Enhancements**

- **Health Checks**: More comprehensive health endpoints
- **Metrics Collection**: Prometheus/Grafana integration
- **Error Tracking**: Sentry or similar error monitoring
- **Performance Monitoring**: APM tools integration

---

This technical architecture document provides a comprehensive overview of the Scrum Poker application's design, implementation, and operational aspects. It serves as a reference for developers, architects, and stakeholders to understand the system's technical foundation and design decisions.
