/**
 * main.tsx - Application Entry Point
 * 
 * This file serves as the main entry point for the React application.
 * It's responsible for:
 * 1. Initializing React with StrictMode for development warnings
 * 2. Setting up React Query for server state management (though not actively used yet)
 * 3. Mounting the main App component to the DOM
 * 4. Importing global CSS styles
 * 
 * The file uses Vite's development server for hot module replacement during development.
 * React.StrictMode helps identify potential problems in the application during development.
 * 
 * React Query is included for potential future server state management needs,
 * though the current WebSocket-based architecture doesn't heavily utilize it.
 */

import React from 'react'
import ReactDOM from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App'
import './index.css'

/**
 * Initialize React Query client for server state management
 * 
 * React Query provides powerful data synchronization for React apps.
 * Currently included for future extensibility, though the app primarily
 * uses WebSocket connections for real-time communication.
 */
const queryClient = new QueryClient()

/**
 * Mount the React application to the DOM
 * 
 * The application is wrapped in:
 * 1. React.StrictMode - Enables additional checks and warnings in development
 * 2. QueryClientProvider - Provides React Query context to all child components
 * 
 * The app is mounted to the element with id='root' in index.html
 */
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>,
) 