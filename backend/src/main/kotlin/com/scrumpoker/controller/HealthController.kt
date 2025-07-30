/**
 * HealthController.kt - Application Health Monitoring Endpoints
 * 
 * This REST controller provides health monitoring and status endpoints for the
 * Scrum Poker backend application. These endpoints are essential for:
 * 
 * 1. Application health checks in production deployments
 * 2. Load balancer health probes
 * 3. Monitoring system integration
 * 4. Debugging and operational visibility
 * 5. Container orchestration health checks
 * 
 * The controller provides two main endpoints:
 * - /health: Comprehensive system health information
 * - /heartbeat: Lightweight alive check
 * 
 * These endpoints are useful for deployment platforms like Railway, Heroku,
 * AWS ELB, Kubernetes, and other container orchestration systems that require
 * health check endpoints to determine if the application is ready to serve traffic.
 */

package com.scrumpoker.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.lang.management.ManagementFactory

/**
 * HealthController - REST Controller for Health Monitoring
 * 
 * This controller provides endpoints for monitoring the health and status of the
 * Scrum Poker backend application. It uses JMX beans to gather system information
 * and provides real-time metrics about the application's operational state.
 * 
 * The controller is stateless and thread-safe, using Java Management APIs to
 * gather system metrics on-demand for each request.
 * 
 * Endpoints:
 * - GET /health: Detailed health information including memory usage and system info
 * - GET /heartbeat: Simple alive signal for basic health checks
 */
@RestController
class HealthController {
    
    /** Application start time for uptime calculation */
    private val startTime = Instant.now()
    
    /** Runtime MX Bean for JVM runtime information */
    private val runtime = ManagementFactory.getRuntimeMXBean()
    
    /** Memory MX Bean for memory usage statistics */
    private val memory = ManagementFactory.getMemoryMXBean()
    
    /**
     * Health Check Endpoint
     * 
     * This endpoint provides comprehensive health information about the application
     * including system metrics, memory usage, and runtime information. It's designed
     * to give operators and monitoring systems detailed insight into the application's
     * current state.
     * 
     * The response includes:
     * - Service status and identification
     * - Current timestamp and uptime
     * - Memory usage statistics (used, max, committed)
     * - JVM information (version, vendor)
     * 
     * This endpoint is ideal for:
     * - Detailed monitoring dashboards
     * - Application performance monitoring (APM) tools
     * - Debugging operational issues
     * - Capacity planning and resource monitoring
     * 
     * @return Map containing comprehensive health information
     * 
     * @sample
     * GET /health
     * {
     *   "status": "healthy",
     *   "service": "scrum-poker-backend",
     *   "timestamp": "2023-12-07T10:30:00Z",
     *   "uptime": 3600000,
     *   "startTime": "2023-12-07T09:30:00Z",
     *   "memory": {
     *     "used": 128,
     *     "max": 2048,
     *     "committed": 256
     *   },
     *   "jvm": {
     *     "version": "17.0.1",
     *     "vendor": "Eclipse Adoptium"
     *   }
     * }
     */
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            // Service identification and status
            "status" to "healthy",
            "service" to "scrum-poker-backend",
            
            // Timestamp information
            "timestamp" to Instant.now().toString(),
            "uptime" to runtime.uptime,  // Uptime in milliseconds
            "startTime" to startTime.toString(),
            
            // Memory usage statistics (in MB)
            "memory" to mapOf(
                "used" to (memory.heapMemoryUsage.used / 1024 / 1024),      // Currently used heap memory
                "max" to (memory.heapMemoryUsage.max / 1024 / 1024),        // Maximum available heap memory
                "committed" to (memory.heapMemoryUsage.committed / 1024 / 1024)  // Committed heap memory
            ),
            
            // JVM runtime information
            "jvm" to mapOf(
                "version" to System.getProperty("java.version"),    // Java version
                "vendor" to System.getProperty("java.vendor")      // JVM vendor
            )
        )
    }
    
    /**
     * Heartbeat Endpoint
     * 
     * This endpoint provides a lightweight "alive" check for the application.
     * It's designed for scenarios where you need a fast, minimal response to
     * determine if the application is responsive.
     * 
     * The response is intentionally minimal to reduce overhead and response time.
     * This makes it ideal for:
     * - Load balancer health probes
     * - High-frequency health checks
     * - Container orchestration liveness probes
     * - Basic uptime monitoring
     * 
     * Unlike the /health endpoint, this endpoint provides minimal information
     * and focuses on speed and low resource usage.
     * 
     * @return Map containing minimal alive status
     * 
     * @sample
     * GET /heartbeat
     * {
     *   "status": "alive",
     *   "timestamp": "2023-12-07T10:30:00Z",
     *   "service": "scrum-poker-backend"
     * }
     */
    @GetMapping("/heartbeat")
    fun heartbeat(): Map<String, Any> {
        return mapOf(
            // Minimal status information
            "status" to "alive",
            "timestamp" to Instant.now().toString(),
            "service" to "scrum-poker-backend"
        )
    }
} 