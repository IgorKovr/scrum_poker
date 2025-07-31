# 📝 User Action Logging

The Scrum Poker application includes comprehensive logging of all user interactions for monitoring, debugging, and audit purposes.

## 🎯 Logged Actions

### 1. User Joining Room

```
📋 New user joined with name 'Alice' in room 'daily-standup' (User ID: 123e4567-e89b-12d3-a456-426614174000)
```

- **When**: User successfully joins a poker room
- **Includes**: User name, room ID, generated user ID
- **Triggered by**: WebSocket JOIN message

### 2. Card Selection (Voting)

```
🎯 User 'Bob' selected card '5' in room 'daily-standup' (User ID: 123e4567-e89b-12d3-a456-426614174001)
```

- **When**: User selects an estimation card
- **Includes**: User name, selected card value, room ID, user ID
- **Triggered by**: WebSocket VOTE message

### 3. Show Estimates

```
👁️ User 'Charlie' pressed Show estimates in room 'daily-standup' (User ID: 123e4567-e89b-12d3-a456-426614174002)
```

- **When**: User reveals all estimates in the room
- **Includes**: User name who triggered the action, room ID, user ID
- **Triggered by**: WebSocket SHOW_ESTIMATES message

### 4. Delete Estimates

```
🗑️ User 'Dave' pressed Delete Estimations in room 'daily-standup' (User ID: 123e4567-e89b-12d3-a456-426614174003)
```

- **When**: User clears all estimates to start a new round
- **Includes**: User name who triggered the action, room ID, user ID
- **Triggered by**: WebSocket DELETE_ESTIMATES message

### 5. User Leaving Room

```
👋 User 'Eve' left room 'daily-standup' (User ID: 123e4567-e89b-12d3-a456-426614174004)
```

- **When**: User explicitly leaves or disconnects from a room
- **Includes**: User name, room ID, user ID
- **Triggered by**: WebSocket disconnection or explicit leave

### 6. WebSocket Connection Events

```
🔌 New WebSocket connection established: abc123-session-id from /192.168.1.100:54321
🔌 WebSocket connection closed: abc123-session-id - Status: 1000 (Normal closure)
```

- **When**: WebSocket connections are established or closed
- **Includes**: Session ID, remote address, close status and reason
- **Triggered by**: WebSocket lifecycle events

### 7. Room Cleanup

```
🧹 Cleaning up empty room 'daily-standup'
```

- **When**: The last user leaves a room and it's automatically cleaned up
- **Includes**: Room ID being cleaned up
- **Triggered by**: Room becoming empty

## 🔧 Technical Implementation

### Logging Framework

- **Library**: SLF4J with Logback (Spring Boot default)
- **Level**: INFO for all user actions
- **Format**: Structured format with emojis for easy visual parsing

### Location

- **RoomService**: Business logic logging (join, leave, vote, show/delete estimates)
- **WebSocketHandler**: Connection events and message processing

### Configuration

Logging is configured through Spring Boot's default logging configuration. For production, you can:

1. **Adjust log levels** in `application.yml`:

```yaml
logging:
  level:
    com.scrumpoker.service.RoomService: INFO
    com.scrumpoker.websocket.ScrumPokerWebSocketHandler: INFO
```

2. **Configure log output** for structured logging:

```yaml
logging:
  pattern:
    console: "%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/scrum-poker.log
```

## 📊 Log Analysis

### Monitoring User Activity

```bash
# Count user joins per hour
grep "📋 New user joined" scrum-poker.log | grep "$(date +%Y-%m-%d.*%H)" | wc -l

# Find most active rooms
grep "in room" scrum-poker.log | grep -o "room '[^']*'" | sort | uniq -c | sort -nr

# Track estimation sessions
grep "👁️.*pressed Show" scrum-poker.log | wc -l
```

### Debugging Issues

```bash
# Find all actions by a specific user
grep "User 'Alice'" scrum-poker.log

# Track room lifecycle
grep "room 'daily-standup'" scrum-poker.log

# Monitor WebSocket connections
grep "🔌" scrum-poker.log
```

## 🔒 Privacy Considerations

- **User Names**: Only display names chosen by users (no personal information)
- **User IDs**: Generated UUIDs with no personal data
- **IP Addresses**: WebSocket remote addresses for debugging (consider masking in production)
- **Room IDs**: Room identifiers chosen by users

## 🚀 Production Recommendations

1. **Log Rotation**: Configure log rotation to prevent disk space issues
2. **Log Aggregation**: Use tools like ELK Stack, Splunk, or cloud logging services
3. **Alerting**: Set up alerts for connection issues or unusual activity patterns
4. **Retention**: Define log retention policies based on compliance requirements
5. **Performance**: Monitor logging overhead in high-traffic scenarios

## 🧪 Testing

All logging functionality is tested in `RoomServiceLoggingTest.kt`:

- Individual action logging verification
- Log message format validation
- Complete user journey testing
- Error scenario handling

Run logging tests:

```bash
cd backend
./gradlew test --tests "*RoomServiceLoggingTest*"
```

The logging system provides complete visibility into user interactions while maintaining performance and user privacy.
