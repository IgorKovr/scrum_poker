/**
 * vitest.config.ts - Vitest Configuration for Frontend Testing
 * 
 * This configuration sets up Vitest for testing React components and utilities.
 * It configures the testing environment, global setup, and necessary imports.
 */

import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test-setup.ts'],
  },
}); 