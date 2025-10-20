/**
 * ScrumPokerApplication.kt - Main Spring Boot Application Entry Point
 *
 * This file contains the main application class and startup configuration for the Scrum Poker
 * backend service. It sets up the Spring Boot application with WebSocket support for real-time
 * communication.
 *
 * Key Components:
 * 1. Main application class with Spring Boot configuration
 * 2. Startup listener that provides detailed system information on boot
 *
 * The application uses Spring Boot's auto-configuration to set up:
 * - WebSocket endpoints for real-time communication
 * - REST controllers for health checks
 * - Service layer for business logic
 * - In-memory state management for rooms and users
 *
 * System Information Display: On startup, the application displays comprehensive system information
 * including memory usage, JVM details, and available endpoints for debugging and monitoring.
 */
package com.scrumpoker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

/**
 * Main Spring Boot Application Class
 *
 * This class serves as the entry point for the Scrum Poker backend application. It uses Spring Boot
 * annotations to configure the application automatically.
 *
 * Annotations:
 * - @SpringBootApplication: Enables auto-configuration, component scanning, and configuration
 * - @EnableScheduling: Enables scheduled task execution for grace period cleanup
 *
 * The application automatically scans for components in the com.scrumpoker package and sets up all
 * necessary beans for dependency injection.
 */
@SpringBootApplication @EnableScheduling class ScrumPokerApplication

/**
 * Main function - Application entry point
 *
 * This function starts the Spring Boot application using the standard runApplication helper
 * function. It passes command line arguments to the application for configuration flexibility.
 *
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<ScrumPokerApplication>(*args)
}

/**
 * Startup Listener Component
 *
 * This component listens for the ApplicationReadyEvent and displays detailed system information
 * when the application has fully started. This provides valuable debugging information and confirms
 * the application is ready to accept connections.
 *
 * The displayed information includes:
 * - Service endpoints and URLs
 * - Java and JVM information
 * - System specifications
 * - Memory usage statistics
 *
 * This information is especially useful for:
 * - Debugging deployment issues
 * - Monitoring resource usage
 * - Confirming proper startup in containerized environments
 */
@Component
class StartupListener {

    /**
     * Application Ready Event Handler
     *
     * This method is called when the Spring Boot application has fully started and is ready to
     * accept requests. It displays comprehensive system information in a formatted, easy-to-read
     * banner.
     *
     * The method calculates and displays:
     * - Current memory usage in megabytes
     * - Available system resources
     * - JVM and operating system information
     * - Application endpoints for client connections
     *
     * This information helps with:
     * - Deployment verification
     * - Performance monitoring
     * - Troubleshooting connection issues
     */
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        // Get runtime instance for system information
        val runtime = Runtime.getRuntime()
        val mb = 1024 * 1024 // Conversion factor for bytes to megabytes

        // Calculate memory usage statistics
        val maxMemory = runtime.maxMemory() / mb // Maximum available memory
        val totalMemory = runtime.totalMemory() / mb // Currently allocated memory
        val freeMemory = runtime.freeMemory() / mb // Free memory within allocated
        val usedMemory = totalMemory - freeMemory // Actually used memory

        // Display startup banner with system information
        println("\n" + "=".repeat(60))
        println("🚀 SCRUM POKER BACKEND IS READY!")
        println("=".repeat(60))

        // Display service endpoints for client connection
        println("✅ Server is running at: http://localhost:8080")
        println("✅ WebSocket endpoint: ws://localhost:8080/ws")
        println("✅ Health check: http://localhost:8080/health")

        println("=".repeat(60))
        println("📊 System Information:")

        // Display Java and system environment information
        println("   - Java Version: ${System.getProperty("java.version")}")
        println("   - JVM Vendor: ${System.getProperty("java.vendor")}")
        println("   - OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
        println("   - Available Processors: ${runtime.availableProcessors()}")

        println("=".repeat(60))
        println("💾 Memory Information:")

        // Display detailed memory usage statistics
        println("   - Used Memory: $usedMemory MB")
        println("   - Free Memory: $freeMemory MB")
        println("   - Total Memory: $totalMemory MB")
        println("   - Max Memory: $maxMemory MB")

        println("=".repeat(60))
        println("🔗 Ready to accept WebSocket connections!")
        println("   Frontend can now connect to ws://localhost:8080/ws")
        println("=".repeat(60) + "\n")
    }
}
