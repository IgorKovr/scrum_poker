/**
 * Models.kt - Data Models for Scrum Poker Backend
 * 
 * This file contains all the data classes, enums, and model definitions used
 * throughout the Scrum Poker backend application. These models define the
 * structure of data as it flows through the system and is stored in memory.
 * 
 * Key Categories:
 * 1. Core Domain Models (User, Room)
 * 2. Request/Response Models for WebSocket communication
 * 3. State Transfer Objects for client updates
 * 4. Message enums and wrapper classes
 * 
 * The models are designed to be:
 * - Immutable where possible (using val)
 * - Mutable only where business logic requires it (using var)
 * - JSON-serializable for WebSocket communication
 * - Type-safe with clear property definitions
 * 
 * These models ensure consistency between the backend business logic
 * and the frontend client communication.
 */

package com.scrumpoker.model

/**
 * User Data Class - Represents a participant in a poker room
 * 
 * This class models a user as they exist within the backend system.
 * It contains all the information needed to track a user's participation
 * in a poker session, including their voting status and estimate.
 * 
 * The class uses a mix of immutable and mutable properties:
 * - Immutable: id, name, roomId (these don't change after creation)
 * - Mutable: estimate, hasVoted (these change during the estimation process)
 * 
 * @property id Unique identifier for the user (UUID)
 * @property name Display name chosen by the user
 * @property roomId Identifier of the room the user belongs to
 * @property estimate The user's story point estimate (null if not voted)
 * @property hasVoted Boolean flag indicating if user has submitted an estimate
 */
data class User(
    /** Unique identifier for the user, generated when joining a room */
    val id: String,
    
    /** Display name provided by the user */
    val name: String,
    
    /** ID of the poker room this user belongs to */
    val roomId: String,
    
    /** The user's current estimate (null if not voted or reset) */
    var estimate: String? = null,
    
    /** Whether the user has submitted an estimate for the current round */
    var hasVoted: Boolean = false
)

/**
 * Room Data Class - Represents a poker estimation session
 * 
 * This class models a poker room where users gather to estimate story points.
 * It maintains the list of participants and the current session state.
 * 
 * The room uses a mutable list of users to allow dynamic addition/removal
 * of participants during the session. The showEstimates flag controls
 * whether estimates are visible to all participants.
 * 
 * @property id Unique identifier for the room
 * @property users Mutable list of users currently in the room
 * @property showEstimates Whether estimates are currently revealed to all users
 */
data class Room(
    /** Unique identifier for the poker room */
    val id: String,
    
    /** List of users currently participating in this room */
    val users: MutableList<User> = mutableListOf(),
    
    /** Whether estimates are currently visible to all participants */
    var showEstimates: Boolean = false
)

/**
 * JoinRoomRequest Data Class - Request to join a poker room
 * 
 * This class represents the data sent by a client when requesting to join
 * a poker room. It contains the minimum information needed to create a
 * user and add them to the specified room.
 * 
 * @property name The display name the user wants to use
 * @property roomId The ID of the room the user wants to join
 */
data class JoinRoomRequest(
    /** Display name chosen by the user */
    val name: String,
    
    /** ID of the room to join */
    val roomId: String
)

/**
 * VoteRequest Data Class - Request to submit an estimate
 * 
 * This class represents the data sent by a client when submitting their
 * story point estimate. It includes the user identification and their
 * chosen estimate value.
 * 
 * @property userId The ID of the user submitting the vote
 * @property roomId The ID of the room where the vote is being cast
 * @property estimate The story point estimate value (number or special symbol)
 */
data class VoteRequest(
    /** ID of the user submitting the estimate */
    val userId: String,
    
    /** ID of the room where the vote is being cast */
    val roomId: String,
    
    /** The estimate value (e.g., "5", "?", "☕") */
    val estimate: String
)

/**
 * RoomStateUpdate Data Class - Complete room state for client updates
 * 
 * This class represents the complete state of a poker room as sent to clients.
 * It's used when broadcasting room updates to all participants after any
 * state change (user joins, votes, estimates shown/hidden, etc.).
 * 
 * The class transforms internal User objects into UserState objects that
 * contain only the information that should be visible to clients, respecting
 * the estimate visibility rules.
 * 
 * @property roomId The ID of the room this state represents
 * @property users List of user states for display on the client
 * @property showEstimates Whether estimates are currently visible
 */
data class RoomStateUpdate(
    /** ID of the room this state update represents */
    val roomId: String,
    
    /** List of user states for client display */
    val users: List<UserState>,
    
    /** Whether estimates are currently revealed to all participants */
    val showEstimates: Boolean
)

/**
 * UserState Data Class - User information for client display
 * 
 * This class represents user information as it should be displayed on the client.
 * It's a filtered version of the User class that respects visibility rules
 * for estimates (estimates are only included when showEstimates is true).
 * 
 * This separation ensures that the backend can maintain full user state
 * while controlling what information is sent to clients based on the
 * current session state.
 * 
 * @property id The user's unique identifier
 * @property name The user's display name
 * @property estimate The user's estimate (null if hidden or not voted)
 * @property hasVoted Whether the user has submitted an estimate
 */
data class UserState(
    /** Unique identifier for the user */
    val id: String,
    
    /** Display name of the user */
    val name: String,
    
    /** The user's estimate (only included when estimates are revealed) */
    val estimate: String?,
    
    /** Whether the user has submitted an estimate */
    val hasVoted: Boolean
)

/**
 * MessageType Enum - WebSocket Message Types
 * 
 * This enum defines all the different types of messages that can be exchanged
 * between the backend and frontend via WebSocket connections. Each message type
 * corresponds to a specific action or event in the poker session workflow.
 * 
 * Message Types:
 * - JOIN: Client requests to join a room
 * - VOTE: Client submits an estimate
 * - SHOW_ESTIMATES: Request to reveal all estimates
 * - HIDE_ESTIMATES: Request to hide all estimates  
 * - DELETE_ESTIMATES: Request to clear all estimates and start fresh
 * - ROOM_UPDATE: Server sends updated room state to clients
 * - USER_LEFT: Notification that a user has left the room
 * 
 * The enum values match exactly with the frontend MessageType enum to ensure
 * consistent communication protocol between client and server.
 */
enum class MessageType {
    /** Client request to join a poker room */
    JOIN,
    
    /** Client submits their story point estimate */
    VOTE,
    
    /** Request to reveal all user estimates */
    SHOW_ESTIMATES,
    
    /** Request to hide all user estimates */
    HIDE_ESTIMATES,
    
    /** Request to delete all estimates and reset voting */
    DELETE_ESTIMATES,
    
    /** Server broadcast of updated room state */
    ROOM_UPDATE,
    
    /** Notification that a user has left the room */
    USER_LEFT
}

/**
 * WebSocketMessage Data Class - Wrapper for WebSocket communication
 * 
 * This class provides a consistent structure for all messages sent over
 * WebSocket connections. It acts as an envelope that contains the message
 * type and the actual data payload.
 * 
 * The payload can be any type of data (request objects, room states, etc.)
 * and is serialized/deserialized using Jackson JSON processing. The type
 * field allows the receiver to determine how to interpret the payload.
 * 
 * @property type The message type enum indicating the kind of message
 * @property payload The actual message data (structure varies by type)
 */
data class WebSocketMessage(
    /** The type of message being sent */
    val type: MessageType,
    
    /** The message payload (structure depends on message type) */
    val payload: Any
) 