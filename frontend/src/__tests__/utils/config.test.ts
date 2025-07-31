/**
 * config.test.ts - Unit Tests for Configuration Utilities
 * 
 * This test suite validates the configuration functions used throughout
 * the application, particularly the WebSocket URL generation logic.
 * 
 * Test Coverage:
 * 1. Local development WebSocket URL generation
 * 2. Production WebSocket URL generation
 * 3. Protocol selection (ws vs wss)
 * 4. Hostname detection logic
 * 5. Edge cases and different environments
 * 
 * Testing Approach:
 * - Mocks window.location to simulate different environments
 * - Tests both HTTP and HTTPS scenarios
 * - Validates URL generation for various hostnames
 * - Ensures proper protocol selection
 */

import { describe, it, expect, afterEach } from 'vitest';
import { getWebSocketUrl } from '../../config';

describe('Configuration Utilities', () => {
  // Store original window.location
  const originalLocation = window.location;

  // Helper function to mock window.location
  const mockLocation = (location: Partial<Location>) => {
    Object.defineProperty(window, 'location', {
      value: { ...originalLocation, ...location },
      writable: true,
    });
  };

  afterEach(() => {
    // Restore original location
    Object.defineProperty(window, 'location', {
      value: originalLocation,
      writable: true,
    });
  });

  describe('getWebSocketUrl', () => {
    describe('Local Development Environment', () => {
      it('should return localhost WebSocket URL for localhost hostname', () => {
        mockLocation({
          hostname: 'localhost',
          protocol: 'http:',
          host: 'localhost:3000'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should return localhost WebSocket URL for 127.0.0.1 hostname', () => {
        mockLocation({
          hostname: '127.0.0.1',
          protocol: 'http:',
          host: '127.0.0.1:3000'
        });

        const url = getWebSocketUrl();

                 expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should use ws protocol for HTTP on localhost', () => {
        mockLocation({
          hostname: 'localhost',
          protocol: 'http:',
          host: 'localhost:3000'
        });

        const url = getWebSocketUrl();

        expect(url).toMatch(/^ws:/);
        expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should use wss protocol for HTTPS on localhost', () => {
        mockLocation({
          hostname: 'localhost',
          protocol: 'https:',
          host: 'localhost:3000'
        });

        const url = getWebSocketUrl();

        expect(url).toMatch(/^wss:/);
        expect(url).toBe('wss://localhost:3000/ws');
      });
    });

    describe('Production Environment', () => {
      it('should return production WebSocket URL for production hostname', () => {
        mockLocation({
          hostname: 'scrum-poker.railway.app',
          protocol: 'https:',
          host: 'scrum-poker.railway.app'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('wss://scrum-poker.railway.app/ws');
      });

      it('should use wss protocol for HTTPS in production', () => {
        mockLocation({
          hostname: 'myapp.com',
          protocol: 'https:',
          host: 'myapp.com'
        });

        const url = getWebSocketUrl();

        expect(url).toMatch(/^wss:/);
        expect(url).toBe('wss://myapp.com/ws');
      });

      it('should use ws protocol for HTTP in production', () => {
        mockLocation({
          hostname: 'staging.myapp.com',
          protocol: 'http:',
          host: 'staging.myapp.com:8080'
        });

        const url = getWebSocketUrl();

        expect(url).toMatch(/^ws:/);
        expect(url).toBe('ws://staging.myapp.com:8080/ws');
      });

      it('should handle custom ports in production', () => {
        mockLocation({
          hostname: 'custom-domain.com',
          protocol: 'https:',
          host: 'custom-domain.com:443'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('wss://custom-domain.com:443/ws');
      });
    });

    describe('Protocol Selection', () => {
      it('should select ws protocol for http pages', () => {
        mockLocation({
          hostname: 'example.com',
          protocol: 'http:'
        });

        const url = getWebSocketUrl();

        expect(url).toMatch(/^ws:/);
      });

      it('should select wss protocol for https pages', () => {
        mockLocation({
          hostname: 'example.com',
          protocol: 'https:'
        });

        const url = getWebSocketUrl();

        expect(url).toMatch(/^wss:/);
      });
    });

    describe('Environment Detection', () => {
      it('should detect localhost as local environment', () => {
        mockLocation({
          hostname: 'localhost',
          protocol: 'http:',
          host: 'localhost:3000'
        });

        const url = getWebSocketUrl();

        // Should use localhost:3000 not the original host
        expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should detect 127.0.0.1 as local environment', () => {
        mockLocation({
          hostname: '127.0.0.1',
          protocol: 'http:',
          host: '127.0.0.1:5173'
        });

        const url = getWebSocketUrl();

                 // Should use localhost:3000 for local development
         expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should treat any other hostname as production', () => {
        mockLocation({
          hostname: 'app.example.com',
          protocol: 'https:',
          host: 'app.example.com'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('wss://app.example.com/ws');
      });

      it('should handle IP addresses as production', () => {
        mockLocation({
          hostname: '192.168.1.100',
          protocol: 'http:',
          host: '192.168.1.100:8080'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('ws://192.168.1.100:8080/ws');
      });
    });

    describe('Edge Cases', () => {
      it('should handle missing port in localhost', () => {
        mockLocation({
          hostname: 'localhost',
          protocol: 'http:',
          host: 'localhost'
        });

        const url = getWebSocketUrl();

                 expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should handle non-standard ports', () => {
        mockLocation({
          hostname: 'localhost',
          protocol: 'http:',
          host: 'localhost:8080'
        });

        const url = getWebSocketUrl();

                 expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should handle subdomain hostnames', () => {
        mockLocation({
          hostname: 'api.staging.myapp.com',
          protocol: 'https:',
          host: 'api.staging.myapp.com'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('wss://api.staging.myapp.com/ws');
      });

      it('should always append /ws path', () => {
        const testCases = [
          { hostname: 'localhost', protocol: 'http:', host: 'localhost:3000' },
          { hostname: 'production.com', protocol: 'https:', host: 'production.com' },
          { hostname: '127.0.0.1', protocol: 'http:', host: '127.0.0.1:5173' },
        ];

        testCases.forEach(({ hostname, protocol, host }) => {
          mockLocation({ hostname, protocol, host });
          const url = getWebSocketUrl();
          expect(url).toMatch(/\/ws$/);
        });
      });
    });

    describe('Real-world Scenarios', () => {
      it('should work for Railway deployment', () => {
        mockLocation({
          hostname: 'scrum-poker-production.railway.app',
          protocol: 'https:',
          host: 'scrum-poker-production.railway.app'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('wss://scrum-poker-production.railway.app/ws');
      });

      it('should work for Vercel deployment', () => {
        mockLocation({
          hostname: 'scrum-poker.vercel.app',
          protocol: 'https:',
          host: 'scrum-poker.vercel.app'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('wss://scrum-poker.vercel.app/ws');
      });

      it('should work for Heroku deployment', () => {
        mockLocation({
          hostname: 'my-scrum-poker.herokuapp.com',
          protocol: 'https:',
          host: 'my-scrum-poker.herokuapp.com'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('wss://my-scrum-poker.herokuapp.com/ws');
      });

      it('should work for development server on different port', () => {
        mockLocation({
          hostname: 'localhost',
          protocol: 'http:',
          host: 'localhost:5173'
        });

        const url = getWebSocketUrl();

                 expect(url).toBe('ws://localhost:3000/ws');
      });

      it('should work for local network testing', () => {
        mockLocation({
          hostname: '192.168.1.10',
          protocol: 'http:',
          host: '192.168.1.10:3000'
        });

        const url = getWebSocketUrl();

        expect(url).toBe('ws://192.168.1.10:3000/ws');
      });
    });
  });
}); 