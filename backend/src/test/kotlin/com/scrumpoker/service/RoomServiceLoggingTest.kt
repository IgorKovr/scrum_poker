/**
 * RoomServiceLoggingTest.kt - Tests for User Action Logging
 *
 * This test file verifies that all user actions are properly logged for monitoring, debugging, and
 * audit purposes. It uses Spring Boot testing with log capture to verify that appropriate log
 * messages are generated.
 */
package com.scrumpoker.service

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest

/**
 * Test class for verifying user action logging in RoomService
 *
 * These tests ensure that all user interactions are properly logged with appropriate detail levels
 * and formatting for monitoring and audit purposes.
 */
@SpringBootTest
class RoomServiceLoggingTest {

    private lateinit var roomService: RoomService
    private lateinit var logger: Logger
    private lateinit var listAppender: ListAppender<ILoggingEvent>

    @BeforeEach
    fun setUp() {
        roomService = RoomService()

        // Set up log capture for testing
        logger = LoggerFactory.getLogger(RoomService::class.java) as Logger
        listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)
        logger.level = Level.INFO
    }

    @AfterEach
    fun tearDown() {
        logger.detachAppender(listAppender)
    }

    /** Test that joining a room generates appropriate log messages */
    @Test
    fun `should log user joining room`() {
        // Given
        val userName = "Alice"
        val roomId = "test-room"

        // When
        val user = roomService.joinRoom(userName, roomId)

        // Then
        val logEvents = listAppender.list
        Assertions.assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        Assertions.assertEquals(Level.INFO, logEvent.level)
        Assertions.assertTrue(
                logEvent.formattedMessage.contains("📋 New user joined with name 'Alice'")
        )
        Assertions.assertTrue(logEvent.formattedMessage.contains("in room 'test-room'"))
        Assertions.assertTrue(logEvent.formattedMessage.contains("User ID: ${user.id}"))
    }

    /** Test that voting generates appropriate log messages */
    @Test
    fun `should log user vote selection`() {
        // Given
        val user = roomService.joinRoom("Bob", "test-room")
        listAppender.list.clear() // Clear join log

        // When
        roomService.vote(user.id, "5")

        // Then
        val logEvents = listAppender.list
        Assertions.assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        Assertions.assertEquals(Level.INFO, logEvent.level)
        Assertions.assertTrue(logEvent.formattedMessage.contains("🎯 User 'Bob' selected card '5'"))
        Assertions.assertTrue(logEvent.formattedMessage.contains("in room 'test-room'"))
        Assertions.assertTrue(logEvent.formattedMessage.contains("User ID: ${user.id}"))
    }

    /** Test that showing estimates generates appropriate log messages */
    @Test
    fun `should log show estimates action with user`() {
        // Given
        val user = roomService.joinRoom("Charlie", "test-room")
        listAppender.list.clear() // Clear join log

        // When
        roomService.showEstimates("test-room", user.id)

        // Then
        val logEvents = listAppender.list
        Assertions.assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        Assertions.assertEquals(Level.INFO, logEvent.level)
        Assertions.assertTrue(
                logEvent.formattedMessage.contains("👁️ User 'Charlie' pressed Show estimates")
        )
        Assertions.assertTrue(logEvent.formattedMessage.contains("in room 'test-room'"))
        Assertions.assertTrue(logEvent.formattedMessage.contains("User ID: ${user.id}"))
    }

    /** Test that showing estimates without user ID still logs appropriately */
    @Test
    fun `should log show estimates action without user`() {
        // Given
        roomService.joinRoom("Dave", "test-room")
        listAppender.list.clear() // Clear join log

        // When
        roomService.showEstimates("test-room", null)

        // Then
        val logEvents = listAppender.list
        Assertions.assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        Assertions.assertEquals(Level.INFO, logEvent.level)
        Assertions.assertTrue(
                logEvent.formattedMessage.contains(
                        "👁️ Show estimates triggered in room 'test-room'"
                )
        )
    }

    /** Test that deleting estimates generates appropriate log messages */
    @Test
    fun `should log delete estimates action with user`() {
        // Given
        val user = roomService.joinRoom("Eve", "test-room")
        listAppender.list.clear() // Clear join log

        // When
        roomService.deleteEstimates("test-room", user.id)

        // Then
        val logEvents = listAppender.list
        Assertions.assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        Assertions.assertEquals(Level.INFO, logEvent.level)
        Assertions.assertTrue(
                logEvent.formattedMessage.contains("🗑️ User 'Eve' pressed Delete Estimations")
        )
        Assertions.assertTrue(logEvent.formattedMessage.contains("in room 'test-room'"))
        Assertions.assertTrue(logEvent.formattedMessage.contains("User ID: ${user.id}"))
    }

    /** Test that leaving a room generates appropriate log messages */
    @Test
    fun `should log user leaving room`() {
        // Given - Add two users so room won't be cleaned up when one leaves
        val user1 = roomService.joinRoom("Frank", "test-room")
        val user2 = roomService.joinRoom("Grace", "test-room")
        listAppender.list.clear() // Clear join logs

        // When - Only one user leaves
        roomService.leaveRoom(user1.id)

        // Then - Should only log the user leaving (no room cleanup)
        val logEvents = listAppender.list
        Assertions.assertEquals(1, logEvents.size)

        val logEvent = logEvents[0]
        Assertions.assertEquals(Level.INFO, logEvent.level)
        Assertions.assertTrue(
                logEvent.formattedMessage.contains("👋 User 'Frank' left room 'test-room'")
        )
        Assertions.assertTrue(logEvent.formattedMessage.contains("User ID: ${user1.id}"))
    }

    /** Test that leaving an empty room also logs room cleanup */
    @Test
    fun `should log room cleanup when last user leaves`() {
        // Given
        val user = roomService.joinRoom("Grace", "test-room")
        listAppender.list.clear() // Clear join log

        // When
        roomService.leaveRoom(user.id)

        // Then
        val logEvents = listAppender.list
        Assertions.assertEquals(2, logEvents.size)

        // First log: user leaving
        val leaveLog = logEvents[0]
        Assertions.assertTrue(leaveLog.formattedMessage.contains("👋 User 'Grace' left room"))

        // Second log: room cleanup
        val cleanupLog = logEvents[1]
        Assertions.assertTrue(
                cleanupLog.formattedMessage.contains("🧹 Cleaning up empty room 'test-room'")
        )
    }

    /** Test comprehensive user journey with multiple actions */
    @Test
    fun `should log complete user journey`() {
        // Given
        val roomId = "journey-test"

        // When - User joins
        val user1 = roomService.joinRoom("Alice", roomId)
        val user2 = roomService.joinRoom("Bob", roomId)

        // User votes
        roomService.vote(user1.id, "3")
        roomService.vote(user2.id, "5")

        // Show estimates
        roomService.showEstimates(roomId, user1.id)

        // Delete estimates
        roomService.deleteEstimates(roomId, user2.id)

        // User leaves
        roomService.leaveRoom(user2.id)
        roomService.leaveRoom(user1.id)

        // Then
        val logEvents = listAppender.list
        Assertions.assertTrue(logEvents.size >= 8) // At least 8 log events expected

        // Verify key action types are logged
        val logMessages = logEvents.map { it.formattedMessage }
        Assertions.assertTrue(logMessages.any { it.contains("📋 New user joined") })
        Assertions.assertTrue(
                logMessages.any { it.contains("🎯 User") && it.contains("selected card") }
        )
        Assertions.assertTrue(
                logMessages.any { it.contains("👁️ User") && it.contains("pressed Show") }
        )
        Assertions.assertTrue(
                logMessages.any { it.contains("🗑️ User") && it.contains("pressed Delete") }
        )
        Assertions.assertTrue(
                logMessages.any { it.contains("👋 User") && it.contains("left room") }
        )
        Assertions.assertTrue(logMessages.any { it.contains("🧹 Cleaning up empty room") })
    }
}
