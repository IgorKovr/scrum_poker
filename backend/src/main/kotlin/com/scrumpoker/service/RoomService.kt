package com.scrumpoker.service

import com.scrumpoker.model.*
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

@Service
class RoomService {
    private val rooms = ConcurrentHashMap<String, Room>()
    private val userSessions = ConcurrentHashMap<String, User>()
    
    fun joinRoom(name: String, roomId: String): User {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            roomId = roomId
        )
        
        val room = rooms.computeIfAbsent(roomId) { Room(it) }
        room.users.add(user)
        userSessions[user.id] = user
        
        return user
    }
    
    fun leaveRoom(userId: String) {
        userSessions[userId]?.let { user ->
            rooms[user.roomId]?.users?.removeIf { it.id == userId }
            userSessions.remove(userId)
            
            // Clean up empty rooms
            if (rooms[user.roomId]?.users?.isEmpty() == true) {
                rooms.remove(user.roomId)
            }
        }
    }
    
    fun vote(userId: String, estimate: String) {
        userSessions[userId]?.let { user ->
            user.estimate = estimate
            user.hasVoted = true
        }
    }
    
    fun showEstimates(roomId: String) {
        rooms[roomId]?.showEstimates = true
    }
    
    fun hideEstimates(roomId: String) {
        rooms[roomId]?.showEstimates = false
    }
    
    fun deleteEstimates(roomId: String) {
        rooms[roomId]?.let { room ->
            room.users.forEach { user ->
                user.estimate = null
                user.hasVoted = false
            }
            room.showEstimates = false
        }
    }
    
    fun getRoomState(roomId: String): RoomStateUpdate? {
        return rooms[roomId]?.let { room ->
            RoomStateUpdate(
                roomId = roomId,
                users = room.users.map { user ->
                    UserState(
                        id = user.id,
                        name = user.name,
                        estimate = if (room.showEstimates) user.estimate else null,
                        hasVoted = user.hasVoted
                    )
                },
                showEstimates = room.showEstimates
            )
        }
    }
    
    fun getUserById(userId: String): User? = userSessions[userId]
    
    fun getUsersByRoom(roomId: String): List<User> {
        return rooms[roomId]?.users ?: emptyList()
    }
} 