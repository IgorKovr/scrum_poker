/**
 * test-setup.ts - Global Test Configuration
 * 
 * This file sets up global test utilities and matchers for all test files.
 * It's automatically loaded before each test suite runs.
 */

import '@testing-library/jest-dom';

// Setup global vi function
import { vi } from 'vitest';

// Global test setup
beforeEach(() => {
  // Clear any previous test artifacts
  document.body.innerHTML = '';
});

// Mock console methods to avoid noise in test output
global.console = {
  ...console,
  log: vi.fn(),
  error: vi.fn(),
  warn: vi.fn(),
};

// Mock window.location for tests that need it
Object.defineProperty(window, 'location', {
  value: {
    hostname: 'localhost',
    protocol: 'http:',
    host: 'localhost:3000',
    href: 'http://localhost:3000/',
    origin: 'http://localhost:3000',
    pathname: '/',
    search: '',
    hash: '',
  },
  writable: true,
});

// Setup global vi function
import { vi } from 'vitest'; 