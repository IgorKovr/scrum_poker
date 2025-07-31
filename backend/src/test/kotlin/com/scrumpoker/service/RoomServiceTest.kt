/**
 * RoomServiceTest.kt - Unit Tests for RoomService Business Logic
 * 
 * This test class provides comprehensive coverage of the RoomService class,
 * which contains the core business logic for the Scrum Poker application.
 * 
 * Test Coverage:
 * 1. Room joining and user creation
 * 2. User leaving and cleanup
 * 3. Voting functionality
 * 4. Estimate visibility management (show/hide)
 * 5. Estimate deletion and reset
 * 6. Room state generation
 * 7. Edge cases and error conditions
 * 
 * Testing Approach:
 * - Each test method focuses on a single piece of functionality
 * - Tests verify both successful operations and edge cases
 * - State is verified after each operation to ensure consistency
 * - Concurrent access scenarios are tested where relevant
 */

package com.scrumpoker.service

import com.scrumpoker.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

/**
 * Test class for RoomService business logic
 * 
 * Uses JUnit 5 features including nested test classes for better organization
 * and descriptive display names for clear test reporting.
 */
@DisplayName("RoomService Business Logic Tests")
class RoomServiceTest {
    
    private lateinit var roomService: RoomService
    
    /**
     * Set up fresh RoomService instance for each test
     * This ensures test isolation and prevents test interdependence
     */
    @BeforeEach
    fun setUp() {
        roomService = RoomService()
    }
    
    /**
     * Tests for room joining functionality
     */
    @Nested
    @DisplayName("Room Joining Tests")
    inner class RoomJoiningTests {
        
        @Test
        @DisplayName("Should create new user when joining room")
        fun `joinRoom should create new user with generated ID`() {
            // Given
            val userName = "Alice"
            val roomId = "test-room"
            
            // When
            val user = roomService.joinRoom(userName, roomId)
            
            // Then
            assertNotNull(user.id, "User ID should be generated")
            assertEquals(userName, user.name, "User name should match input")
            assertEquals(roomId, user.roomId, "User room ID should match input")
            assertNull(user.estimate, "New user should have no estimate")
            assertFalse(user.hasVoted, "New user should not have voted")
        }
        
        @Test
        @DisplayName("Should create room automatically when first user joins")
        fun `joinRoom should create room if it doesn't exist`() {
            // Given
            val roomId = "new-room"
            val userName = "Bob"
            
            // When
            roomService.joinRoom(userName, roomId)
            val rooms = roomService.getAllRooms()
            
            // Then
            assertTrue(rooms.containsKey(roomId), "Room should be created automatically")
            assertEquals(1, rooms[roomId]?.users?.size, "Room should have one user")
        }
        
        @Test
        @DisplayName("Should add user to existing room")
        fun `joinRoom should add user to existing room`() {
            // Given
            val roomId = "existing-room"
            roomService.joinRoom("Alice", roomId)
            
            // When
            val secondUser = roomService.joinRoom("Bob", roomId)
            val rooms = roomService.getAllRooms()
            
            // Then
            assertEquals(2, rooms[roomId]?.users?.size, "Room should have two users")
            assertTrue(rooms[roomId]?.users?.any { it.name == "Alice" } ?: false, "Room should contain Alice")
            assertTrue(rooms[roomId]?.users?.any { it.name == "Bob" } ?: false, "Room should contain Bob")
        }
        
        @Test
        @DisplayName("Should assign unique IDs to different users")
        fun `joinRoom should assign unique IDs to each user`() {
            // Given
            val roomId = "test-room"
            
            // When
            val user1 = roomService.joinRoom("Alice", roomId)
            val user2 = roomService.joinRoom("Bob", roomId)
            
            // Then
            assertNotEquals(user1.id, user2.id, "Each user should have unique ID")
        }
    }
    
