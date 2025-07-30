package com.scrumpoker.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class HeartbeatService(
    private val roomService: RoomService
) {
    
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private var heartbeatCount = 0L
    
    @Scheduled(fixedDelay = 60000) // Every 60 seconds
    fun heartbeat() {
        heartbeatCount++
        val timestamp = LocalDateTime.now().format(formatter)
        val rooms = roomService.getAllRooms()
        val totalUsers = rooms.values.sumOf { it.users.size }
        val activeRooms = rooms.size
        
        println("[HEARTBEAT #$heartbeatCount] $timestamp - Active rooms: $activeRooms, Total users: $totalUsers")
    }
    
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    fun detailedStatus() {
        val runtime = Runtime.getRuntime()
        val mb = 1024 * 1024
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
        val totalMemory = runtime.totalMemory() / mb
        
        println("=".repeat(50))
        println("[SYSTEM STATUS] ${LocalDateTime.now().format(formatter)}")
        println("Memory: $usedMemory MB / $totalMemory MB")
        println("Threads: ${Thread.activeCount()}")
        println("=".repeat(50))
    }
} 