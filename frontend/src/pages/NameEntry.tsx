/**
 * NameEntry.tsx - User Name Entry Page Component
 *
 * This component provides the initial landing page where users enter their display name
 * before joining a Scrum Poker room. It serves as a gateway to ensure all users
 * have identifiable names in the poker session.
 *
 * Key Features:
 * 1. Form validation using React Hook Form for robust input handling
 * 2. Clean, centered layout with clear instructions
 * 3. Input validation with error messages
 * 4. Automatic navigation to poker room after name submission
 * 5. Cancel functionality for navigation flexibility
 *
 * User Flow:
 * 1. User enters their display name in the text input
 * 2. Form validates the input (required, minimum length)
 * 3. On submission, the name is passed to parent component
 * 4. User is automatically navigated to the default poker room
 *
 * The component uses a fixed room ID ('default-room') for simplicity,
 * but could be enhanced to allow custom room selection.
 */

import React from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";

/**
 * Props interface for the NameEntry component
 * Defines the callback function for when a user submits their name
 */
interface NameEntryProps {
  /** Callback function called when user successfully submits their name */
  onNameSubmit: (name: string) => void;
}

/**
 * Form data interface for type-safe form handling
 * Ensures TypeScript knows the structure of form data
 */
interface FormData {
  /** The user's display name input */
  name: string;
}

/**
 * NameEntry Component - Landing page for user name collection
 *
 * This functional component renders a form where users enter their display name
 * before joining a poker room. It uses React Hook Form for validation and
 * React Router for navigation.
 *
 * @param {NameEntryProps} props - Component props containing name submission callback
 * @returns {JSX.Element} The name entry form with validation and navigation
 */
export const NameEntry: React.FC<NameEntryProps> = ({ onNameSubmit }) => {
  // React Hook Form setup for form state management and validation
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>();

  // React Router navigation hook for programmatic routing
  const navigate = useNavigate();

  /**
   * Form submission handler
   *
   * Called when the form is successfully submitted (passes validation).
   * Updates the parent component with the user's name and navigates
   * to the poker room.
   *
   * @param {FormData} data - The validated form data containing user's name
   */
  const onSubmit = (data: FormData) => {
    // Pass the name to parent component (App.tsx) to update global state
    onNameSubmit(data.name);

    // Generate a random room ID or use a predefined one
    // TODO: Could be enhanced to allow user-specified room names
    const roomId = "default-room";

    // Navigate to the poker room with the specified room ID
    navigate(`/room/${roomId}`);
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="max-w-md w-full space-y-8">
        {/* Header section with title and instructions */}
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-dark-text">
            Enter Room
          </h1>
          <p className="mt-2 text-sm text-gray-600 dark:text-dark-text-secondary">
            Provide your name or any pseudonym to enter the scrum poker room
          </p>
        </div>

        {/* Main form with validation and submission handling */}
        <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-6">
          <div>
            {/* Name input field with validation */}
            <input
              {...register("name", {
                required: "Name is required",
                minLength: {
                  value: 1,
                  message: "Name must be at least 1 character",
                },
              })}
              type="text"
              placeholder="Display Name*"
              className="appearance-none relative block w-full px-3 py-3 border border-gray-300 dark:border-dark-border placeholder-gray-500 dark:placeholder-dark-text-secondary text-gray-900 dark:text-dark-text bg-white dark:bg-dark-surface rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm transition-colors duration-200"
            />
            {/* Error message display for validation failures */}
            {errors.name && (
              <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                {errors.name.message}
              </p>
            )}
          </div>

          {/* Action button */}
          <div>
            {/* Primary submit button */}
            <button
              type="submit"
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 dark:bg-blue-700 dark:hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-dark-bg focus:ring-blue-500 transition-colors duration-200"
            >
              ENTER ROOM
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
