/**
 * RoomService.kt - Core Business Logic Service for Poker Rooms
 *
 * This service class contains the core business logic for managing poker rooms, users, and
 * estimation sessions. It serves as the main data access layer and business rules engine for the
 * Scrum Poker application.
 *
 * Key Responsibilities:
 * 1. Room lifecycle management (creation, cleanup)
 * 2. User management (join, leave, session tracking)
 * 3. Voting logic (submit estimates, validate users)
 * 4. Session state management (show/hide/delete estimates)
 * 5. Data transformation for client updates
 *
 * Data Storage: The service uses in-memory ConcurrentHashMap collections for data storage, making
 * it suitable for development and small-scale deployments. For production at scale, this could be
 * replaced with database persistence.
 *
 * Thread Safety: All operations are thread-safe using concurrent collections and atomic operations,
 * ensuring data consistency in the multi-user WebSocket environment.
 *
 * Business Rules:
 * - Users are uniquely identified by UUID
 * - Each user belongs to exactly one room
 * - Rooms are created automatically when first user joins
 * - Empty rooms are automatically cleaned up
 * - Estimates are private until explicitly revealed
 */
package com.scrumpoker.service

import com.scrumpoker.model.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * RoomService - Core business logic service for poker room management
 *
 * This service manages all aspects of poker rooms and user sessions, providing the central business
 * logic for the estimation process. It maintains in-memory state for all active rooms and users.
 *
 * The service is designed to be stateless except for the data it manages, making it suitable for
 * dependency injection and testing. All methods are designed to be idempotent where possible.
 *
 * Concurrency Design:
 * - Uses ConcurrentHashMap for thread-safe collections
 * - Employs atomic operations where possible
 * - Designed for high-concurrency WebSocket environment
 */
@Service
class RoomService {

    /**
     * Logger for tracking user actions and system events
     *
     * Used to log all user interactions for monitoring, debugging, and audit purposes. Logs include
     * user names, actions performed, and relevant context.
     */
    private val logger = LoggerFactory.getLogger(RoomService::class.java)

    /**
     * In-memory storage for all active poker rooms
     *
     * Maps room ID to Room object containing all room state including participants, estimates, and
     * session settings. Uses ConcurrentHashMap for thread safety.
     */
    private val rooms = ConcurrentHashMap<String, Room>()

    /**
     * In-memory storage for user session tracking
     *
     * Maps user ID to User object for quick lookup during operations. Used for validating users and
     * accessing user information efficiently.
     */
    private val userSessions = ConcurrentHashMap<String, User>()

    /**
     * Adds a new user to a poker room
     *
     * This method handles the complete process of user registration:
     * 1. Creates a new User with generated UUID
     * 2. Gets or creates the specified room
     * 3. Adds the user to the room's participant list
     * 4. Registers the user in the session tracking map
     *
     * The room auto-creation feature simplifies the user experience by eliminating the need for
     * explicit room creation. Users can join any room by ID, and it will be created automatically
     * if it doesn't exist.
     *
     * @param name The display name chosen by the user
     * @param roomId The ID of the room to join
     * @return The created User object with generated ID
     */
    fun joinRoom(name: String, roomId: String): User {
        // Create new user with generated UUID and provided information
        val user =
                User(
                        id = UUID.randomUUID().toString(), // Generate unique ID for the user
                        name = name, // User's chosen display name
                        roomId = roomId // Room they're joining
                )

        // Get existing room or create new one atomically
        // computeIfAbsent ensures thread-safe room creation
        val room = rooms.computeIfAbsent(roomId) { Room(it) }

        // Add user to room's participant list
        room.users.add(user)

        // Register user in session tracking for quick lookup
        userSessions[user.id] = user

        // Log the user joining action
        logger.info(
                "📋 New user joined with name '{}' in room '{}' (User ID: {})",
                name,
                roomId,
                user.id
        )

        return user
    }

    /**
     * Removes a user from their poker room
     *
     * This method handles the complete process of removing a user from the system:
     * 1. Finds the user by ID in the session tracking
     * 2. Removes them from their room's participant list
     * 3. Removes them from session tracking
     * 4. Cleans up empty rooms automatically
     *
     * The automatic room cleanup prevents memory leaks and ensures that unused rooms don't persist
     * in memory indefinitely.
     *
     * @param userId The unique ID of the user to remove
     */
    fun leaveRoom(userId: String) {
        // Find the user in our session tracking
        userSessions[userId]?.let { user ->
            // Log the user leaving action
            logger.info("👋 User '{}' left room '{}' (User ID: {})", user.name, user.roomId, userId)

            // Remove user from their room's participant list
            rooms[user.roomId]?.users?.removeIf { it.id == userId }

            // Remove user from session tracking
            userSessions.remove(userId)

            // Clean up empty rooms to prevent memory leaks
            if (rooms[user.roomId]?.users?.isEmpty() == true) {
                logger.info("🧹 Cleaning up empty room '{}'", user.roomId)
                rooms.remove(user.roomId)
            }
        }
    }

