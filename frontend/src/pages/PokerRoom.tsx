import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { PokerCard } from '../components/PokerCard';
import { UserTable } from '../components/UserTable';
import { wsService } from '../services/websocket';
import { MessageType, RoomState, FIBONACCI_VALUES, SPECIAL_VALUES } from '../types';
import { getWebSocketUrl } from '../config';

interface PokerRoomProps {
  userName: string;
  userId: string;
  setUserId: (id: string) => void;
}

export const PokerRoom: React.FC<PokerRoomProps> = ({ userName, userId, setUserId }) => {
  const { roomId } = useParams<{ roomId: string }>();
  const [roomState, setRoomState] = useState<RoomState | null>(null);
  const [selectedCard, setSelectedCard] = useState<string | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [connectionError, setConnectionError] = useState<string | null>(null);
  const [connectionAttempts, setConnectionAttempts] = useState(0);

  useEffect(() => {
    const connectWebSocket = async () => {
      try {
        setConnectionError(null);
        setConnectionAttempts(prev => prev + 1);
        
        // Set a timeout to show error if connection takes too long
        const timeoutId = setTimeout(() => {
          if (!wsService.isConnected()) {
            setConnectionError('Connection timeout. The backend service might be down or starting up.');
          }
        }, 10000); // 10 seconds timeout
        
        // Use the configured WebSocket URL
        const wsUrl = getWebSocketUrl();
        console.log('[PokerRoom] WebSocket URL:', wsUrl);
        console.log('[PokerRoom] Connecting to room:', roomId);
        
        await wsService.connect(wsUrl);
        clearTimeout(timeoutId);
        setIsConnected(true);
        setConnectionError(null);
        console.log('[PokerRoom] WebSocket connected successfully');

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
            roomId: roomId || 'default-room'
          }
        });
      } catch (error) {
        console.error('[PokerRoom] Failed to connect to WebSocket:', error);
        console.error('[PokerRoom] Room ID was:', roomId);
        console.error('[PokerRoom] Attempt number:', connectionAttempts);
        setIsConnected(false);
        setConnectionError('Failed to connect to the backend service. The backend might be starting up or experiencing issues.');
      }
    };

    connectWebSocket();

    return () => {
      wsService.disconnect();
    };
  }, [roomId, userName, setUserId]);

  const handleCardSelect = (value: string) => {
    if (!userId || !roomId) return;
    
    setSelectedCard(value);
    wsService.send({
      type: MessageType.VOTE,
      payload: {
        userId,
        roomId,
        estimate: value
      }
    });
  };

  const handleShowEstimates = () => {
    if (!roomId) return;
    wsService.send({
      type: MessageType.SHOW_ESTIMATES,
      payload: { roomId }
    });
  };

  const handleHideEstimates = () => {
    if (!roomId) return;
    wsService.send({
      type: MessageType.HIDE_ESTIMATES,
      payload: { roomId }
    });
  };

  const handleDeleteEstimates = () => {
    if (!roomId) return;
    setSelectedCard(null);
    wsService.send({
      type: MessageType.DELETE_ESTIMATES,
      payload: { roomId }
    });
  };

  const allCards = [...SPECIAL_VALUES, ...FIBONACCI_VALUES];

  if (!isConnected || !roomState) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center max-w-md">
          {connectionError ? (
            <>
              <div className="mb-4">
                <svg className="mx-auto h-12 w-12 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Connection Error</h3>
              <p className="text-gray-600 mb-4">{connectionError}</p>
              <div className="space-y-3">
                <button
                  onClick={() => window.location.reload()}
                  className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
                >
                  Retry Connection
                </button>
                <div className="text-sm text-gray-500">
                  <p>If the problem persists:</p>
                  <p>• Check the backend logs in Railway dashboard</p>
                  <p>• Ensure the backend service is running</p>
                  <p>• Wait a moment for the backend to start up</p>
                </div>
                {connectionAttempts > 1 && (
                  <p className="text-xs text-gray-400">
                    Connection attempts: {connectionAttempts}
                  </p>
                )}
              </div>
            </>
          ) : (
            <>
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-4 text-gray-600">Connecting to room...</p>
              {connectionAttempts > 1 && (
                <p className="mt-2 text-sm text-gray-400">
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
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-gray-800 mb-2">
            Provide an effort estimate - choose one of the cards
          </h2>
          <p className="text-sm text-gray-600">
            Each team member should estimate the complexity of the task (user story) to be completed.{' '}
            <a href="#" className="text-blue-600 hover:underline">Read more...</a>
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

        <div className="bg-white rounded-lg shadow p-6">
          <div className="mb-4">
            <h3 className="text-lg font-semibold text-gray-800 mb-2">Reveal the cards</h3>
            <p className="text-sm text-gray-600">
              Once everyone has submitted their effort estimates, the organizer reveals the cards.{' '}
              <a href="#" className="text-blue-600 hover:underline">Read more...</a>
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