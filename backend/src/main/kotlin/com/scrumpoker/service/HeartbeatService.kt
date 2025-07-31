/**
 * HeartbeatService.kt - Scheduled Service for Application Monitoring
 *
 * This service provides regular system health monitoring and logging capabilities. It runs on a
 * scheduled basis to provide insights into application performance, resource usage, and operational
 * status.
 *
 * Key Responsibilities:
 * 1. Memory usage monitoring and reporting
 * 2. System resource tracking (threads, memory, etc.)
 * 3. Application health status logging
 * 4. Regular maintenance and cleanup operations
 * 5. Performance metrics collection
 *
 * The service helps with operational monitoring and can alert administrators to potential issues
 * through log analysis. It's particularly useful for identifying memory leaks, performance
 * degradation, and system resource trends.
 *
 * Scheduling Configuration: The service uses Spring's @Scheduled annotation with fixed rate
 * execution, ensuring consistent monitoring intervals regardless of execution time.
 */
package com.scrumpoker.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * HeartbeatService - Scheduled monitoring and maintenance service
 *
 * This service performs regular health checks and maintenance operations to ensure optimal
 * application performance and resource usage.
 *
 * @param roomService Service for poker room business logic and memory management
 */
@Service
class HeartbeatService(private val roomService: RoomService) {

    /**
     * Logger for heartbeat and system monitoring information
     *
     * Used to log regular system status updates, memory usage, and performance metrics for
     * monitoring and alerting purposes.
     */
    private val logger = LoggerFactory.getLogger(HeartbeatService::class.java)

    /** Counter for heartbeat cycles to control cleanup frequency */
    private var heartbeatCount = 0

    /**
     * Basic heartbeat logging
     *
     * This method runs every 30 seconds to provide basic application status. It logs active room
     * and user counts to help monitor application usage.
     *
     * The quick heartbeat provides immediate feedback on application activity without the overhead
     * of detailed system inspection.
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    fun heartbeat() {
        heartbeatCount++

        // Get basic activity metrics
        val totalRooms = roomService.getAllRooms().size
        val totalUsers = roomService.getAllRooms().values.sumOf { it.users.size }

        logger.info(
                "[HEARTBEAT #{}] Active rooms: {}, Total users: {}",
                heartbeatCount,
                totalRooms,
                totalUsers
        )
    }

    /**
     * Detailed system status and maintenance
     *
     * This method runs every 5 minutes to provide comprehensive system monitoring:
     * 1. Memory usage analysis (heap, non-heap, garbage collection)
     * 2. Thread monitoring and resource tracking
     * 3. Application-specific metrics (rooms, users, sessions)
     * 4. Memory leak prevention through maintenance cleanup
     *
     * The detailed status helps identify performance trends and potential issues before they become
     * critical problems.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    fun detailedStatus() {
        // Memory monitoring
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory() / (1024 * 1024) // MB
        val freeMemory = runtime.freeMemory() / (1024 * 1024) // MB
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory() / (1024 * 1024) // MB

        // Thread monitoring
        val threadCount = Thread.activeCount()

        // Application metrics
        val allRooms = roomService.getAllRooms()
        val totalRooms = allRooms.size
        val totalUsers = allRooms.values.sumOf { it.users.size }
        val averageUsersPerRoom = if (totalRooms > 0) totalUsers.toDouble() / totalRooms else 0.0

        // Calculate memory utilization percentage
        val memoryUtilization = (usedMemory.toDouble() / maxMemory * 100)

        logger.info(
                """
            💾 MEMORY STATUS:
               Used: {} MB / {} MB ({:.1f}%)
               Free: {} MB
               Total Heap: {} MB
               
            📊 APPLICATION METRICS:
               Active Rooms: {}
               Total Users: {}
               Avg Users/Room: {:.1f}
               Active Threads: {}
               
            ⚡ PERFORMANCE:
               Memory Utilization: {:.1f}%
               Heartbeat Count: {}
        """.trimIndent(),
                usedMemory,
                maxMemory,
                memoryUtilization,
                freeMemory,
                totalMemory,
                totalRooms,
                totalUsers,
                averageUsersPerRoom,
                threadCount,
                memoryUtilization,
                heartbeatCount
        )

        // Memory leak prevention: Run cleanup every 3rd detailed status (15 minutes)
        if (heartbeatCount % 3 == 0) {
            logger.info("🧹 Running scheduled maintenance cleanup...")
            roomService.performMaintenanceCleanup()
        }

        // Alert if memory usage is high
        if (memoryUtilization > 80) {
            logger.warn(
                    "⚠️ HIGH MEMORY USAGE: {:.1f}% - Consider investigating memory leaks",
                    memoryUtilization
            )
        }

        // Alert if too many rooms/users (potential memory leak)
        if (totalRooms > 100) {
            logger.warn("⚠️ HIGH ROOM COUNT: {} rooms - Potential memory leak detected", totalRooms)
        }

        if (totalUsers > 500) {
            logger.warn("⚠️ HIGH USER COUNT: {} users - Potential memory leak detected", totalUsers)
        }
    }
}
