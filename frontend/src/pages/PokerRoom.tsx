import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { PokerCard } from "../components/PokerCard";
import { UserTable } from "../components/UserTable";
import { getWebSocketUrl } from "../config";
import { wsService } from "../services/websocket";
import {
  FIBONACCI_VALUES,
  MessageType,
  RoomState,
  SPECIAL_VALUES,
} from "../types";

interface PokerRoomProps {
  userName: string;
  userId: string;
  setUserId: (id: string) => void;
}

export const PokerRoom: React.FC<PokerRoomProps> = ({
  userName,
  userId,
  setUserId,
}) => {
  const { roomId } = useParams<{ roomId: string }>();
  const [roomState, setRoomState] = useState<RoomState | null>(null);
  const [selectedCard, setSelectedCard] = useState<string | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [connectionError, setConnectionError] = useState<string | null>(null);
  const [connectionAttempts, setConnectionAttempts] = useState(0);
  const [showError, setShowError] = useState(false);
  const [retryTrigger, setRetryTrigger] = useState(0);
  const [autoRetryEnabled, setAutoRetryEnabled] = useState(true);
  const [retryCountdown, setRetryCountdown] = useState(0);
  const [totalRetryAttempts, setTotalRetryAttempts] = useState(0);
  const [showCopiedNotification, setShowCopiedNotification] = useState(false);
  const [showReconnectingBanner, setShowReconnectingBanner] = useState(false);

  // Store room context in localStorage for recovery after disconnection
  useEffect(() => {
    if (roomId && userName) {
      localStorage.setItem(
        "scrumPokerSession",
        JSON.stringify({ roomId, userName })
      );
    }
  }, [roomId, userName]);

  // Sync selected card with room state (for multi-tab support)
  useEffect(() => {
    if (userId && roomState?.users) {
      const currentUser = roomState.users.find((u) => u.id === userId);
      if (currentUser) {
        // Sync card selection across tabs
        if (currentUser.hasVoted && currentUser.estimate) {
          setSelectedCard(currentUser.estimate);
        } else if (!currentUser.hasVoted) {
          setSelectedCard(null);
        }
      }
    }
  }, [userId, roomState]);

  // Page Visibility API - Auto-reconnect when tab becomes visible
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible") {
        console.log("[PokerRoom] Tab became visible");

        // Check if we're disconnected
        if (!wsService.isConnected()) {
          console.log(
            "[PokerRoom] Detected disconnection, attempting to reconnect..."
          );
          setShowReconnectingBanner(true);

          // Attempt to reconnect after a short delay
          setTimeout(() => {
            handleRetryConnection();
          }, 500);
        }
      }
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, []);

  useEffect(() => {
    const connectWebSocket = async () => {
      // Minimum loading time before showing error
      const minLoadingTime = 2000; // 2 seconds
      const startTime = Date.now();

      try {
        setConnectionError(null);
        setShowError(false);
        setConnectionAttempts((prev) => prev + 1);

        // Set a timeout to show error if connection takes too long
        const timeoutId = setTimeout(() => {
          if (!wsService.isConnected()) {
            setConnectionError(
              "Connection timeout. The backend service might be down or starting up."
            );
            // Only show error after minimum loading time
            const elapsed = Date.now() - startTime;
            if (elapsed >= minLoadingTime) {
              setShowError(true);
            } else {
              setTimeout(() => setShowError(true), minLoadingTime - elapsed);
            }
          }
        }, 10000); // 10 seconds timeout

        // Use the configured WebSocket URL
        const wsUrl = getWebSocketUrl();
        console.log("[PokerRoom] WebSocket URL:", wsUrl);
        console.log("[PokerRoom] Connecting to room:", roomId);

        await wsService.connect(wsUrl);
        clearTimeout(timeoutId);
        setIsConnected(true);
        setConnectionError(null);
        setShowReconnectingBanner(false);
        console.log("[PokerRoom] WebSocket connected successfully");

        // Set up message handlers
        wsService.on(MessageType.JOIN, (payload) => {
          if (payload.userId) {
            setUserId(payload.userId);
            // Store userId in localStorage with room key for tab sharing
            const storageKey = `scrumPokerUserId_${roomId}_${userName}`;
            localStorage.setItem(storageKey, payload.userId);
          }
        });

        wsService.on(MessageType.ROOM_UPDATE, (payload) => {
          setRoomState(payload);
          
          // Sync selected card across tabs: find current user's vote
          if (userId && payload.users) {
            const currentUser = payload.users.find((u: any) => u.id === userId);
            if (currentUser) {
              // If user has voted, update the selected card to match
              if (currentUser.hasVoted && currentUser.estimate) {
                setSelectedCard(currentUser.estimate);
              } else if (!currentUser.hasVoted) {
                // If estimates were deleted, clear selection
                setSelectedCard(null);
              }
            }
          }
        });

        // Check for existing userId for this room+user combination
        const storageKey = `scrumPokerUserId_${roomId}_${userName}`;
        const existingUserId = localStorage.getItem(storageKey);

        // Join the room (reuse userId if available)
        wsService.send({
          type: MessageType.JOIN,
          payload: {
            name: userName,
            roomId: roomId || "default-room",
            userId: existingUserId || undefined, // Include existing userId if available
          },
        });
      } catch (error) {
        console.error("[PokerRoom] Failed to connect to WebSocket:", error);
        console.error("[PokerRoom] Room ID was:", roomId);
        console.error("[PokerRoom] Attempt number:", connectionAttempts);
        setIsConnected(false);
        setConnectionError(
          "Failed to connect to the backend service. The backend might be starting up or experiencing issues."
        );

        // Ensure minimum loading time before showing error
        const elapsed = Date.now() - startTime;
        if (elapsed >= minLoadingTime) {
          setShowError(true);
        } else {
          setTimeout(() => setShowError(true), minLoadingTime - elapsed);
        }
      }
    };

    connectWebSocket();

    return () => {
      wsService.disconnect();
    };
  }, [roomId, userName, setUserId, retryTrigger]);

  const getRetryDelay = (attemptNumber: number): number | null => {
    if (attemptNumber < 10) {
      return 10; // First 10 attempts: 10 seconds
    } else if (attemptNumber < 20) {
      return 30; // Next 10 attempts: 30 seconds
    } else if (attemptNumber < 30) {
      return 60; // Next 10 attempts: 60 seconds
    } else {
      return null; // Stop retrying after 30 attempts
    }
  };

  const getRetryPhaseInfo = (attemptNumber: number) => {
    if (attemptNumber < 10) {
      return { phase: 1, remaining: 10 - attemptNumber, delay: 10 };
    } else if (attemptNumber < 20) {
      return { phase: 2, remaining: 20 - attemptNumber, delay: 30 };
    } else if (attemptNumber < 30) {
      return { phase: 3, remaining: 30 - attemptNumber, delay: 60 };
    } else {
      return { phase: 4, remaining: 0, delay: 0 };
    }
  };

  const handleRetryConnection = () => {
    // Reset error states
    setConnectionError(null);
    setShowError(false);
    setIsConnected(false);
    setRoomState(null);
    setRetryCountdown(0);

    // Trigger reconnection by updating the retry trigger
    setRetryTrigger((prev) => prev + 1);
  };

  const handleManualRetry = () => {
    // Reset total retry attempts on manual retry
    setTotalRetryAttempts(0);
    handleRetryConnection();
  };

  /**
   * Copies the room URL to clipboard
   */
  const handleCopyRoomLink = async () => {
    try {
      const roomUrl = window.location.href;
      await navigator.clipboard.writeText(roomUrl);
      setShowCopiedNotification(true);
      setTimeout(() => setShowCopiedNotification(false), 2000);
    } catch (err) {
      console.error("Failed to copy:", err);
    }
  };

  // Auto-retry functionality
  useEffect(() => {
    let countdownInterval: NodeJS.Timeout | null = null;
    let retryTimeout: NodeJS.Timeout | null = null;

    if (showError && autoRetryEnabled && connectionError) {
      const retryDelay = getRetryDelay(totalRetryAttempts);

      if (retryDelay !== null) {
        // Start countdown with appropriate delay
        setRetryCountdown(retryDelay);

        countdownInterval = setInterval(() => {
          setRetryCountdown((prev) => {
            if (prev <= 1) {
              // Time to retry
              clearInterval(countdownInterval!);
              retryTimeout = setTimeout(() => {
                setTotalRetryAttempts((prev) => prev + 1);
                handleRetryConnection();
              }, 100); // Small delay to ensure countdown shows 0
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } else {
        // Maximum attempts reached, stop retrying
        setRetryCountdown(0);
      }
    } else {
      // Clear countdown when error is resolved or auto-retry disabled
      setRetryCountdown(0);
    }

    return () => {
      if (countdownInterval) clearInterval(countdownInterval);
      if (retryTimeout) clearTimeout(retryTimeout);
    };
  }, [showError, autoRetryEnabled, connectionError, totalRetryAttempts]);

  // Reset retry attempts when connection succeeds
  useEffect(() => {
    if (isConnected && roomState) {
      setTotalRetryAttempts(0);
    }
  }, [isConnected, roomState]);

  const handleCardSelect = (value: string) => {
    if (!userId || !roomId) return;

    setSelectedCard(value);
    wsService.send({
      type: MessageType.VOTE,
      payload: {
        userId,
        roomId,
        estimate: value,
      },
    });
  };

  const handleShowEstimates = () => {
    if (!roomId) return;
    wsService.send({
      type: MessageType.SHOW_ESTIMATES,
      payload: { roomId },
    });
  };

  const handleHideEstimates = () => {
    if (!roomId) return;
    wsService.send({
      type: MessageType.HIDE_ESTIMATES,
      payload: { roomId },
    });
  };

  const handleDeleteEstimates = () => {
    if (!roomId) return;
    setSelectedCard(null);
    wsService.send({
      type: MessageType.DELETE_ESTIMATES,
      payload: { roomId },
    });
  };

  const allCards = [...SPECIAL_VALUES, ...FIBONACCI_VALUES];

  if (!isConnected || !roomState) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center max-w-md">
          {connectionError && showError ? (
            <>
              <div className="mb-4">
                <svg
                  className="mx-auto h-12 w-12 text-red-500"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                  />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-dark-text mb-2">
                Connection Error
              </h3>
              <p className="text-gray-600 dark:text-dark-text-secondary mb-4">
                {connectionError}
              </p>
              <div className="space-y-4">
                {/* Auto-retry status and countdown */}
                {autoRetryEnabled && (
                  <>
                    {retryCountdown > 0 ? (
                      <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-700 rounded-lg p-3">
                        <div className="flex items-center justify-center space-x-2 text-blue-600 dark:text-blue-400">
                          <div className="animate-pulse">🔄</div>
                          <span className="text-sm font-medium">
                            Auto-retry in {retryCountdown} seconds...
                          </span>
                        </div>

                        {/* Retry phase information */}
                        {(() => {
                          const phaseInfo =
                            getRetryPhaseInfo(totalRetryAttempts);
                          const currentDelay =
                            getRetryDelay(totalRetryAttempts);
                          return (
                            <div className="text-xs text-blue-500 dark:text-blue-300 text-center mt-1">
                              Phase {phaseInfo.phase}/3: {phaseInfo.remaining}{" "}
                              attempts remaining at {currentDelay}s intervals
                            </div>
                          );
                        })()}

                        {/* Progress bar */}
                        <div className="w-full bg-blue-100 dark:bg-blue-800 rounded-full h-1.5 mt-2">
                          <div
                            className="bg-blue-600 dark:bg-blue-400 h-1.5 rounded-full transition-all duration-1000 ease-linear"
                            style={{
                              width: `${
                                (((getRetryDelay(totalRetryAttempts) || 10) -
                                  retryCountdown) /
                                  (getRetryDelay(totalRetryAttempts) || 10)) *
                                100
                              }%`,
                            }}
                          ></div>
                        </div>
                      </div>
                    ) : getRetryDelay(totalRetryAttempts) === null ? (
                      <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-lg p-3">
                        <div className="flex items-center justify-center space-x-2 text-red-600 dark:text-red-400">
                          <span>⛔</span>
                          <span className="text-sm font-medium">
                            Auto-retry stopped after 30 attempts
                          </span>
                        </div>
                        <div className="text-xs text-red-500 dark:text-red-300 text-center mt-1">
                          Manual retry available - this will reset the retry
                          counter
                        </div>
                      </div>
                    ) : null}
                  </>
                )}

                {/* Manual retry button */}
                <button
                  onClick={handleManualRetry}
                  className="bg-blue-600 dark:bg-blue-700 text-white px-4 py-2 rounded hover:bg-blue-700 dark:hover:bg-blue-800 transition-colors"
                >
                  {retryCountdown > 0
                    ? "Try Again Now"
                    : getRetryDelay(totalRetryAttempts) === null
                    ? "Try Again (Reset Counter)"
                    : "Try Again"}
                </button>

                {/* Auto-retry toggle */}
                <div className="flex items-center justify-center space-x-2">
                  <button
                    onClick={() => setAutoRetryEnabled(!autoRetryEnabled)}
                    className={`flex items-center space-x-2 text-xs px-3 py-1 rounded-full transition-colors ${
                      autoRetryEnabled
                        ? "bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400"
                        : "bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400"
                    }`}
                  >
                    <span>{autoRetryEnabled ? "✅" : "⏸️"}</span>
                    <span>Auto-retry (10s→30s→60s)</span>
                  </button>
                </div>

                {/* Retry strategy explanation */}
                {autoRetryEnabled && (
                  <div className="text-xs text-gray-400 dark:text-gray-500 text-center">
                    Strategy: 10×10s, then 10×30s, then 10×60s (30 total
                    attempts)
                  </div>
                )}

                <div className="text-sm text-gray-500 dark:text-dark-text-secondary">
                  <p>💤 The backend may be sleeping to save resources.</p>
                  <p>
                    🚀 It will wake up automatically when you retry (may take
                    15-30 seconds).
                  </p>
                  <p>
                    ⏱️ Your name "{userName}" will be preserved during retry.
                  </p>
                </div>
                {connectionAttempts > 1 && (
                  <p className="text-xs text-gray-400 dark:text-dark-text-secondary">
                    Connection attempts: {connectionAttempts}
                  </p>
                )}
              </div>
            </>
          ) : (
            <>
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-400 mx-auto"></div>
              <p className="mt-4 text-gray-600 dark:text-dark-text-secondary">
                Connecting to room...
              </p>
              {connectionAttempts > 1 && (
                <p className="mt-2 text-sm text-gray-400 dark:text-dark-text-secondary">
                  Attempt {connectionAttempts}...
                </p>
              )}
            </>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-dark-bg p-4 transition-colors duration-200">
      <div className="max-w-6xl mx-auto space-y-6">
        {/* Reconnecting Banner */}
        {showReconnectingBanner && (
          <div className="bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-700 rounded-lg p-3 shadow-sm">
            <div className="flex items-center justify-center space-x-2">
              <div className="animate-spin rounded-full h-4 w-4 border-2 border-blue-600 dark:border-blue-400 border-t-transparent"></div>
              <span className="text-sm font-medium text-blue-600 dark:text-blue-400">
                Reconnecting to room...
              </span>
            </div>
          </div>
        )}

        <div className="text-center">
          <div className="mb-4">
            {/* <span className="text-sm text-gray-500 dark:text-dark-text-secondary">
              Room Name
            </span> */}
            <div className="flex items-center justify-center gap-3 mt-2">
              <h1 className="text-sm text-gray-500 dark:text-dark-text font-mono">
                Room name - {roomId}
              </h1>
              <button
                onClick={handleCopyRoomLink}
                className="relative group p-2 rounded-lg bg-blue-100 dark:bg-blue-900/30 hover:bg-blue-200 dark:hover:bg-blue-900/50 transition-colors duration-200"
                title="Copy room link"
              >
                {showCopiedNotification ? (
                  <svg
                    className="w-5 h-5 text-green-600 dark:text-green-400"
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
                ) : (
                  <svg
                    className="w-5 h-5 text-blue-600 dark:text-blue-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"
                    />
                  </svg>
                )}
                {showCopiedNotification && (
                  <span className="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-gray-900 dark:bg-gray-100 text-white dark:text-gray-900 text-xs py-1 px-2 rounded whitespace-nowrap">
                    Copied!
                  </span>
                )}
              </button>
            </div>
            <p className="text-sm text-gray-500 dark:text-dark-text-secondary mt-2">
              Share this link with your team
            </p>
            <div></div>
            <h2> ... </h2>
          </div>
          <h2 className="text-2xl font-semibold text-gray-800 dark:text-dark-text mb-2">
            Provide an effort estimate - choose one of the cards
          </h2>
          <p className="text-sm text-gray-600 dark:text-dark-text-secondary">
            Each team member estimates the complexity of the task (user story).
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-8 gap-3">
          {allCards.map((value) => (
            <PokerCard
              key={value}
              value={value}
              isSelected={selectedCard === value}
              onClick={() => handleCardSelect(value)}
            />
          ))}
        </div>

        <div className="bg-white dark:bg-dark-surface rounded-lg shadow p-6 transition-colors duration-200">
          <div className="mb-4">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-dark-text mb-2">
              Reveal the cards
            </h3>
            <p className="text-sm text-gray-600 dark:text-dark-text-secondary">
              Once everyone has submitted their effort estimates, the organizer
              reveals the cards.
            </p>
          </div>

          <UserTable
            users={roomState.users}
            showEstimates={roomState.showEstimates}
            onShowEstimates={handleShowEstimates}
            onHideEstimates={handleHideEstimates}
            onDeleteEstimates={handleDeleteEstimates}
            currentUserId={userId}
          />
        </div>
      </div>
    </div>
  );
};
