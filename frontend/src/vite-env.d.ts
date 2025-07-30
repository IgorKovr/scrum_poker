/**
 * vite-env.d.ts - Vite Environment Type Declarations
 * 
 * This file provides TypeScript type definitions for Vite-specific features
 * and environment variables. It ensures type safety when accessing Vite's
 * import.meta.env object and related functionality.
 * 
 * Key Features:
 * 1. Imports Vite's built-in type definitions
 * 2. Extends the ImportMetaEnv interface with custom environment variables
 * 3. Provides type safety for environment variable access
 * 
 * Environment Variables:
 * - VITE_WS_URL: Custom WebSocket URL override (optional)
 * - VITE_API_URL: Custom API endpoint URL override (optional)
 * 
 * These variables allow runtime configuration without code changes,
 * particularly useful for different deployment environments.
 * 
 * Usage Example:
 * const wsUrl = import.meta.env.VITE_WS_URL || getWebSocketUrl();
 */

/// <reference types="vite/client" />

/**
 * Extended environment variables interface
 * 
 * Defines the shape of environment variables available through Vite's
 * import.meta.env object. All properties are readonly to prevent
 * accidental modification of environment configuration.
 * 
 * Environment variables with VITE_ prefix are exposed to the client-side
 * code and can be accessed at runtime. Other variables remain server-side only.
 */
interface ImportMetaEnv {
  /** Optional WebSocket URL override for different environments */
  readonly VITE_WS_URL: string
  
  /** Optional API URL override for different environments */
  readonly VITE_API_URL: string
}

/**
 * Extended ImportMeta interface
 * 
 * Enhances the global ImportMeta interface with our custom environment
 * variable definitions. This ensures TypeScript recognizes the env property
 * and provides proper autocompletion and type checking.
 */
interface ImportMeta {
  /** Environment variables object with type-safe property access */
  readonly env: ImportMetaEnv
} 