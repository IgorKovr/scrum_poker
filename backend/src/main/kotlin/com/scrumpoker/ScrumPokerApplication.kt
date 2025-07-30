package com.scrumpoker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableScheduling
class ScrumPokerApplication

fun main(args: Array<String>) {
    runApplication<ScrumPokerApplication>(*args)
}

@Component
class StartupListener {
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val runtime = Runtime.getRuntime()
        val mb = 1024 * 1024
        val maxMemory = runtime.maxMemory() / mb
        val totalMemory = runtime.totalMemory() / mb
        val freeMemory = runtime.freeMemory() / mb
        val usedMemory = totalMemory - freeMemory
        
        println("\n" + "=".repeat(60))
        println("🚀 SCRUM POKER BACKEND IS READY!")
        println("=".repeat(60))
        println("✅ Server is running at: http://localhost:8080")
        println("✅ WebSocket endpoint: ws://localhost:8080/ws")
        println("✅ Health check: http://localhost:8080/health")
        println("✅ Heartbeat: http://localhost:8080/heartbeat")
        println("=".repeat(60))
        println("📊 System Information:")
        println("   - Java Version: ${System.getProperty("java.version")}")
        println("   - JVM Vendor: ${System.getProperty("java.vendor")}")
        println("   - OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
        println("   - Available Processors: ${runtime.availableProcessors()}")
        println("=".repeat(60))
        println("💾 Memory Information:")
        println("   - Used Memory: $usedMemory MB")
        println("   - Free Memory: $freeMemory MB")
        println("   - Total Memory: $totalMemory MB")
        println("   - Max Memory: $maxMemory MB")
        println("=".repeat(60))
        println("📝 To stop the server, press Ctrl+C")
        println("=".repeat(60) + "\n")
    }
} 