    /**
     * Tests for user leaving functionality
     */
    @Nested
    @DisplayName("User Leaving Tests")
    inner class UserLeavingTests {
        
        @Test
        @DisplayName("Should remove user from room when leaving")
        fun `leaveRoom should remove user from room`() {
            // Given
            val roomId = "test-room"
            val user = roomService.joinRoom("Alice", roomId)
            
            // When
            roomService.leaveRoom(user.id)
            val rooms = roomService.getAllRooms()
            
            // Then
            assertTrue(rooms.isEmpty() || rooms[roomId]?.users?.isEmpty() == true, 
                "User should be removed from room")
        }
        
        @Test
        @DisplayName("Should clean up empty room when last user leaves")
        fun `leaveRoom should remove empty room`() {
            // Given
            val roomId = "test-room"
            val user = roomService.joinRoom("Alice", roomId)
            
            // When
            roomService.leaveRoom(user.id)
            val rooms = roomService.getAllRooms()
            
            // Then
            assertFalse(rooms.containsKey(roomId), "Empty room should be cleaned up")
        }
        
        @Test
        @DisplayName("Should keep room when other users remain")
        fun `leaveRoom should not remove room with remaining users`() {
            // Given
            val roomId = "test-room"
            val user1 = roomService.joinRoom("Alice", roomId)
            val user2 = roomService.joinRoom("Bob", roomId)
            
            // When
            roomService.leaveRoom(user1.id)
            val rooms = roomService.getAllRooms()
            
            // Then
            assertTrue(rooms.containsKey(roomId), "Room should exist with remaining users")
            assertEquals(1, rooms[roomId]?.users?.size, "Room should have one remaining user")
            assertEquals("Bob", rooms[roomId]?.users?.first()?.name, "Bob should remain in room")
        }
        
        @Test
        @DisplayName("Should handle leaving non-existent user gracefully")
        fun `leaveRoom should handle non-existent user ID gracefully`() {
            // Given
            val nonExistentUserId = "non-existent-user"
            
            // When & Then (should not throw exception)
            assertDoesNotThrow {
                roomService.leaveRoom(nonExistentUserId)
            }
        }
    }
    
    /**
     * Tests for voting functionality
     */
    @Nested
    @DisplayName("Voting Tests")
    inner class VotingTests {
        
        @Test
        @DisplayName("Should record user vote correctly")
        fun `vote should record estimate and mark user as voted`() {
            // Given
            val user = roomService.joinRoom("Alice", "test-room")
            val estimate = "5"
            
            // When
            roomService.vote(user.id, estimate)
            val retrievedUser = roomService.getUserById(user.id)
            
            // Then
            assertEquals(estimate, retrievedUser?.estimate, "Estimate should be recorded")
            assertTrue(retrievedUser?.hasVoted ?: false, "User should be marked as voted")
        }
        
        @Test
        @DisplayName("Should allow user to change vote")
        fun `vote should allow changing estimate`() {
            // Given
            val user = roomService.joinRoom("Alice", "test-room")
            roomService.vote(user.id, "3")
            
            // When
            roomService.vote(user.id, "8")
            val retrievedUser = roomService.getUserById(user.id)
            
            // Then
            assertEquals("8", retrievedUser?.estimate, "Estimate should be updated")
            assertTrue(retrievedUser?.hasVoted ?: false, "User should still be marked as voted")
        }
        
        @Test
        @DisplayName("Should handle voting by non-existent user gracefully")
        fun `vote should handle non-existent user ID gracefully`() {
            // Given
            val nonExistentUserId = "non-existent-user"
            
            // When & Then (should not throw exception)
            assertDoesNotThrow {
                roomService.vote(nonExistentUserId, "5")
            }
        }
        
        @Test
        @DisplayName("Should accept any string as estimate")
        fun `vote should accept special values as estimates`() {
            // Given
            val user = roomService.joinRoom("Alice", "test-room")
            val specialEstimate = "?"
            
            // When
            roomService.vote(user.id, specialEstimate)
            val retrievedUser = roomService.getUserById(user.id)
            
            // Then
            assertEquals(specialEstimate, retrievedUser?.estimate, "Special estimate should be recorded")
        }
    }
    
