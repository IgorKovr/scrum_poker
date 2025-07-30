/**
 * HeartbeatService.kt - Scheduled Monitoring and Logging Service
 * 
 * This service provides automated monitoring and logging capabilities for the
 * Scrum Poker backend application. It runs scheduled tasks to provide visibility
 * into the application's operational state and resource usage.
 * 
 * Key Functions:
 * 1. Regular heartbeat logging with room and user statistics
 * 2. Detailed system status reporting at intervals
 * 3. Memory usage monitoring and reporting
 * 4. Application health tracking over time
 * 
 * The service uses Spring's @Scheduled annotation to run tasks at fixed intervals,
 * providing operational insights without requiring external monitoring tools.
 * This is particularly useful for:
 * - Debugging production issues
 * - Monitoring application performance
 * - Tracking resource usage trends
 * - Verifying application is processing requests
 * 
 * The logging output can be captured by log aggregation systems for
 * comprehensive application monitoring and alerting.
 */

package com.scrumpoker.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * HeartbeatService - Scheduled Monitoring and Logging Service
 * 
 * This service provides automated health monitoring through scheduled tasks
 * that log application statistics and system information at regular intervals.
 * It helps operators understand the application's current load and health status.
 * 
 * The service integrates with the RoomService to provide business-level metrics
 * about active rooms and users, combined with system-level metrics about
 * memory usage and thread counts.
 * 
 * Dependencies:
 * - RoomService: To get current room and user statistics
 * - Spring Scheduling: For automated task execution
 * 
 * @param roomService Service for accessing room and user data
 */
@Service
class HeartbeatService(
    /** RoomService dependency for accessing current room statistics */
    private val roomService: RoomService
) {
    
    /** Date/time formatter for consistent timestamp formatting in logs */
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    /** Counter for tracking total number of heartbeats since startup */
    private var heartbeatCount = 0L
    
    /**
     * Regular Heartbeat Monitor
     * 
     * This method runs every 60 seconds and logs basic application statistics.
     * It provides a regular "heartbeat" that shows the application is alive
     * and processing requests, along with current load information.
     * 
     * The heartbeat includes:
     * - Timestamp of the heartbeat
     * - Number of active poker rooms
     * - Total number of users across all rooms
     * - Sequential heartbeat counter
     * 
     * This information helps operators:
     * - Verify the application is running and responsive
     * - Monitor current user load and room usage
     * - Track application activity over time
     * - Identify patterns in usage (peak times, etc.)
     * 
     * Example log output:
     * [HEARTBEAT #42] 2023-12-07 14:30:15 - Active rooms: 3, Total users: 8
     */
    @Scheduled(fixedDelay = 60000) // Every 60 seconds
    fun heartbeat() {
        // Increment heartbeat counter for tracking
        heartbeatCount++
        
        // Get current timestamp for logging
        val timestamp = LocalDateTime.now().format(formatter)
        
        // Gather current application statistics
        val rooms = roomService.getAllRooms()
        val totalUsers = rooms.values.sumOf { it.users.size }  // Count users across all rooms
        val activeRooms = rooms.size  // Number of rooms with at least one user
        
        // Log heartbeat with current statistics
        println("[HEARTBEAT #$heartbeatCount] $timestamp - Active rooms: $activeRooms, Total users: $totalUsers")
    }
    
    /**
     * Detailed System Status Monitor
     * 
     * This method runs every 5 minutes and logs detailed system information
     * including memory usage and thread counts. It provides deeper insight
     * into the application's resource consumption and performance.
     * 
     * The detailed status includes:
     * - Current memory usage in megabytes
     * - Total allocated memory
     * - Number of active threads
     * - Formatted timestamp
     * 
     * This information helps with:
     * - Memory leak detection and monitoring
     * - Performance analysis and optimization
     * - Resource usage trending
     * - Capacity planning decisions
     * - Troubleshooting performance issues
     * 
     * The 5-minute interval provides regular snapshots without generating
     * excessive log volume, making it suitable for production monitoring.
     * 
     * Example log output:
     * ==================================================
     * [SYSTEM STATUS] 2023-12-07 14:35:00
     * Memory: 128 MB / 512 MB
     * Threads: 24
     * ==================================================
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    fun detailedStatus() {
        // Get runtime instance for system information
        val runtime = Runtime.getRuntime()
        val mb = 1024 * 1024  // Conversion factor for bytes to megabytes
        
        // Calculate current memory usage
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
        val totalMemory = runtime.totalMemory() / mb
        
        // Create formatted status report
        println("=".repeat(50))
        println("[SYSTEM STATUS] ${LocalDateTime.now().format(formatter)}")
        println("Memory: $usedMemory MB / $totalMemory MB")
        println("Threads: ${Thread.activeCount()}")
        println("=".repeat(50))
    }
} 