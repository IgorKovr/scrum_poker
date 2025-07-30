import React from 'react';
import { User } from '../types';

interface UserTableProps {
  users: User[];
  showEstimates: boolean;
  onShowEstimates: () => void;
  onHideEstimates: () => void;
  onDeleteEstimates: () => void;
  currentUserId: string;
}

export const UserTable: React.FC<UserTableProps> = ({
  users,
  showEstimates,
  onShowEstimates,
  onHideEstimates,
  onDeleteEstimates,
  currentUserId
}) => {
  return (
    <div>
      <div className="flex items-center gap-4 mb-4">
        <div className="flex items-center gap-2 text-gray-600">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <span className="text-sm">{users.length} participant{users.length !== 1 ? 's' : ''}</span>
        </div>
        
        <div className="flex gap-2 ml-auto">
          <button
            onClick={onDeleteEstimates}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Delete Estimates
          </button>
          
          {showEstimates ? (
            <button
              onClick={onHideEstimates}
              className="px-4 py-2 text-sm font-medium text-white bg-gray-600 rounded-md hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
            >
              Hide
            </button>
          ) : (
            <button
              onClick={onShowEstimates}
              className="px-4 py-2 text-sm font-medium text-white bg-gray-600 rounded-md hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
            >
              Show
            </button>
          )}
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Story Points
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {users.map((user) => (
              <tr key={user.id} className={user.id === currentUserId ? 'bg-blue-50' : ''}>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">
                    {user.name}
                    {user.id === currentUserId && (
                      <span className="ml-2 text-xs text-blue-600">(You)</span>
                    )}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {showEstimates && user.estimate ? (
                    <div className="inline-flex items-center justify-center w-12 h-12 text-lg font-bold text-blue-600 bg-blue-100 rounded-lg">
                      {user.estimate}
                    </div>
                  ) : user.hasVoted ? (
                    <div className="inline-flex items-center justify-center w-12 h-12 bg-green-100 rounded-lg">
                      <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                  ) : (
                    <div className="text-2xl text-gray-400">-</div>
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