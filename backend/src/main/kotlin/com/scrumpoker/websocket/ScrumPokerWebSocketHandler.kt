/**
 * ScrumPokerWebSocketHandler.kt - WebSocket Message Handler for Real-time Communication
 *
 * This class handles all WebSocket communication between the frontend clients and the backend
 * server. It processes incoming messages, manages client sessions, and broadcasts updates to all
 * connected clients in real-time.
 *
 * Key Responsibilities:
 * 1. WebSocket connection lifecycle management (connect, disconnect)
 * 2. Message parsing and routing based on message type
 * 3. Business logic integration via RoomService
 * 4. Real-time broadcasting of room state updates
 * 5. Session tracking and cleanup
 *
 * Message Flow:
 * 1. Client sends JSON message via WebSocket
 * 2. Handler parses message and extracts type and payload
 * 3. Appropriate handler method processes the business logic
 * 4. Room state is updated via RoomService
 * 5. Updated state is broadcast to all room participants
 *
 * The handler ensures that all participants in a room receive real-time updates when any state
 * changes occur, providing a synchronized view of the estimation session across all clients.
 *
 * Thread Safety: This handler is designed to be thread-safe, with concurrent access managed through
 * the underlying Spring WebSocket framework and concurrent collections.
 */
package com.scrumpoker.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.scrumpoker.model.*
import com.scrumpoker.service.RoomService
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

/**
 * ScrumPokerWebSocketHandler - Main WebSocket message processor
 *
 * This class extends Spring's TextWebSocketHandler to provide custom message processing for the
 * Scrum Poker application. It maintains session state and coordinates between WebSocket connections
 * and business logic.
 *
 * Dependencies:
 * - RoomService: For all business logic operations
 * - ObjectMapper: For JSON serialization/deserialization
 *
 * Session Management:
 * - sessions: Maps user ID to WebSocket session for message delivery
 * - sessionToUser: Maps session ID to user ID for reverse lookup during cleanup
 *
 * The handler processes five main message types:
 * 1. JOIN - User joins a room
 * 2. VOTE - User submits an estimate
 * 3. SHOW_ESTIMATES - Reveal all estimates in the room
 * 4. HIDE_ESTIMATES - Hide estimates again
 * 5. DELETE_ESTIMATES - Clear all estimates and start fresh
 */
