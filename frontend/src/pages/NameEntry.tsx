/**
 * NameEntry.tsx - Landing Page with Room Management
 *
 * This component provides the initial landing page where users can:
 * 1. Create a new room with a unique animal name
 * 2. Join an existing room by entering its name
 *
 * Key Features:
 * 1. Two primary actions: Create Room and Join Room
 * 2. Form validation using React Hook Form
 * 3. Modal dialog for joining existing rooms
 * 4. Automatic animal name generation (e.g., "happy-panda")
 * 5. Clean, intuitive user interface
 *
 * User Flows:
 * - Create Room: Enter name → Click Create → Navigate to new room
 * - Join Room: Enter name → Click Join → Enter room name → Navigate to room
 */

import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useLocation, useNavigate } from "react-router-dom";

/**
 * Props interface for the NameEntry component
 */
interface NameEntryProps {
  /** Callback function called when user successfully submits their name */
  onNameSubmit: (name: string) => void;
}

/**
 * Form data interfaces for type-safe form handling
 */
interface NameFormData {
  /** The user's display name input */
  name: string;
}

interface JoinRoomFormData {
  /** The room name to join */
  roomCode: string;
}

/**
 * Lists of adjectives and animals for generating room names
 */
const adjectives = [
  "happy",
  "brave",
  "clever",
  "gentle",
  "swift",
  "mighty",
  "calm",
  "bright",
  "wise",
  "jolly",
  "proud",
  "fierce",
  "noble",
  "quiet",
  "wild",
  "bold",
  "quick",
  "strong",
  "agile",
  "graceful",
  "loyal",
  "keen",
  "cheerful",
  "daring",
  "eager",
  "fearless",
  "friendly",
  "generous",
  "honest",
  "kind",
  "lively",
  "merry",
];

const animals = [
  "panda",
  "tiger",
  "lion",
  "elephant",
  "giraffe",
  "zebra",
  "koala",
  "kangaroo",
  "dolphin",
  "whale",
  "eagle",
  "hawk",
  "owl",
  "penguin",
  "fox",
  "wolf",
  "bear",
  "deer",
  "rabbit",
  "otter",
  "seal",
  "turtle",
  "falcon",
  "raven",
  "leopard",
  "cheetah",
  "jaguar",
  "lynx",
  "bison",
  "moose",
  "badger",
  "raccoon",
];

/**
 * Generates a unique room name using adjective-animal combination
 * @returns A string like "happy-panda" or "brave-tiger"
 */
const generateRoomCode = (): string => {
  const adjective = adjectives[Math.floor(Math.random() * adjectives.length)];
  const animal = animals[Math.floor(Math.random() * animals.length)];
  return `${adjective}-${animal}`;
};

/**
 * NameEntry Component - Landing page with room management options
 */