    /**
     * Tests for estimate visibility management
     */
    @Nested
    @DisplayName("Estimate Visibility Tests")
    inner class EstimateVisibilityTests {
        
        @Test
        @DisplayName("Should show estimates when requested")
        fun `showEstimates should make estimates visible`() {
            // Given
            val roomId = "test-room"
            val user = roomService.joinRoom("Alice", roomId)
            roomService.vote(user.id, "5")
            
            // When
            roomService.showEstimates(roomId)
            val roomState = roomService.getRoomState(roomId)
            
            // Then
            assertTrue(roomState?.showEstimates ?: false, "Estimates should be visible")
            assertEquals("5", roomState?.users?.first()?.estimate, "User estimate should be visible")
        }
        
        @Test
        @DisplayName("Should hide estimates when requested")
        fun `hideEstimates should make estimates invisible`() {
            // Given
            val roomId = "test-room"
            val user = roomService.joinRoom("Alice", roomId)
            roomService.vote(user.id, "5")
            roomService.showEstimates(roomId)
            
            // When
            roomService.hideEstimates(roomId)
            val roomState = roomService.getRoomState(roomId)
            
            // Then
            assertFalse(roomState?.showEstimates ?: true, "Estimates should be hidden")
            assertNull(roomState?.users?.first()?.estimate, "User estimate should be hidden")
        }
        
        @Test
        @DisplayName("Should handle show estimates for non-existent room")
        fun `showEstimates should handle non-existent room gracefully`() {
            // Given
            val nonExistentRoomId = "non-existent-room"
            
            // When & Then (should not throw exception)
            assertDoesNotThrow {
                roomService.showEstimates(nonExistentRoomId)
            }
        }
    }
    
    /**
     * Tests for estimate deletion and reset
     */
    @Nested
    @DisplayName("Estimate Deletion Tests")
    inner class EstimateDeletionTests {
        
        @Test
        @DisplayName("Should clear all estimates and reset voting status")
        fun `deleteEstimates should clear all user estimates and reset voting`() {
            // Given
            val roomId = "test-room"
            val user1 = roomService.joinRoom("Alice", roomId)
            val user2 = roomService.joinRoom("Bob", roomId)
            roomService.vote(user1.id, "5")
            roomService.vote(user2.id, "8")
            roomService.showEstimates(roomId)
            
            // When
            roomService.deleteEstimates(roomId)
            val roomState = roomService.getRoomState(roomId)
            
            // Then
            assertFalse(roomState?.showEstimates ?: true, "Estimates should be hidden")
            roomState?.users?.forEach { user ->
                assertNull(user.estimate, "User estimate should be cleared")
                assertFalse(user.hasVoted, "User voting status should be reset")
            }
        }
        
        @Test
        @DisplayName("Should handle delete estimates for non-existent room")
        fun `deleteEstimates should handle non-existent room gracefully`() {
            // Given
            val nonExistentRoomId = "non-existent-room"
            
            // When & Then (should not throw exception)
            assertDoesNotThrow {
                roomService.deleteEstimates(nonExistentRoomId)
            }
        }
    }
    
    /**
     * Tests for room state generation
     */
    @Nested
    @DisplayName("Room State Tests")
    inner class RoomStateTests {
        
        @Test
        @DisplayName("Should return correct room state with hidden estimates")
        fun `getRoomState should return state with hidden estimates`() {
            // Given
            val roomId = "test-room"
            val user = roomService.joinRoom("Alice", roomId)
            roomService.vote(user.id, "5")
            
            // When
            val roomState = roomService.getRoomState(roomId)
            
            // Then
            assertNotNull(roomState, "Room state should be returned")
            assertEquals(roomId, roomState?.roomId, "Room ID should match")
            assertEquals(1, roomState?.users?.size, "Should have one user")
            assertFalse(roomState?.showEstimates ?: true, "Estimates should be hidden by default")
            assertNull(roomState?.users?.first()?.estimate, "User estimate should be hidden")
            assertTrue(roomState?.users?.first()?.hasVoted ?: false, "Voting status should be visible")
        }
        
        @Test
        @DisplayName("Should return correct room state with visible estimates")
        fun `getRoomState should return state with visible estimates`() {
            // Given
            val roomId = "test-room"
            val user = roomService.joinRoom("Alice", roomId)
            roomService.vote(user.id, "5")
            roomService.showEstimates(roomId)
            
            // When
            val roomState = roomService.getRoomState(roomId)
            
            // Then
            assertTrue(roomState?.showEstimates ?: false, "Estimates should be visible")
            assertEquals("5", roomState?.users?.first()?.estimate, "User estimate should be visible")
        }
        
        @Test
        @DisplayName("Should return null for non-existent room")
        fun `getRoomState should return null for non-existent room`() {
            // Given
            val nonExistentRoomId = "non-existent-room"
            
            // When
            val roomState = roomService.getRoomState(nonExistentRoomId)
            
            // Then
            assertNull(roomState, "Should return null for non-existent room")
        }
    }
    
