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

@Component
class ScrumPokerWebSocketHandler(
    private val roomService: RoomService,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {
    
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val sessionToUser = ConcurrentHashMap<String, String>()
    
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            println("[WebSocket] Received message: ${message.payload}")
            val webSocketMessage = objectMapper.readValue(message.payload, WebSocketMessage::class.java)
            println("[WebSocket] Message type: ${webSocketMessage.type}")
            
            when (webSocketMessage.type) {
                MessageType.JOIN -> handleJoin(session, webSocketMessage.payload)
                MessageType.VOTE -> handleVote(session, webSocketMessage.payload)
                MessageType.SHOW_ESTIMATES -> handleShowEstimates(session, webSocketMessage.payload)
                MessageType.HIDE_ESTIMATES -> handleHideEstimates(session, webSocketMessage.payload)
                MessageType.DELETE_ESTIMATES -> handleDeleteEstimates(session, webSocketMessage.payload)
                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleJoin(session: WebSocketSession, payload: Any) {
        val joinRequest = objectMapper.convertValue(payload, JoinRoomRequest::class.java)
        val user = roomService.joinRoom(joinRequest.name, joinRequest.roomId)
        
        sessions[user.id] = session
        sessionToUser[session.id] = user.id
        
        // Send user ID back to the client
        sendMessage(session, WebSocketMessage(
            type = MessageType.JOIN,
            payload = mapOf("userId" to user.id)
        ))
        
        // Broadcast room state to all users in the room
        broadcastRoomState(joinRequest.roomId)
    }
    
    private fun handleVote(session: WebSocketSession, payload: Any) {
        val voteRequest = objectMapper.convertValue(payload, VoteRequest::class.java)
        roomService.vote(voteRequest.userId, voteRequest.estimate)
        broadcastRoomState(voteRequest.roomId)
    }
    
    private fun handleShowEstimates(session: WebSocketSession, payload: Any) {
        val roomId = (payload as Map<*, *>)["roomId"] as String
        roomService.showEstimates(roomId)
        broadcastRoomState(roomId)
    }
    
    private fun handleHideEstimates(session: WebSocketSession, payload: Any) {
        val roomId = (payload as Map<*, *>)["roomId"] as String
        roomService.hideEstimates(roomId)
        broadcastRoomState(roomId)
    }
    
    private fun handleDeleteEstimates(session: WebSocketSession, payload: Any) {
        val roomId = (payload as Map<*, *>)["roomId"] as String
        roomService.deleteEstimates(roomId)
        broadcastRoomState(roomId)
    }
    
    private fun broadcastRoomState(roomId: String) {
        val roomState = roomService.getRoomState(roomId) ?: return
        val message = WebSocketMessage(
            type = MessageType.ROOM_UPDATE,
            payload = roomState
        )
        
        roomService.getUsersByRoom(roomId).forEach { user ->
            sessions[user.id]?.let { session ->
                sendMessage(session, message)
            }
        }
    }
    
    private fun sendMessage(session: WebSocketSession, message: WebSocketMessage) {
        if (session.isOpen) {
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
        }
    }
    
    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("[WebSocket] New connection established: ${session.id}")
        println("[WebSocket] Remote address: ${session.remoteAddress}")
        super.afterConnectionEstablished(session)
    }
    
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        println("[WebSocket] Connection closed: ${session.id}, Status: ${status.code} - ${status.reason}")
        sessionToUser[session.id]?.let { userId ->
            roomService.getUserById(userId)?.let { user ->
                val roomId = user.roomId
                roomService.leaveRoom(userId)
                sessions.remove(userId)
                sessionToUser.remove(session.id)
                
                // Broadcast updated room state
                broadcastRoomState(roomId)
            }
        }
    }
} 