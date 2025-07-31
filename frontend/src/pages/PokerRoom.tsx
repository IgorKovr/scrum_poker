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
        console.log("[PokerRoom] WebSocket connected successfully");

        // Set up message handlers
        wsService.on(MessageType.JOIN, (payload) => {
          if (payload.userId) {
            setUserId(payload.userId);
          }
        });

        wsService.on(MessageType.ROOM_UPDATE, (payload) => {
          setRoomState(payload);
        });

        // Join the room
        wsService.send({
          type: MessageType.JOIN,
          payload: {
            name: userName,
            roomId: roomId || "default-room",
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

  // Auto-retry functionality
  useEffect(() => {
    let countdownInterval: NodeJS.Timeout | null = null;
    let retryTimeout: NodeJS.Timeout | null = null;

    if (showError && autoRetryEnabled && connectionError) {
      // Start 10-second countdown
      setRetryCountdown(10);

      countdownInterval = setInterval(() => {
        setRetryCountdown((prev) => {
          if (prev <= 1) {
            // Time to retry
            clearInterval(countdownInterval!);
            retryTimeout = setTimeout(() => {
              handleRetryConnection();
            }, 100); // Small delay to ensure countdown shows 0
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else {
      // Clear countdown when error is resolved or auto-retry disabled
      setRetryCountdown(0);
    }

    return () => {
      if (countdownInterval) clearInterval(countdownInterval);
      if (retryTimeout) clearTimeout(retryTimeout);
    };
  }, [showError, autoRetryEnabled, connectionError]);

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
                {autoRetryEnabled && retryCountdown > 0 && (
                  <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-700 rounded-lg p-3">
                    <div className="flex items-center justify-center space-x-2 text-blue-600 dark:text-blue-400">
                      <div className="animate-pulse">🔄</div>
                      <span className="text-sm font-medium">
                        Auto-retry in {retryCountdown} seconds...
                      </span>
                    </div>
                    {/* Progress bar */}
                    <div className="w-full bg-blue-100 dark:bg-blue-800 rounded-full h-1.5 mt-2">
                      <div
                        className="bg-blue-600 dark:bg-blue-400 h-1.5 rounded-full transition-all duration-1000 ease-linear"
                        style={{
                          width: `${((10 - retryCountdown) / 10) * 100}%`,
                        }}
                      ></div>
                    </div>
                  </div>
                )}

                {/* Manual retry button */}
                <button
                  onClick={handleRetryConnection}
                  className="bg-blue-600 dark:bg-blue-700 text-white px-4 py-2 rounded hover:bg-blue-700 dark:hover:bg-blue-800 transition-colors"
                >
                  {retryCountdown > 0 ? "Try Again Now" : "Try Again"}
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
                    <span>Auto-retry every 10s</span>
                  </button>
                </div>

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
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-gray-800 dark:text-dark-text mb-2">
            Provide an effort estimate - choose one of the cards
          </h2>
          <p className="text-sm text-gray-600 dark:text-dark-text-secondary">
            Each team member should estimate the complexity of the task (user
            story) to be completed.
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