@Component
class ScrumPokerWebSocketHandler(
        private val roomService: RoomService,
        private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    /**
     * Logger for tracking WebSocket events and user interactions
     *
     * Used to log connection events, message processing, and errors for monitoring, debugging, and
     * audit purposes.
     */
    private val logger = LoggerFactory.getLogger(ScrumPokerWebSocketHandler::class.java)

    /**
     * Maps user ID to WebSocket session for message delivery
     *
     * This allows the server to send messages directly to specific users or broadcast to all users
     * in a room by looking up their sessions.
     */
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    /**
     * Maps WebSocket session ID to user ID for reverse lookups
     *
     * This is used during connection cleanup to find which user a disconnecting session belongs to,
     * enabling proper removal from rooms and session tracking.
     */
    private val sessionToUser = ConcurrentHashMap<String, String>()

    // Memory management constants for WebSocket sessions
    private val MAX_CONCURRENT_SESSIONS = 10000 // Maximum concurrent WebSocket sessions

    /** Logs WebSocket session memory usage for monitoring */
    private fun logSessionMemoryUsage() {
        val sessionCount = sessions.size
        val sessionToUserCount = sessionToUser.size

        logger.info(
                "📊 WebSocket Memory: {} sessions, {} session mappings",
                sessionCount,
                sessionToUserCount
        )

        if (sessionCount > MAX_CONCURRENT_SESSIONS * 0.8) {
            logger.warn(
                    "⚠️ WebSocket session count approaching limit: {}/{}",
                    sessionCount,
                    MAX_CONCURRENT_SESSIONS
            )
        }

        // Detect potential memory leaks in session mappings
        if (Math.abs(sessionCount - sessionToUserCount) > 10) {
            logger.warn(
                    "🚨 WebSocket mapping inconsistency: {} sessions vs {} mappings",
                    sessionCount,
                    sessionToUserCount
            )
        }
    }

    /**
     * Handles incoming text messages from WebSocket clients
     *
     * This method is the main entry point for all client messages. It:
     * 1. Parses the incoming JSON message
     * 2. Extracts the message type and payload
     * 3. Routes to the appropriate handler method
     * 4. Handles any parsing or processing errors gracefully
     *
     * All messages follow the WebSocketMessage format with a type field and a payload field
     * containing the specific data for that message type.
     *
     * @param session The WebSocket session that sent the message
     * @param message The text message containing JSON data
     */
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            // Log incoming message for debugging
            println("[WebSocket] Received message: ${message.payload}")

            // Parse JSON message into WebSocketMessage object
            val webSocketMessage =
                    objectMapper.readValue(message.payload, WebSocketMessage::class.java)
            println("[WebSocket] Message type: ${webSocketMessage.type}")

            // Route message to appropriate handler based on type
            when (webSocketMessage.type) {
                MessageType.JOIN -> handleJoin(session, webSocketMessage.payload)
                MessageType.VOTE -> handleVote(session, webSocketMessage.payload)
                MessageType.SHOW_ESTIMATES -> handleShowEstimates(session, webSocketMessage.payload)
                MessageType.HIDE_ESTIMATES -> handleHideEstimates(session, webSocketMessage.payload)
                MessageType.DELETE_ESTIMATES ->
                        handleDeleteEstimates(session, webSocketMessage.payload)
                else -> {
                    // Unknown message type - log but don't crash
                    println("[WebSocket] Unknown message type: ${webSocketMessage.type}")
                }
            }
        } catch (e: Exception) {
            // Log errors but don't terminate the connection
            println("[WebSocket] Error processing message: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Handles JOIN message - user requests to join a poker room
     *
     * This method processes a user's request to join a poker room:
     * 1. Parses the join request payload
     * 2. Creates user and adds them to room via RoomService
     * 3. Registers the session for future message delivery
     * 4. Sends back the assigned user ID to the client
     * 5. Broadcasts updated room state to all participants
     *
     * The JOIN flow is critical for establishing the user's presence in the room and enabling all
     * subsequent interactions.
     *
     * @param session WebSocket session of the joining user
     * @param payload Join request data containing name and room ID
     */
    private fun handleJoin(session: WebSocketSession, payload: Any) {
        // Check WebSocket session limits to prevent memory exhaustion
        if (sessions.size >= MAX_CONCURRENT_SESSIONS) {
            logger.error(
                    "❌ Cannot accept WebSocket connection: maximum session limit {} reached",
                    MAX_CONCURRENT_SESSIONS
            )
            session.close()
            return
        }

        // Convert payload to typed request object
        val joinRequest = objectMapper.convertValue(payload, JoinRoomRequest::class.java)

        try {
            // Create user and add to room via business logic (may throw exception if limits
            // exceeded)
            // Pass existing userId if provided (for multi-tab support)
            val user = roomService.joinRoom(joinRequest.name, joinRequest.roomId, joinRequest.userId)

            // Register session for message delivery
            sessions[user.id] = session
            sessionToUser[session.id] = user.id

            // Send user ID back to the client for future message identification
            sendMessage(
                    session,
                    WebSocketMessage(type = MessageType.JOIN, payload = mapOf("userId" to user.id))
            )

            // Broadcast room state to all users in the room
            broadcastRoomState(joinRequest.roomId)

            // Log session memory usage periodically (every 50 sessions)
            if (sessions.size % 50 == 0) {
                logSessionMemoryUsage()
            }
        } catch (e: IllegalStateException) {
            // Handle room/user limits exceeded
            logger.warn("❌ Cannot join room: {}", e.message)
            sendMessage(
                    session,
                    WebSocketMessage(
                            type = MessageType.ERROR,
                            payload =
                                    mapOf(
                                            "error" to
                                                    "Server capacity limit reached. Please try again later."
                                    )
                    )
            )
            session.close()
        }
    }

    /**
     * Handles VOTE message - user submits their estimate
     *
     * This method processes a user's story point estimate:
     * 1. Parses the vote request payload
     * 2. Records the vote via RoomService
     * 3. Broadcasts updated room state to show voting progress
     *
     * The vote is recorded immediately and the room state is updated to reflect the user's voting
     * status (though the actual estimate value remains hidden until revealed).
     *
     * @param session WebSocket session of the voting user
     * @param payload Vote request data containing user ID, room ID, and estimate
     */
    private fun handleVote(session: WebSocketSession, payload: Any) {
        // Convert payload to typed request object
        val voteRequest = objectMapper.convertValue(payload, VoteRequest::class.java)

        // Record the vote via business logic
        roomService.vote(voteRequest.userId, voteRequest.estimate)

        // Broadcast updated state to show voting progress
        broadcastRoomState(voteRequest.roomId)
    }

    /**
     * Handles SHOW_ESTIMATES message - reveals all estimates in the room
     *
     * This method processes a request to reveal all user estimates:
     * 1. Extracts room ID from payload
     * 2. Gets the user ID from the session for logging
     * 3. Updates room state to show estimates via RoomService
     * 4. Broadcasts updated state with visible estimates
     *
     * This is typically used after all users have voted to reveal the estimation results for
     * discussion.
     *
     * @param session WebSocket session of the requesting user
     * @param payload Request data containing room ID
     */
    private fun handleShowEstimates(session: WebSocketSession, payload: Any) {
        // Extract room ID from generic payload
        val roomId = (payload as Map<*, *>)["roomId"] as String

        // Get the user ID from the session for logging purposes
        val userId = sessionToUser[session.id]

        // Update room state to show estimates (with user ID for logging)
        roomService.showEstimates(roomId, userId)

        // Broadcast updated state with visible estimates
        broadcastRoomState(roomId)
    }

    /**
     * Handles HIDE_ESTIMATES message - hides all estimates in the room
     *
     * This method processes a request to hide all user estimates:
     * 1. Extracts room ID from payload
     * 2. Updates room state to hide estimates via RoomService
     * 3. Broadcasts updated state with hidden estimates
     *
     * This can be used to hide estimates again after they were revealed, perhaps for further
     * discussion before final decisions.
     *
     * @param session WebSocket session of the requesting user
     * @param payload Request data containing room ID
     */
    private fun handleHideEstimates(session: WebSocketSession, payload: Any) {
        // Extract room ID from generic payload
        val roomId = (payload as Map<*, *>)["roomId"] as String

        // Update room state to hide estimates
        roomService.hideEstimates(roomId)

        // Broadcast updated state with hidden estimates
        broadcastRoomState(roomId)
    }

    /**
     * Handles DELETE_ESTIMATES message - clears all estimates and resets voting
     *
     * This method processes a request to start a new estimation round:
     * 1. Extracts room ID from payload
     * 2. Gets the user ID from the session for logging
     * 3. Clears all estimates and resets voting status via RoomService
     * 4. Broadcasts updated state for the new round
     *
     * This is used to start fresh estimation for a new user story or to restart the current
     * estimation process.
     *
     * @param session WebSocket session of the requesting user
     * @param payload Request data containing room ID
     */
    private fun handleDeleteEstimates(session: WebSocketSession, payload: Any) {
        // Extract room ID from generic payload
        val roomId = (payload as Map<*, *>)["roomId"] as String

        // Get the user ID from the session for logging purposes
        val userId = sessionToUser[session.id]

        // Clear all estimates and reset voting (with user ID for logging)
        roomService.deleteEstimates(roomId, userId)

        // Broadcast fresh state for new round
        broadcastRoomState(roomId)
    }

    /**
     * Broadcasts room state to all participants
     *
     * This method sends the current room state to all users in the specified room:
     * 1. Gets current room state from RoomService
     * 2. Creates a ROOM_UPDATE message with the state
     * 3. Sends the message to all sessions for users in the room
     *
     * This is the core mechanism for keeping all clients synchronized with the latest room state
     * after any changes occur.
     *
     * @param roomId The ID of the room to broadcast state for
     */
    private fun broadcastRoomState(roomId: String) {
        // Get current room state from business logic
        val roomState = roomService.getRoomState(roomId) ?: return

        // Create update message with current state
        val message = WebSocketMessage(type = MessageType.ROOM_UPDATE, payload = roomState)

        // Send to all users in the room
        roomService.getUsersByRoom(roomId).forEach { user ->
            sessions[user.id]?.let { session -> sendMessage(session, message) }
        }
    }

    /**
     * Sends a message to a specific WebSocket session
     *
     * This utility method handles the actual message transmission:
     * 1. Checks if the session is still open
     * 2. Serializes the message to JSON
     * 3. Sends the message via the WebSocket
     *
     * This method includes safety checks to prevent errors when sending to closed or invalid
     * sessions.
     *
     * @param session The WebSocket session to send to
     * @param message The message object to send
     */
    private fun sendMessage(session: WebSocketSession, message: WebSocketMessage) {
        if (session.isOpen) {
            try {
                // Serialize message to JSON and send
                session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
            } catch (e: Exception) {
                println("[WebSocket] Error sending message: ${e.message}")
            }
        }
    }

    /**
     * Handles new WebSocket connection establishment
     *
     * This method is called when a new WebSocket connection is established:
     * 1. Logs the connection for monitoring and debugging
     * 2. Records connection details including remote address
     *
     * At this point, the connection is established but the user hasn't joined a room yet. The JOIN
     * message will complete the user setup.
     *
     * @param session The newly established WebSocket session
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info(
                "🔌 New WebSocket connection established: {} from {}",
                session.id,
                session.remoteAddress
        )
        super.afterConnectionEstablished(session)
    }

    /**
     * Handles WebSocket connection closure
     *
     * This method is called when a WebSocket connection is closed:
     * 1. Logs the disconnection with reason and status
     * 2. Finds the user associated with the session
     * 3. Removes the user from their room (triggers user left logging in RoomService)
     * 4. Cleans up session tracking maps
     * 5. Broadcasts updated room state to remaining users
     *
     * This cleanup ensures that disconnected users are properly removed from rooms and other
     * participants see the updated user list.
     *
     * MEMORY LEAK FIX: Always clean up session maps even if user lookup fails
     *
     * @param session The WebSocket session that was closed
     * @param status The close status indicating why the connection closed
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info(
                "🔌 WebSocket connection closed: {} - Status: {} ({})",
                session.id,
                status.code,
                status.reason ?: "No reason provided"
        )

        // Find the user associated with this session
        val userId = sessionToUser[session.id]
        var roomId: String? = null

        if (userId != null) {
            // Try to get user and clean up room membership
            roomService.getUserById(userId)?.let { user ->
                roomId = user.roomId
                // Remove user from room and clean up session tracking
                // The leaveRoom call will trigger the "user left" logging in RoomService
                roomService.leaveRoom(userId)
            }

            // CRITICAL FIX: Always clean up WebSocket session maps even if user lookup fails
            // This prevents memory leaks when RoomService cleanup happens before WebSocket cleanup
            sessions.remove(userId)
            sessionToUser.remove(session.id)

            // Broadcast updated room state if we know the room
            roomId?.let { broadcastRoomState(it) }

            // Perform maintenance cleanup to ensure data consistency and prevent memory leaks
            // This replaces the scheduled cleanup since we no longer use heartbeat service
            roomService.performMaintenanceCleanup()

            logger.debug(
                    "🧹 Cleaned up WebSocket session for user {} (session {})",
                    userId,
                    session.id
            )
        } else {
            // Session was never properly registered (connection closed before JOIN message)
            logger.debug("🔌 WebSocket session {} closed without associated user", session.id)
        }

        super.afterConnectionClosed(session, status)
    }
}
