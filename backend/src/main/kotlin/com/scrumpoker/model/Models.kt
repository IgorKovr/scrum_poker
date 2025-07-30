package com.scrumpoker.model

data class User(
    val id: String,
    val name: String,
    val roomId: String,
    var estimate: String? = null,
    var hasVoted: Boolean = false
)

data class Room(
    val id: String,
    val users: MutableList<User> = mutableListOf(),
    var showEstimates: Boolean = false
)

data class JoinRoomRequest(
    val name: String,
    val roomId: String
)

data class VoteRequest(
    val userId: String,
    val roomId: String,
    val estimate: String
)

data class RoomStateUpdate(
    val roomId: String,
    val users: List<UserState>,
    val showEstimates: Boolean
)

data class UserState(
    val id: String,
    val name: String,
    val estimate: String?,
    val hasVoted: Boolean
)

enum class MessageType {
    JOIN, VOTE, SHOW_ESTIMATES, HIDE_ESTIMATES, DELETE_ESTIMATES, ROOM_UPDATE, USER_LEFT
}

data class WebSocketMessage(
    val type: MessageType,
    val payload: Any
) 