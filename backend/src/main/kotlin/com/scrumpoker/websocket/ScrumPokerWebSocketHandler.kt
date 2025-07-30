/**
 * ScrumPokerWebSocketHandler.kt - WebSocket Message Handler for Real-time Communication
 * 
 * This class handles all WebSocket communication between the frontend clients and
 * the backend server. It processes incoming messages, manages client sessions,
 * and broadcasts updates to all connected clients in real-time.
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
 * The handler ensures that all participants in a room receive real-time updates
 * when any state changes occur, providing a synchronized view of the estimation
 * session across all clients.
 * 
 * Thread Safety:
 * This handler is designed to be thread-safe, with concurrent access managed
 * through the underlying Spring WebSocket framework and concurrent collections.
 */

package com.scrumpoker.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.scrumpoker.model.*
import com.scrumpoker.service.RoomService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * ScrumPokerWebSocketHandler - Main WebSocket message processor
 * 
 * This class extends Spring's TextWebSocketHandler to provide custom message
 * processing for the Scrum Poker application. It maintains session state and
 * coordinates between WebSocket connections and business logic.
 * 
 * Dependencies:
 * - RoomService: For all business logic operations
 * - ObjectMapper: For JSON serialization/deserialization
 * 
 * Session Management:
 * The handler maintains two maps for efficient session tracking:
 * - sessions: Maps user ID to WebSocket session for message delivery
 * - sessionToUser: Maps session ID to user ID for cleanup operations
 * 
 * @param roomService Service for poker room business logic
 * @param objectMapper Jackson ObjectMapper for JSON processing
 */
