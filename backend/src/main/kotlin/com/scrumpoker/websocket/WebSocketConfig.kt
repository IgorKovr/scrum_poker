/**
 * WebSocketConfig.kt - WebSocket Configuration for Real-time Communication
 * 
 * This configuration class sets up WebSocket endpoints and handlers for the
 * Scrum Poker application. It configures Spring's WebSocket support to enable
 * real-time bidirectional communication between the frontend and backend.
 * 
 * Key Configuration:
 * 1. Enables WebSocket support in the Spring application
 * 2. Registers the custom WebSocket handler for message processing
 * 3. Configures CORS settings for cross-origin WebSocket connections
 * 4. Maps WebSocket endpoints to URL paths
 * 
 * WebSocket vs HTTP:
 * While HTTP is request-response based, WebSocket provides full-duplex
 * communication over a single connection. This is ideal for real-time
 * applications like Scrum Poker where multiple users need to see live
 * updates as others vote and reveal estimates.
 * 
 * Security Considerations:
 * The configuration allows all origins (*) for simplicity in development
 * and small deployments. For production systems with strict security
 * requirements, this should be restricted to specific allowed origins.
 */

package com.scrumpoker.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * WebSocketConfig - Spring Configuration for WebSocket Support
 * 
 * This configuration class implements Spring's WebSocketConfigurer interface
 * to provide custom WebSocket setup for the Scrum Poker application.
 * 
 * The configuration:
 * - Enables WebSocket functionality via @EnableWebSocket
 * - Registers custom message handlers for specific endpoints
 * - Configures CORS (Cross-Origin Resource Sharing) settings
 * - Maps URL paths to WebSocket handlers
 * 
 * Spring WebSocket Integration:
 * Spring's WebSocket support provides a higher-level abstraction over
 * the raw WebSocket API, handling connection management, message routing,
 * and integration with the Spring application context.
 * 
 * @param scrumPokerWebSocketHandler The custom handler for poker-specific messages
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(
    /** Custom WebSocket handler for Scrum Poker message processing */
    private val scrumPokerWebSocketHandler: ScrumPokerWebSocketHandler
) : WebSocketConfigurer {
    
    /**
     * Registers WebSocket handlers with the Spring WebSocket infrastructure
     * 
     * This method configures the WebSocket endpoints and their associated handlers.
     * It defines:
     * 1. Which handler processes messages for each endpoint
     * 2. The URL path where clients can connect
     * 3. CORS settings for cross-origin access
     * 
     * Endpoint Configuration:
     * - Path: "/ws" - The WebSocket endpoint URL
     * - Handler: scrumPokerWebSocketHandler - Processes all poker-related messages
     * - CORS: Allows all origins (*) for development simplicity
     * 
     * Client Connection:
     * Frontend clients connect to: ws://localhost:8080/ws (development)
     * or wss://domain.com/ws (production with HTTPS)
     * 
     * CORS Configuration:
     * setAllowedOrigins("*") permits connections from any domain.
     * For production, consider restricting to specific domains:
     * .setAllowedOrigins("https://myapp.com", "https://staging.myapp.com")
     * 
     * @param registry The WebSocket handler registry for configuration
     */
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            // Register our custom handler for the /ws endpoint
            .addHandler(scrumPokerWebSocketHandler, "/ws")
            // Allow cross-origin connections from any domain
            // Note: In production, consider restricting this to specific domains
            .setAllowedOrigins("*")
    }
} 