    /**
     * Records a user's estimate vote
     *
     * This method handles the voting process for story point estimation:
     * 1. Finds the user by ID
     * 2. Records their estimate value
     * 3. Marks them as having voted
     *
     * The method is idempotent - users can change their vote by voting again. The estimate remains
     * private until the session organizer reveals all estimates.
     *
     * Business Rules:
     * - Users can vote multiple times (last vote counts)
     * - Votes are private until revealed
     * - Any string value is accepted as an estimate
     *
     * @param userId The ID of the user submitting the vote
     * @param estimate The estimate value (number or special symbol)
     */
    fun vote(userId: String, estimate: String) {
        // Find user and update their voting status
        userSessions[userId]?.let { user ->
            user.estimate = estimate // Store the estimate value
            user.hasVoted = true // Mark as having voted

            // Log the voting action
            logger.info(
                    "🎯 User '{}' selected card '{}' in room '{}' (User ID: {})",
                    user.name,
                    estimate,
                    user.roomId,
                    userId
            )
        }
    }

    /**
     * Reveals all estimates in a room
     *
     * This method makes all user estimates visible to all participants in the room. It's typically
     * called after all users have voted to reveal the results of the estimation round.
     *
     * @param roomId The ID of the room to reveal estimates for
     * @param triggerUserId The ID of the user who triggered the show action (for logging)
     */
    fun showEstimates(roomId: String, triggerUserId: String? = null) {
        rooms[roomId]?.showEstimates = true

        // Log the show estimates action
        val triggerUser = triggerUserId?.let { userSessions[it] }
        if (triggerUser != null) {
            logger.info(
                    "👁️ User '{}' pressed Show estimates in room '{}' (User ID: {})",
                    triggerUser.name,
                    roomId,
                    triggerUserId
            )
        } else {
            logger.info("👁️ Show estimates triggered in room '{}'", roomId)
        }
    }

    /**
     * Hides all estimates in a room
     *
     * This method hides all user estimates from participants while preserving the votes. This can
     * be useful if estimates need to be hidden again for discussion before revealing final results.
     *
     * @param roomId The ID of the room to hide estimates for
     */
    fun hideEstimates(roomId: String) {
        rooms[roomId]?.showEstimates = false
    }

    /**
     * Clears all estimates and resets voting
     *
     * This method resets the room to start a new estimation round:
     * 1. Clears all user estimates
     * 2. Resets voting status for all users
     * 3. Hides estimates (resets visibility)
     *
     * This is typically used to start a new round of estimation for a different user story or when
     * redoing an estimation.
     *
     * @param roomId The ID of the room to reset
     * @param triggerUserId The ID of the user who triggered the delete action (for logging)
     */
    fun deleteEstimates(roomId: String, triggerUserId: String? = null) {
        rooms[roomId]?.let { room ->
            // Reset all users' voting status
            room.users.forEach { user ->
                user.estimate = null // Clear their estimate
                user.hasVoted = false // Mark as not voted
            }
            // Hide estimates for new round
            room.showEstimates = false

            // Log the delete estimates action
            val triggerUser = triggerUserId?.let { userSessions[it] }
            if (triggerUser != null) {
                logger.info(
                        "🗑️ User '{}' pressed Delete Estimations in room '{}' (User ID: {})",
                        triggerUser.name,
                        roomId,
                        triggerUserId
                )
            } else {
                logger.info("🗑️ Delete Estimations triggered in room '{}'", roomId)
            }
        }
    }

    /**
     * Gets the current state of a room for client updates
     *
     * This method creates a RoomStateUpdate object that contains all the information needed to
     * update clients about the current room state. It transforms internal User objects into
     * UserState objects that respect estimate visibility rules.
     *
     * Visibility Rules:
     * - User names are always visible
     * - Voting status (hasVoted) is always visible
     * - Estimates are only included when showEstimates is true
     *
     * This separation ensures that clients only receive information they should see based on the
     * current session state.
     *
     * @param roomId The ID of the room to get state for
     * @return RoomStateUpdate with current room state, or null if room doesn't exist
     */
    fun getRoomState(roomId: String): RoomStateUpdate? {
        return rooms[roomId]?.let { room ->
            RoomStateUpdate(
                    roomId = roomId,
                    users =
                            room.users.map { user ->
                                UserState(
                                        id = user.id,
                                        name = user.name,
                                        hasVoted = user.hasVoted,
                                        estimate = if (room.showEstimates) user.estimate else null
                                )
                            },
                    showEstimates = room.showEstimates
            )
        }
    }

    /**
     * Finds a user by their unique ID
     *
     * This method provides quick lookup of users by ID for session management and validation
     * purposes.
     *
     * @param userId The unique ID of the user to find
     * @return The User object if found, null otherwise
     */
    fun getUserById(userId: String): User? {
        return userSessions[userId]
    }

    /**
     * Gets all users in a specific room
     *
     * This method returns a list of all users currently participating in the specified room. It's
     * useful for operations that need to affect all users in a room, such as broadcasting updates.
     *
     * @param roomId The ID of the room to get users for
     * @return List of User objects in the room, empty list if room doesn't exist
     */
    fun getUsersByRoom(roomId: String): List<User> {
        return rooms[roomId]?.users?.toList() ?: emptyList()
    }

    /**
     * Gets all active rooms for monitoring
     *
     * This method returns a snapshot of all currently active rooms in the system. It's primarily
     * used by monitoring services to track application usage and for administrative purposes.
     *
     * The returned map is a copy to prevent external modification of the internal room state.
     *
     * @return Map of room ID to Room objects for all active rooms
     */
    fun getAllRooms(): Map<String, Room> {
        return rooms.toMap() // Return immutable copy
    }
}
