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
import org.springframework.scheduling.annotation.Scheduled
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
 *
 * Memory Management:
 * - Automatic cleanup of empty rooms to prevent memory leaks
 * - Size monitoring and logging for memory usage tracking
 * - Bounds checking to prevent unbounded growth
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

    // Memory management constants
    private val MAX_ROOMS = 1000 // Maximum number of concurrent rooms
    private val MAX_USERS_PER_ROOM = 50 // Maximum users per room
    private val MAX_TOTAL_USERS = 5000 // Maximum total users across all rooms

    // Grace period for disconnected users (5 minutes in milliseconds)
    private val DISCONNECT_GRACE_PERIOD_MS = 5 * 60 * 1000L

    /** Logs current memory usage for monitoring */
    private fun logMemoryUsage() {
        val totalUsers = userSessions.size
        val totalRooms = rooms.size
        val averageUsersPerRoom = if (totalRooms > 0) totalUsers.toDouble() / totalRooms else 0.0

        logger.info(
                "📊 Memory Usage: {} rooms, {} users, {:.1f} avg users/room",
                totalRooms,
                totalUsers,
                averageUsersPerRoom
        )

        // Log warning if approaching limits
        if (totalRooms > MAX_ROOMS * 0.8) {
            logger.warn("⚠️ Room count approaching limit: {}/{}", totalRooms, MAX_ROOMS)
        }
        if (totalUsers > MAX_TOTAL_USERS * 0.8) {
            logger.warn("⚠️ User count approaching limit: {}/{}", totalUsers, MAX_TOTAL_USERS)
        }
    }

    /**
     * Scheduled task to clean up users who exceeded grace period
     *
     * Runs every minute to check for users who have been disconnected for longer than the grace
     * period and permanently removes them from the system.
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    fun cleanupDisconnectedUsers() {
        val currentTime = System.currentTimeMillis()
        var cleanedUsers = 0

        // Find users who have been disconnected longer than grace period
        val expiredUsers =
                userSessions.values.filter { user ->
                    user.disconnectedAt != null &&
                            (currentTime - user.disconnectedAt!!) > DISCONNECT_GRACE_PERIOD_MS
                }

        // Permanently remove expired users
        expiredUsers.forEach { user ->
            permanentlyRemoveUser(user.id)
            cleanedUsers++
        }

        if (cleanedUsers > 0) {
            logger.info("🧹 Grace period cleanup: removed {} expired users", cleanedUsers)
        }
    }

    /** Performs cleanup of stale data to prevent memory leaks */
    fun performMaintenanceCleanup() {
        var cleanedRooms = 0
        var cleanedUsers = 0

        // Find rooms with no users (potential orphaned rooms)
        val emptyRoomIds = rooms.values.filter { it.users.isEmpty() }.map { it.id }
        emptyRoomIds.forEach { roomId ->
            rooms.remove(roomId)
            cleanedRooms++
            logger.info("🧹 Maintenance cleanup: removed empty room '{}'", roomId)
        }

        // Find users not in any room (potential orphaned users)
        val activeRoomIds = rooms.keys
        val orphanedUsers =
                userSessions.values.filter { user -> !activeRoomIds.contains(user.roomId) }
        orphanedUsers.forEach { user ->
            userSessions.remove(user.id)
            cleanedUsers++
            logger.info(
                    "🧹 Maintenance cleanup: removed orphaned user '{}' from room '{}'",
                    user.name,
                    user.roomId
            )
        }

        // Validate consistency between rooms and userSessions
        validateDataConsistency()

        if (cleanedRooms > 0 || cleanedUsers > 0) {
            logger.info(
                    "🧹 Maintenance cleanup completed: {} rooms, {} users removed",
                    cleanedRooms,
                    cleanedUsers
            )
        }

        logMemoryUsage()
    }

    /** Validates consistency between rooms and userSessions to detect memory leaks */
    private fun validateDataConsistency() {
        val usersInRooms = rooms.values.flatMap { it.users }.map { it.id }.toSet()
        val usersInSessions = userSessions.keys.toSet()

        val orphanedInSessions = usersInSessions - usersInRooms
        val orphanedInRooms = usersInRooms - usersInSessions

        if (orphanedInSessions.isNotEmpty()) {
            logger.warn(
                    "🚨 Data inconsistency: {} users in sessions but not in rooms: {}",
                    orphanedInSessions.size,
                    orphanedInSessions.take(5)
            )
        }

        if (orphanedInRooms.isNotEmpty()) {
            logger.warn(
                    "🚨 Data inconsistency: {} users in rooms but not in sessions: {}",
                    orphanedInRooms.size,
                    orphanedInRooms.take(5)
            )
        }
    }

    /**
     * Adds a new user to a poker room or reconnects a disconnected user
     *
     * This method handles the complete process of user registration with reconnection support:
     * 1. If userId provided, checks if user exists and reconnects (multi-tab support)
     * 2. Otherwise, checks if a disconnected user with same name exists (within grace period)
     * 3. If found, reconnects the user (clears disconnectedAt, preserves state)
     * 4. Otherwise, creates a new User with generated UUID
     * 5. Gets or creates the specified room
     * 6. Adds the user to the room's participant list
     * 7. Registers the user in the session tracking map
     *
     * Multi-Tab Support: If a userId is provided (from localStorage), the method will reconnect to
     * that existing user instead of creating a duplicate. This allows multiple browser tabs to share
     * the same user identity.
     *
     * Reconnection Feature: If a user disconnects (tab close, network loss) and rejoins within the
     * grace period, they will reconnect to their existing session, preserving their vote and user
     * ID. This provides a seamless experience for temporary disconnections.
     *
     * @param name The display name chosen by the user
     * @param roomId The ID of the room to join
     * @param existingUserId Optional user ID from a previous session (for multi-tab support)
     * @return The created or reconnected User object with ID
     * @throws IllegalStateException if system limits are exceeded
     */
    fun joinRoom(name: String, roomId: String, existingUserId: String? = null): User {
        // Get existing room or create new one atomically
        val room = rooms.computeIfAbsent(roomId) { Room(it) }

        // PRIORITY 1: Check if connecting with an existing userId (multi-tab support)
        if (existingUserId != null) {
            val existingUser = room.users.find { it.id == existingUserId }
            if (existingUser != null && existingUser.name == name) {
                // Reconnect to existing user (multi-tab scenario)
                existingUser.disconnectedAt = null // Clear disconnection if any

                logger.info(
                        "🔄 User '{}' reconnected to room '{}' from another tab (User ID: {})",
                        name,
                        roomId,
                        existingUserId
                )

                return existingUser
            }
        }

        // PRIORITY 2: Check if there's a disconnected user with the same name in this room
        val disconnectedUser = room.users.find { it.name == name && it.disconnectedAt != null }

        if (disconnectedUser != null) {
            // User is reconnecting within grace period - restore their session
            disconnectedUser.disconnectedAt = null

            logger.info(
                    "🔄 User '{}' reconnected to room '{}' (User ID: {}) - Vote preserved",
                    name,
                    roomId,
                    disconnectedUser.id
            )

            return disconnectedUser
        }

        // Check system limits to prevent memory exhaustion (only for new users)
        if (rooms.size >= MAX_ROOMS) {
            logger.error(
                    "❌ Cannot create room '{}': maximum room limit {} reached",
                    roomId,
                    MAX_ROOMS
            )
            throw IllegalStateException("Maximum number of rooms ($MAX_ROOMS) reached")
        }

        if (userSessions.size >= MAX_TOTAL_USERS) {
            logger.error(
                    "❌ Cannot add user '{}': maximum user limit {} reached",
                    name,
                    MAX_TOTAL_USERS
            )
            throw IllegalStateException("Maximum number of users ($MAX_TOTAL_USERS) reached")
        }

        // Check room capacity (count only connected users)
        val connectedUsersCount = room.users.count { it.disconnectedAt == null }
        if (connectedUsersCount >= MAX_USERS_PER_ROOM) {
            logger.error(
                    "❌ Cannot add user '{}' to room '{}': maximum room capacity {} reached",
                    name,
                    roomId,
                    MAX_USERS_PER_ROOM
            )
            throw IllegalStateException("Room capacity limit ($MAX_USERS_PER_ROOM) reached")
        }

        // Create new user with generated UUID and provided information
        val user =
                User(
                        id = UUID.randomUUID().toString(), // Generate unique ID for the user
                        name = name, // User's chosen display name
                        roomId = roomId // Room they're joining
                )

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

        // Log memory usage periodically (every 10 users)
        if (userSessions.size % 10 == 0) {
            logMemoryUsage()
        }

        return user
    }

    /**
     * Marks a user as disconnected with grace period
     *
     * This method handles user disconnections with a grace period approach:
     * 1. Finds the user by ID in the session tracking
     * 2. Marks them as disconnected with current timestamp
     * 3. Keeps them in room for grace period (5 minutes)
     * 4. User can reconnect within grace period without losing state
     *
     * This prevents users from being kicked out when switching tabs, minimizing browser, or brief
     * network interruptions. A scheduled task will clean up users who remain disconnected beyond
     * the grace period.
     *
     * @param userId The unique ID of the user who disconnected
     */
    fun leaveRoom(userId: String) {
        // Find the user in our session tracking
        userSessions[userId]?.let { user ->
            // Mark user as disconnected with current timestamp
            user.disconnectedAt = System.currentTimeMillis()

            // Log the disconnection with grace period info
            logger.info(
                    "⏸️ User '{}' disconnected from room '{}' (User ID: {}) - Grace period: {} minutes",
                    user.name,
                    user.roomId,
                    userId,
                    DISCONNECT_GRACE_PERIOD_MS / 60000
            )
        }
                ?: run {
                    // User not found - this could indicate a memory leak or race condition
                    logger.warn(
                            "⚠️ Attempted to mark non-existent user as disconnected: {}",
                            userId
                    )
                }
    }

    /**
     * Completely removes a user from the system (called after grace period expires)
     *
     * This is the final cleanup that happens when a user has been disconnected for longer than the
     * grace period. It removes them from all data structures.
     *
     * @param userId The unique ID of the user to remove permanently
     */
    private fun permanentlyRemoveUser(userId: String) {
        userSessions[userId]?.let { user ->
            // Log permanent removal
            logger.info(
                    "👋 User '{}' permanently removed from room '{}' after grace period (User ID: {})",
                    user.name,
                    user.roomId,
                    userId
            )

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
     * - Disconnected users are filtered out (not shown to clients)
     *
     * The disconnected user filtering ensures clients only see active participants, while the
     * backend keeps disconnected users in memory during the grace period for seamless reconnection.
     *
     * @param roomId The ID of the room to get state for
     * @return RoomStateUpdate with current room state, or null if room doesn't exist
     */
    fun getRoomState(roomId: String): RoomStateUpdate? {
        return rooms[roomId]?.let { room ->
            RoomStateUpdate(
                    roomId = roomId,
                    users =
                            room.users
                                    // Filter out disconnected users from client view
                                    .filter { user -> user.disconnectedAt == null }
                                    .map { user ->
                                        UserState(
                                                id = user.id,
                                                name = user.name,
                                                hasVoted = user.hasVoted,
                                                estimate =
                                                        if (room.showEstimates) user.estimate
                                                        else null
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