export const NameEntry: React.FC<NameEntryProps> = ({ onNameSubmit }) => {
  // State for showing/hiding the join room modal
  const [showJoinModal, setShowJoinModal] = useState(false);

  // React Hook Form for name input
  const {
    register: registerName,
    handleSubmit: handleNameSubmit,
    formState: { errors: nameErrors },
    watch,
    setValue,
  } = useForm<NameFormData>();

  // React Hook Form for join room modal
  const {
    register: registerJoin,
    handleSubmit: handleJoinSubmit,
    formState: { errors: joinErrors },
    reset: resetJoinForm,
  } = useForm<JoinRoomFormData>();

  // React Router navigation and location
  const navigate = useNavigate();
  const location = useLocation();

  // Watch the name field to enable/disable buttons
  const watchedName = watch("name");

  // Get room ID from location state (if redirected from a direct room link)
  const redirectRoomId = (location.state as { roomId?: string })?.roomId;

  // Check for existing user session and auto-join if applicable
  useEffect(() => {
    const existingUserName = localStorage.getItem("scrumPokerUserName");

    if (existingUserName) {
      // Pre-fill the name field
      setValue("name", existingUserName);

      // If there's a room to join, auto-join immediately
      if (redirectRoomId) {
        onNameSubmit(existingUserName);
        navigate(`/room/${redirectRoomId}`);
      }
    }
  }, [redirectRoomId, onNameSubmit, navigate, setValue]);

  /**
   * Handles creating a new room
   */
  const handleCreateRoom = (data: NameFormData) => {
    onNameSubmit(data.name);
    const roomCode = generateRoomCode();
    navigate(`/room/${roomCode}`);
  };

  /**
   * Handles auto-joining when redirected from a direct room link
   */
  const handleAutoJoin = (data: NameFormData) => {
    if (redirectRoomId) {
      onNameSubmit(data.name);
      navigate(`/room/${redirectRoomId}`);
    }
  };

  /**
   * Opens the join room modal
   */
  const handleJoinRoomClick = (data: NameFormData) => {
    onNameSubmit(data.name);
    setShowJoinModal(true);
  };

  /**
   * Handles Enter key press - creates room or joins based on context
   */
  const handleEnterKeySubmit = (data: NameFormData) => {
    if (redirectRoomId) {
      // If invited to a room, Enter key joins the room
      handleAutoJoin(data);
    } else {
      // If no room invite, Enter key creates a new room
      handleCreateRoom(data);
    }
  };

  /**
   * Handles joining an existing room
   */
  const handleJoinRoom = (data: JoinRoomFormData) => {
    navigate(`/room/${data.roomCode}`);
  };

  /**
   * Closes the join room modal
   */
  const handleCloseModal = () => {
    setShowJoinModal(false);
    resetJoinForm();
  };

  return (
    <>
      <div className="min-h-screen flex items-center justify-center px-4">
        <div className="max-w-md w-full space-y-8">
          {/* Header section */}
          <div className="text-center">
            <h1 className="text-4xl font-bold text-gray-900 dark:text-dark-text mb-2">
              Scrum Poker
            </h1>
            <p className="text-lg text-gray-600 dark:text-dark-text-secondary">
              {redirectRoomId
                ? `Join Room ${redirectRoomId}`
                : "Start or join a planning session"}
            </p>
          </div>

          {/* Name input form */}
          <div className="mt-8 space-y-6">
            {/* Show existing user indicator if logged in */}
            {watchedName &&
              watchedName === localStorage.getItem("scrumPokerUserName") && (
                <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-700 rounded-lg p-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <svg
                        className="w-5 h-5 text-green-600 dark:text-green-400 mr-2"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                      <span className="text-sm text-green-800 dark:text-green-300">
                        Logged in as{" "}
                        <span className="font-semibold">{watchedName}</span>
                      </span>
                    </div>
                    <button
                      type="button"
                      onClick={() => {
                        localStorage.removeItem("scrumPokerUserName");
                        setValue("name", "");
                      }}
                      className="text-xs text-green-700 dark:text-green-400 hover:text-green-900 dark:hover:text-green-200 underline"
                    >
                      Change user
                    </button>
                  </div>
                </div>
              )}

            <form onSubmit={handleNameSubmit(handleEnterKeySubmit)}>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-dark-text mb-2">
                  Your Name
                </label>
                <input
                  {...registerName("name", {
                    required: "Name is required",
                    minLength: {
                      value: 1,
                      message: "Name must be at least 1 character",
                    },
                  })}
                  type="text"
                  placeholder="Enter your display name"
                  className="appearance-none relative block w-full px-3 py-3 border border-gray-300 dark:border-dark-border placeholder-gray-500 dark:placeholder-dark-text-secondary text-gray-900 dark:text-dark-text bg-white dark:bg-dark-surface rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm transition-colors duration-200"
                />
                {nameErrors.name && (
                  <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                    {nameErrors.name.message}
                  </p>
                )}
              </div>
            </form>

            {/* Action buttons */}
            <div className="space-y-3">
              {redirectRoomId ? (
                /* Auto-join button when redirected from a room link */
                <>
                  <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-700 rounded-lg p-3 mb-3">
                    <p className="text-sm text-blue-800 dark:text-blue-300 text-center">
                      You've been invited to room{" "}
                      <span className="font-mono font-bold">
                        {redirectRoomId}
                      </span>
                    </p>
                  </div>
                  <button
                    onClick={handleNameSubmit(handleAutoJoin)}
                    disabled={!watchedName}
                    className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 dark:bg-blue-700 dark:hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-dark-bg focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <svg
                      className="w-5 h-5 mr-2"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
                      />
                    </svg>
                    Join Room
                  </button>
                </>
              ) : (
                /* Normal create/join buttons */
                <>
                  {/* Create Room button */}
                  <button
                    onClick={handleNameSubmit(handleCreateRoom)}
                    disabled={!watchedName}
                    className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 dark:bg-blue-700 dark:hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-dark-bg focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <svg
                      className="w-5 h-5 mr-2"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 4v16m8-8H4"
                      />
                    </svg>
                    Create New Room
                  </button>

                  {/* Join Room button */}
                  <button
                    onClick={handleNameSubmit(handleJoinRoomClick)}
                    disabled={!watchedName}
                    className="group relative w-full flex justify-center py-3 px-4 border border-gray-300 dark:border-dark-border text-sm font-medium rounded-md text-gray-700 dark:text-dark-text bg-white dark:bg-dark-surface hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 dark:focus:ring-offset-dark-bg focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <svg
                      className="w-5 h-5 mr-2"
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
                    Join Existing Room
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Join Room Modal */}
      {showJoinModal && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 dark:bg-opacity-90 transition-opacity z-50">
          <div className="fixed inset-0 z-50 overflow-y-auto">
            <div className="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
              <div className="relative transform overflow-hidden rounded-lg bg-white dark:bg-dark-surface text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-sm">
                <form onSubmit={handleJoinSubmit(handleJoinRoom)}>
                  <div className="bg-white dark:bg-dark-surface px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
                    <div className="text-center">
                      <h3 className="text-lg font-semibold leading-6 text-gray-900 dark:text-dark-text">
                        Join Room
                      </h3>
                      <div className="mt-4">
                        <label className="block text-sm font-medium text-gray-700 dark:text-dark-text-secondary mb-2">
                          Enter room name
                        </label>
                        <input
                          {...registerJoin("roomCode", {
                            required: "Room name is required",
                            pattern: {
                              value: /^[a-z]+-[a-z]+$/,
                              message:
                                "Room name must be in format: word-word (e.g., happy-panda)",
                            },
                          })}
                          type="text"
                          placeholder="happy-panda"
                          className="appearance-none relative block w-full px-3 py-3 border border-gray-300 dark:border-dark-border placeholder-gray-500 dark:placeholder-dark-text-secondary text-gray-900 dark:text-dark-text bg-white dark:bg-dark-surface rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 text-center text-lg font-mono transition-colors duration-200"
                        />
                        {joinErrors.roomCode && (
                          <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                            {joinErrors.roomCode.message}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                    <button
                      type="submit"
                      className="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 dark:bg-blue-700 dark:hover:bg-blue-800 sm:ml-3 sm:w-auto transition-colors duration-200"
                    >
                      Join
                    </button>
                    <button
                      type="button"
                      onClick={handleCloseModal}
                      className="mt-3 inline-flex w-full justify-center rounded-md bg-white dark:bg-dark-surface px-3 py-2 text-sm font-semibold text-gray-900 dark:text-dark-text shadow-sm ring-1 ring-inset ring-gray-300 dark:ring-dark-border hover:bg-gray-50 dark:hover:bg-gray-600 sm:mt-0 sm:w-auto transition-colors duration-200"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