@Component
class ScrumPokerWebSocketHandler(
    /** RoomService dependency for business logic operations */
    private val roomService: RoomService,
    
    /** ObjectMapper for JSON serialization and deserialization */
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {
    
    /** Map of user ID to WebSocket session for message broadcasting */
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    
    /** Map of session ID to user ID for cleanup when connections close */
    private val sessionToUser = ConcurrentHashMap<String, String>()
    
    /**
     * Handles incoming text messages from WebSocket clients
     * 
     * This method is the main entry point for all client messages. It:
     * 1. Parses the incoming JSON message
     * 2. Extracts the message type and payload
     * 3. Routes to the appropriate handler method
     * 4. Handles any parsing or processing errors gracefully
     * 
     * All messages follow the WebSocketMessage format with a type field
     * and a payload field containing the specific data for that message type.
     * 
     * @param session The WebSocket session that sent the message
     * @param message The text message containing JSON data
     */
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            // Log incoming message for debugging
            println("[WebSocket] Received message: ${message.payload}")
            
            // Parse JSON message into WebSocketMessage object
            val webSocketMessage = objectMapper.readValue(message.payload, WebSocketMessage::class.java)
            println("[WebSocket] Message type: ${webSocketMessage.type}")
            
            // Route message to appropriate handler based on type
            when (webSocketMessage.type) {
                MessageType.JOIN -> handleJoin(session, webSocketMessage.payload)
                MessageType.VOTE -> handleVote(session, webSocketMessage.payload)
                MessageType.SHOW_ESTIMATES -> handleShowEstimates(session, webSocketMessage.payload)
                MessageType.HIDE_ESTIMATES -> handleHideEstimates(session, webSocketMessage.payload)
                MessageType.DELETE_ESTIMATES -> handleDeleteEstimates(session, webSocketMessage.payload)
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
     * The JOIN flow is critical for establishing the user's presence in
     * the room and enabling all subsequent interactions.
     * 
     * @param session WebSocket session of the joining user
     * @param payload Join request data containing name and room ID
     */
    private fun handleJoin(session: WebSocketSession, payload: Any) {
        // Convert payload to typed request object
        val joinRequest = objectMapper.convertValue(payload, JoinRoomRequest::class.java)
        
        // Create user and add to room via business logic
        val user = roomService.joinRoom(joinRequest.name, joinRequest.roomId)
        
        // Register session for message delivery
        sessions[user.id] = session
        sessionToUser[session.id] = user.id
        
        // Send user ID back to the client for future message identification
        sendMessage(session, WebSocketMessage(
            type = MessageType.JOIN,
            payload = mapOf("userId" to user.id)
        ))
        
        // Broadcast room state to all users in the room
        broadcastRoomState(joinRequest.roomId)
    }
    
    /**
     * Handles VOTE message - user submits their estimate
     * 
     * This method processes a user's story point estimate:
     * 1. Parses the vote request payload
     * 2. Records the vote via RoomService
     * 3. Broadcasts updated room state to show voting progress
     * 
     * The vote is recorded immediately and the room state is updated
     * to reflect the user's voting status (though the actual estimate
     * value remains hidden until revealed).
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
     * 2. Updates room state to show estimates via RoomService
     * 3. Broadcasts updated state with visible estimates
     * 
     * This is typically used after all users have voted to reveal
     * the estimation results for discussion.
     * 
     * @param session WebSocket session of the requesting user
     * @param payload Request data containing room ID
     */
    private fun handleShowEstimates(session: WebSocketSession, payload: Any) {
        // Extract room ID from generic payload
        val roomId = (payload as Map<*, *>)["roomId"] as String
        
        // Update room state to show estimates
        roomService.showEstimates(roomId)
        
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
     * This can be used to hide estimates again after they were revealed,
     * perhaps for further discussion before final decisions.
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
     * 2. Clears all estimates and resets voting status via RoomService
     * 3. Broadcasts updated state for the new round
     * 
     * This is used to start fresh estimation for a new user story
     * or to restart the current estimation process.
     * 
     * @param session WebSocket session of the requesting user
     * @param payload Request data containing room ID
     */
    private fun handleDeleteEstimates(session: WebSocketSession, payload: Any) {
        // Extract room ID from generic payload
        val roomId = (payload as Map<*, *>)["roomId"] as String
        
        // Clear all estimates and reset voting
        roomService.deleteEstimates(roomId)
        
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
     * This is the core mechanism for keeping all clients synchronized
     * with the latest room state after any changes occur.
     * 
     * @param roomId The ID of the room to broadcast state for
     */
    private fun broadcastRoomState(roomId: String) {
        // Get current room state from business logic
        val roomState = roomService.getRoomState(roomId) ?: return
        
        // Create update message with current state
        val message = WebSocketMessage(
            type = MessageType.ROOM_UPDATE,
            payload = roomState
        )
        
        // Send to all users in the room
        roomService.getUsersByRoom(roomId).forEach { user ->
            sessions[user.id]?.let { session ->
                sendMessage(session, message)
            }
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
     * This method includes safety checks to prevent errors when
     * sending to closed or invalid sessions.
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
     * 1. Logs the connection for debugging
     * 2. Records connection details
     * 
     * At this point, the connection is established but the user hasn't
     * joined a room yet. The JOIN message will complete the user setup.
     * 
     * @param session The newly established WebSocket session
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("[WebSocket] New connection established: ${session.id}")
        println("[WebSocket] Remote address: ${session.remoteAddress}")
        super.afterConnectionEstablished(session)
    }
    
    /**
     * Handles WebSocket connection closure
     * 
     * This method is called when a WebSocket connection is closed:
     * 1. Logs the disconnection with reason
     * 2. Finds the user associated with the session
     * 3. Removes the user from their room
     * 4. Cleans up session tracking maps
     * 5. Broadcasts updated room state to remaining users
     * 
     * This cleanup ensures that disconnected users are properly removed
     * from rooms and other participants see the updated user list.
     * 
     * @param session The WebSocket session that was closed
     * @param status The close status indicating why the connection closed
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        println("[WebSocket] Connection closed: ${session.id}, Status: ${status.code} - ${status.reason}")
        
        // Find the user associated with this session
        sessionToUser[session.id]?.let { userId ->
            roomService.getUserById(userId)?.let { user ->
                val roomId = user.roomId
                
                // Remove user from room and clean up session tracking
                roomService.leaveRoom(userId)
                sessions.remove(userId)
                sessionToUser.remove(session.id)
                
                // Broadcast updated room state to remaining users
                broadcastRoomState(roomId)
            }
        }
    }
} 