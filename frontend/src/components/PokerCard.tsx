/**
 * PokerCard.tsx - Individual Poker Card Component
 * 
 * This component renders a single poker card that users can click to select
 * as their estimation. Each card displays a value (number or special symbol)
 * and provides visual feedback for selection state.
 * 
 * Key Features:
 * 1. Interactive button with hover and focus states
 * 2. Visual selection feedback (color change, elevation)
 * 3. Responsive design that adapts to different screen sizes
 * 4. Accessible with proper focus management and keyboard support
 * 5. Smooth animations for state transitions
 * 
 * Visual States:
 * - Default: White background with gray border
 * - Hover: Slightly elevated with shadow increase
 * - Selected: Blue background with white text and elevation
 * - Focus: Blue ring for keyboard accessibility
 * 
 * The card uses Tailwind CSS for styling with conditional classes
 * based on the selection state.
 */

import React from 'react';

/**
 * Props interface for the PokerCard component
 * Defines the required properties for rendering and handling card interactions
 */
interface PokerCardProps {
  /** The value to display on the card (number or special symbol like '?', '☕') */
  value: string;
  
  /** Whether this card is currently selected by the user */
  isSelected: boolean;
  
  /** Callback function called when the card is clicked */
  onClick: () => void;
}

/**
 * PokerCard Component - Interactive estimation card
 * 
 * Renders a clickable card that represents a story point value or special option.
 * The card provides visual feedback for selection state and handles user interaction.
 * 
 * The component is designed to be used within a grid layout where multiple cards
 * are displayed for user selection. Only one card should be selected at a time
 * per user.
 * 
 * @param {PokerCardProps} props - Component props containing value, selection state, and click handler
 * @returns {JSX.Element} An interactive button styled as a poker card
 */
export const PokerCard: React.FC<PokerCardProps> = ({ value, isSelected, onClick }) => {
  return (
    <button
      onClick={onClick}
      className={`
        relative w-full h-24 rounded-lg font-bold text-xl transition-all duration-200
        ${isSelected 
          ? 'bg-blue-600 text-white shadow-lg transform -translate-y-1' 
          : 'bg-white text-gray-800 hover:bg-gray-50 shadow-md hover:shadow-lg'
        }
        border-2 ${isSelected ? 'border-blue-700' : 'border-gray-200'}
        focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2
      `}
    >
      {/* Display the card value (number or special symbol) */}
      {value}
    </button>
  );
}; 