export interface User {
  id: string;
  name: string;
  estimate?: string;
  hasVoted: boolean;
}

export interface RoomState {
  roomId: string;
  users: User[];
  showEstimates: boolean;
}

export enum MessageType {
  JOIN = 'JOIN',
  VOTE = 'VOTE',
  SHOW_ESTIMATES = 'SHOW_ESTIMATES',
  HIDE_ESTIMATES = 'HIDE_ESTIMATES',
  DELETE_ESTIMATES = 'DELETE_ESTIMATES',
  ROOM_UPDATE = 'ROOM_UPDATE',
  USER_LEFT = 'USER_LEFT'
}

export interface WebSocketMessage {
  type: MessageType;
  payload: any;
}

export const FIBONACCI_VALUES = ['0', '0.5', '1', '2', '3', '5', '8', '13', '20', '40'];
export const SPECIAL_VALUES = ['?', '☕']; 