package com.scrumpoker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@SpringBootApplication
class ScrumPokerApplication

fun main(args: Array<String>) {
    runApplication<ScrumPokerApplication>(*args)
}

@Component
class StartupListener {
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        println("\n" + "=".repeat(60))
        println("🚀 SCRUM POKER BACKEND IS READY!")
        println("=".repeat(60))
        println("✅ Server is running at: http://localhost:8080")
        println("✅ WebSocket endpoint: ws://localhost:8080/ws")
        println("=".repeat(60))
        println("📝 To stop the server, press Ctrl+C")
        println("=".repeat(60) + "\n")
    }
} 