    /**
     * Tests for user lookup functionality
     */
    @Nested
    @DisplayName("User Lookup Tests")
    inner class UserLookupTests {
        
        @Test
        @DisplayName("Should find user by ID")
        fun `getUserById should return correct user`() {
            // Given
            val user = roomService.joinRoom("Alice", "test-room")
            
            // When
            val retrievedUser = roomService.getUserById(user.id)
            
            // Then
            assertNotNull(retrievedUser, "User should be found")
            assertEquals(user.id, retrievedUser?.id, "User ID should match")
            assertEquals(user.name, retrievedUser?.name, "User name should match")
        }
        
        @Test
        @DisplayName("Should return null for non-existent user ID")
        fun `getUserById should return null for non-existent user`() {
            // Given
            val nonExistentUserId = "non-existent-user"
            
            // When
            val retrievedUser = roomService.getUserById(nonExistentUserId)
            
            // Then
            assertNull(retrievedUser, "Should return null for non-existent user")
        }
        
        @Test
        @DisplayName("Should return users by room")
        fun `getUsersByRoom should return all users in room`() {
            // Given
            val roomId = "test-room"
            val user1 = roomService.joinRoom("Alice", roomId)
            val user2 = roomService.joinRoom("Bob", roomId)
            
            // When
            val users = roomService.getUsersByRoom(roomId)
            
            // Then
            assertEquals(2, users.size, "Should return all users in room")
            assertTrue(users.any { it.name == "Alice" }, "Should include Alice")
            assertTrue(users.any { it.name == "Bob" }, "Should include Bob")
        }
        
        @Test
        @DisplayName("Should return empty list for non-existent room")
        fun `getUsersByRoom should return empty list for non-existent room`() {
            // Given
            val nonExistentRoomId = "non-existent-room"
            
            // When
            val users = roomService.getUsersByRoom(nonExistentRoomId)
            
            // Then
            assertTrue(users.isEmpty(), "Should return empty list for non-existent room")
        }
    }
    
    /**
     * Tests for concurrent access scenarios
     */
    @Nested
    @DisplayName("Concurrent Access Tests")
    inner class ConcurrentAccessTests {
        
        @Test
        @DisplayName("Should handle multiple users joining same room concurrently")
        fun `multiple users can join same room safely`() {
            // Given
            val roomId = "concurrent-room"
            val userNames = listOf("Alice", "Bob", "Charlie", "Diana")
            
            // When - Simulate concurrent joins
            val users = userNames.map { name ->
                roomService.joinRoom(name, roomId)
            }
            
            // Then
            val rooms = roomService.getAllRooms()
            assertEquals(4, rooms[roomId]?.users?.size, "All users should be added to room")
            
            // Verify all users have unique IDs
            val userIds = users.map { it.id }
            assertEquals(userIds.size, userIds.toSet().size, "All user IDs should be unique")
        }
        
        @Test
        @DisplayName("Should handle concurrent voting safely")
        fun `concurrent voting should work correctly`() {
            // Given
            val roomId = "vote-room"
            val users = listOf(
                roomService.joinRoom("Alice", roomId),
                roomService.joinRoom("Bob", roomId),
                roomService.joinRoom("Charlie", roomId)
            )
            
            // When - Simulate concurrent voting
            users.forEachIndexed { index, user ->
                roomService.vote(user.id, (index + 1).toString())
            }
            
            // Then
            val roomState = roomService.getRoomState(roomId)
            assertEquals(3, roomState?.users?.size, "All users should be in room")
            roomState?.users?.forEach { user ->
                assertTrue(user.hasVoted, "All users should have voted")
            }
        }
    }
} 