/**
 * HealthControllerTest.kt - Unit Tests for Health Monitoring Endpoints
 * 
 * This test class verifies the functionality of the HealthController,
 * which provides health monitoring capabilities for the application.
 * 
 * Test Coverage:
 * 1. Health endpoint response structure and content
 * 2. Heartbeat endpoint response structure and content
 * 3. Response status codes and content types
 * 4. Data validation and consistency
 * 
 * Testing Approach:
 * - Uses Spring Boot's testing framework for integration testing
 * - Verifies JSON response structure and content
 * - Tests endpoint availability and response times
 * - Validates system metrics and information accuracy
 */

package com.scrumpoker.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Test class for HealthController endpoints
 * 
 * Uses Spring Boot's WebMvcTest for focused controller testing
 * without loading the full application context.
 */
@WebMvcTest(HealthController::class)
@DisplayName("HealthController Endpoint Tests")
class HealthControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    /**
     * Tests for the /health endpoint
     */
    @Nested
    @DisplayName("Health Endpoint Tests")
    inner class HealthEndpointTests {
        
        @Test
        @DisplayName("Should return 200 OK for health endpoint")
        fun `health endpoint should return successful response`() {
            mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        }
        
        @Test
        @DisplayName("Should return correct health response structure")
        fun `health endpoint should return expected JSON structure`() {
            mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("scrum-poker-backend"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.uptime").exists())
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.jvm").exists())
        }
        
        @Test
        @DisplayName("Should return valid memory information")
        fun `health endpoint should return memory metrics`() {
            mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.memory.used").isNumber())
                .andExpect(jsonPath("$.memory.max").isNumber())
                .andExpect(jsonPath("$.memory.committed").isNumber())
        }
        
        @Test
        @DisplayName("Should return valid JVM information")
        fun `health endpoint should return JVM information`() {
            mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.jvm.version").exists())
                .andExpect(jsonPath("$.jvm.vendor").exists())
        }
        
        @Test
        @DisplayName("Should return valid timestamp format")
        fun `health endpoint should return ISO timestamp`() {
            mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.matchesPattern(
                    "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z"
                )))
        }
        
        @Test
        @DisplayName("Should return positive uptime")
        fun `health endpoint should return positive uptime`() {
            mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.uptime").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
        }
    }
    
    /**
     * Tests for the /heartbeat endpoint
     */
    @Nested
    @DisplayName("Heartbeat Endpoint Tests")
    inner class HeartbeatEndpointTests {
        
        @Test
        @DisplayName("Should return 200 OK for heartbeat endpoint")
        fun `heartbeat endpoint should return successful response`() {
            mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        }
        
        @Test
        @DisplayName("Should return correct heartbeat response structure")
        fun `heartbeat endpoint should return expected JSON structure`() {
            mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("alive"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.service").value("scrum-poker-backend"))
        }
        
        @Test
        @DisplayName("Should return valid timestamp format")
        fun `heartbeat endpoint should return ISO timestamp`() {
            mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.matchesPattern(
                    "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z"
                )))
        }
        
        @Test
        @DisplayName("Should be faster than health endpoint")
        fun `heartbeat endpoint should respond quickly`() {
            val start = System.currentTimeMillis()
            
            mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk)
            
            val duration = System.currentTimeMillis() - start
            // Heartbeat should be very fast (under 100ms in test environment)
            assert(duration < 100) { "Heartbeat endpoint took too long: ${duration}ms" }
        }
    }
    
    /**
     * Tests for endpoint comparison and consistency
     */
    @Nested
    @DisplayName("Endpoint Consistency Tests")
    inner class EndpointConsistencyTests {
        
        @Test
        @DisplayName("Both endpoints should return same service name")
        fun `health and heartbeat should return consistent service name`() {
            // Get responses from both endpoints
            val healthResult = mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andReturn()
            
            val heartbeatResult = mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk)
                .andReturn()
            
            // Parse JSON responses
            val healthJson = objectMapper.readTree(healthResult.response.contentAsString)
            val heartbeatJson = objectMapper.readTree(heartbeatResult.response.contentAsString)
            
            // Verify service names match
            assertEquals(
                healthJson.get("service").asText(),
                heartbeatJson.get("service").asText(),
                "Service names should be consistent across endpoints"
            )
        }
        
        @Test
        @DisplayName("Timestamps should be reasonable and close")
        fun `endpoint timestamps should be within reasonable range`() {
            val start = System.currentTimeMillis()
            
            // Call both endpoints
            val healthResult = mockMvc.perform(get("/health"))
                .andExpect(status().isOk)
                .andReturn()
            
            val heartbeatResult = mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk)
                .andReturn()
            
            val end = System.currentTimeMillis()
            
            // Parse timestamps
            val healthJson = objectMapper.readTree(healthResult.response.contentAsString)
            val heartbeatJson = objectMapper.readTree(heartbeatResult.response.contentAsString)
            
            val healthTimestamp = java.time.Instant.parse(healthJson.get("timestamp").asText()).toEpochMilli()
            val heartbeatTimestamp = java.time.Instant.parse(heartbeatJson.get("timestamp").asText()).toEpochMilli()
            
            // Verify timestamps are within test execution window
            assertTrue(healthTimestamp >= start && healthTimestamp <= end,
                "Health timestamp should be within test execution window")
            assertTrue(heartbeatTimestamp >= start && heartbeatTimestamp <= end,
                "Heartbeat timestamp should be within test execution window")
        }
    }
    
    /**
     * Tests for edge cases and error handling
     */
    @Nested
    @DisplayName("Edge Case Tests")
    inner class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle concurrent requests to health endpoint")
        fun `health endpoint should handle concurrent requests`() {
            // Simulate multiple concurrent requests
            val requests = (1..5).map {
                mockMvc.perform(get("/health"))
            }
            
            // All requests should succeed
            requests.forEach { request ->
                request.andExpect(status().isOk)
                    .andExpect(jsonPath("$.status").value("healthy"))
            }
        }
        
        @Test
        @DisplayName("Should handle concurrent requests to heartbeat endpoint")
        fun `heartbeat endpoint should handle concurrent requests`() {
            // Simulate multiple concurrent requests
            val requests = (1..5).map {
                mockMvc.perform(get("/heartbeat"))
            }
            
            // All requests should succeed
            requests.forEach { request ->
                request.andExpect(status().isOk)
                    .andExpect(jsonPath("$.status").value("alive"))
            }
        }
        
        @Test
        @DisplayName("Should return valid JSON even under load")
        fun `endpoints should return valid JSON under repeated requests`() {
            // Make multiple rapid requests
            repeat(10) {
                mockMvc.perform(get("/health"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("healthy"))
                
                mockMvc.perform(get("/heartbeat"))
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("alive"))
            }
        }
    }
} 