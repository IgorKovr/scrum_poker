package com.scrumpoker.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.lang.management.ManagementFactory

@RestController
class HealthController {
    
    private val startTime = Instant.now()
    private val runtime = ManagementFactory.getRuntimeMXBean()
    private val memory = ManagementFactory.getMemoryMXBean()
    
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "healthy",
            "service" to "scrum-poker-backend",
            "timestamp" to Instant.now().toString(),
            "uptime" to runtime.uptime,
            "startTime" to startTime.toString(),
            "memory" to mapOf(
                "used" to (memory.heapMemoryUsage.used / 1024 / 1024),
                "max" to (memory.heapMemoryUsage.max / 1024 / 1024),
                "committed" to (memory.heapMemoryUsage.committed / 1024 / 1024)
            ),
            "jvm" to mapOf(
                "version" to System.getProperty("java.version"),
                "vendor" to System.getProperty("java.vendor")
            )
        )
    }
    
    @GetMapping("/heartbeat")
    fun heartbeat(): Map<String, Any> {
        return mapOf(
            "status" to "alive",
            "timestamp" to Instant.now().toString(),
            "service" to "scrum-poker-backend"
        )
    }
} 