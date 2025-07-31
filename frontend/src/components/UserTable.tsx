/**
 * UserTable.tsx - User Status and Estimation Display Component
 *
 * This component displays a table of all users in the poker room along with
 * their voting status and estimates (when revealed). It also provides controls
 * for managing the estimation session.
 *
 * Key Features:
 * 1. Displays all participants with their names and voting status
 * 2. Shows estimates when revealed, or voting indicators when hidden
 * 3. Provides session management controls (Show/Hide/Delete estimates)
 * 4. Highlights the current user's row for easy identification
 * 5. Responsive table design with proper mobile support
 * 6. Visual voting status indicators (checkmark for voted, dash for not voted)
 *
 * States Displayed:
 * - Not Voted: Shows a dash (-) in gray
 * - Voted (Hidden): Shows a green checkmark icon
 * - Voted (Revealed): Shows the actual estimate value in a blue badge
 *
 * Controls:
 * - Show/Hide: Toggles visibility of all estimates
 * - Delete Estimates: Clears all votes and resets the session
 *
 * The component uses icons from Heroicons for visual indicators and
 * maintains consistent styling with the rest of the application.
 */

import React from "react";
import { User } from "../types";

/**
 * Props interface for the UserTable component
 * Defines all the required properties and callback functions
 */
interface UserTableProps {
  /** Array of all users currently in the poker room */
  users: User[];

  /** Whether estimates are currently visible to all users */
  showEstimates: boolean;

  /** Callback function to reveal all estimates */
  onShowEstimates: () => void;

  /** Callback function to hide all estimates */
  onHideEstimates: () => void;

  /** Callback function to delete all estimates and reset voting */
  onDeleteEstimates: () => void;

  /** Current user's ID for highlighting their row */
  currentUserId: string;
}

/**
 * UserTable Component - Displays poker session participants and controls
 *
 * This component renders a comprehensive view of the poker session state,
 * showing all participants, their voting status, and providing controls
 * for session management. The table adapts its display based on whether
 * estimates are currently shown or hidden.
 *
 * The component follows the Planning Poker methodology where:
 * 1. Users submit their estimates privately
 * 2. Voting status is visible but estimates are hidden
 * 3. Once all have voted, estimates can be revealed simultaneously
 * 4. The session can be reset for a new round of estimation
 *
 * @param {UserTableProps} props - Component props containing users, state, and callbacks
 * @returns {JSX.Element} A table displaying users and session controls
 */
export const UserTable: React.FC<UserTableProps> = ({
  users,
  showEstimates,
  onShowEstimates,
  onHideEstimates,
  onDeleteEstimates,
  currentUserId,
}) => {
  return (
    <div>
      {/* Header section with participant count and session controls */}
      <div className="flex items-center gap-4 mb-4">
        {/* Participant counter with people icon */}
        <div className="flex items-center gap-2 text-gray-600 dark:text-dark-text-secondary">
          {/* Heroicons people icon */}
          <svg
            className="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
            />
          </svg>
          <span className="text-sm">
            {users.length} participant{users.length !== 1 ? "s" : ""}
          </span>
        </div>

        {/* Session control buttons */}
        <div className="flex gap-2 ml-auto">
          {/* Delete estimates button - clears all votes */}
          <button
            onClick={onDeleteEstimates}
            className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-dark-text bg-white dark:bg-dark-surface border border-gray-300 dark:border-dark-border rounded-md hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-dark-bg focus:ring-blue-500 transition-colors duration-200"
          >
            Delete Estimates
          </button>

          {/* Show/Hide estimates toggle button */}
          {showEstimates ? (
            <button
              onClick={onHideEstimates}
              className="px-4 py-2 text-sm font-medium text-white bg-gray-600 dark:bg-gray-700 rounded-md hover:bg-gray-700 dark:hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-dark-bg focus:ring-gray-500 transition-colors duration-200"
            >
              Hide
            </button>
          ) : (
            <button
              onClick={onShowEstimates}
              className="px-4 py-2 text-sm font-medium text-white bg-gray-600 dark:bg-gray-700 rounded-md hover:bg-gray-700 dark:hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-dark-bg focus:ring-gray-500 transition-colors duration-200"
            >
              Show
            </button>
          )}
        </div>
      </div>

      {/* Main table displaying users and their estimates */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-dark-border">
          {/* Table header */}
          <thead className="bg-gray-50 dark:bg-dark-surface">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-dark-text-secondary uppercase tracking-wider">
                Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-dark-text-secondary uppercase tracking-wider">
                Story Points
              </th>
            </tr>
          </thead>

          {/* Table body with user rows */}
          <tbody className="bg-white dark:bg-dark-bg divide-y divide-gray-200 dark:divide-dark-border">
            {users.map((user) => (
              <tr
                key={user.id}
                className={
                  user.id === currentUserId
                    ? "bg-blue-50 dark:bg-blue-900/20"
                    : ""
                }
              >
                {/* User name column */}
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900 dark:text-dark-text">
                    {user.name}
                    {/* "You" indicator for current user */}
                    {user.id === currentUserId && (
                      <span className="ml-2 text-xs text-blue-600 dark:text-blue-400">
                        (You)
                      </span>
                    )}
                  </div>
                </td>

                {/* Estimate/status column */}
                <td className="px-6 py-4 whitespace-nowrap">
                  {/* Logic for displaying estimate vs voting status */}
                  {showEstimates && user.estimate ? (
                    <div className="inline-flex items-center justify-center w-12 h-12 text-lg font-bold text-blue-600 dark:text-blue-300 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                      {user.estimate}
                    </div>
                  ) : user.hasVoted ? (
                    <div className="inline-flex items-center justify-center w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-lg">
                      {/* Heroicons checkmark icon */}
                      <svg
                        className="w-6 h-6 text-green-600 dark:text-green-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                    </div>
                  ) : (
                    <div className="text-2xl text-gray-400 dark:text-dark-text-secondary">
                      -
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
