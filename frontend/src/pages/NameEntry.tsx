import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';

interface NameEntryProps {
  onNameSubmit: (name: string) => void;
}

interface FormData {
  name: string;
}

export const NameEntry: React.FC<NameEntryProps> = ({ onNameSubmit }) => {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>();
  const navigate = useNavigate();

  const onSubmit = (data: FormData) => {
    onNameSubmit(data.name);
    // Generate a random room ID or use a predefined one
    const roomId = 'default-room';
    navigate(`/room/${roomId}`);
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900">Enter Room</h1>
          <p className="mt-2 text-sm text-gray-600">
            Provide your name or any pseudonym to enter the scrum poker room
          </p>
        </div>
        
        <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-6">
          <div>
            <input
              {...register('name', { 
                required: 'Name is required',
                minLength: { value: 1, message: 'Name must be at least 1 character' }
              })}
              type="text"
              placeholder="Display Name*"
              className="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
            />
            {errors.name && (
              <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
            )}
          </div>

          <div className="space-y-3">
            <button
              type="submit"
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              ENTER ROOM
            </button>
            
            <button
              type="button"
              onClick={() => window.history.back()}
              className="group relative w-full flex justify-center py-3 px-4 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              CANCEL
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}; 