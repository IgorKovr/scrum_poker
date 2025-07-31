/**
 * App.tsx - Main React Application Component
 *
 * This is the root component of the Scrum Poker frontend application.
 * It manages the global application state and routing between different pages.
 *
 * Key Responsibilities:
 * 1. Manages user authentication state (userName and userId)
 * 2. Provides client-side routing between the name entry and poker room pages
 * 3. Implements route protection to ensure users enter their name before accessing rooms
 * 4. Establishes the main layout structure with consistent styling
 *
 * State Management:
 * - userName: Stores the display name entered by the user
 * - userId: Stores the unique identifier assigned by the backend WebSocket service
 *
 * Routing Structure:
 * - "/" : Name entry page where users provide their display name
 * - "/room/:roomId" : Protected poker room page where estimation happens
 *
 * The app uses React Router for navigation and ensures users cannot access
 * the poker room without first providing their name.
 */

import { useState } from "react";
import {
  Navigate,
  Route,
  BrowserRouter as Router,
  Routes,
} from "react-router-dom";
import { NameEntry } from "./pages/NameEntry";
import { PokerRoom } from "./pages/PokerRoom";

/**
 * Main Application Component
 *
 * Manages global state and routing for the entire Scrum Poker application.
 * Uses React hooks for state management and React Router for navigation.
 *
 * @returns {JSX.Element} The complete application with routing and layout
 */
function App() {
  // Global state for user information
  const [userName, setUserName] = useState<string>(""); // User's display name entered on the welcome screen
  const [userId, setUserId] = useState<string>(""); // Unique ID assigned by backend when joining a room

  return (
    <Router>
      {/* Main application container with full height and consistent background */}
      <div className="min-h-screen bg-gray-50 dark:bg-dark-bg transition-colors duration-200">
        <Routes>
          {/* Home Route - Name Entry Page */}
          <Route
            path="/"
            element={<NameEntry onNameSubmit={(name) => setUserName(name)} />}
          />

          {/* Protected Route - Poker Room Page */}
          {/* Users can only access this if they have provided a name */}
          <Route
            path="/room/:roomId"
            element={
              userName ? (
                // User has provided name - show poker room
                <PokerRoom
                  userName={userName}
                  userId={userId}
                  setUserId={setUserId}
                />
              ) : (
                // User hasn't provided name - redirect to home
                <Navigate to="/" replace />
              )
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
