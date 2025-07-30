import React from 'react';

interface PokerCardProps {
  value: string;
  isSelected: boolean;
  onClick: () => void;
}

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
      {value}
    </button>
  );
}; 