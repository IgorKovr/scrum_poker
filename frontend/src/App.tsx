import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { NameEntry } from './pages/NameEntry';
import { PokerRoom } from './pages/PokerRoom';

function App() {
  const [userName, setUserName] = useState<string>('');
  const [userId, setUserId] = useState<string>('');

  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          <Route 
            path="/" 
            element={
              <NameEntry 
                onNameSubmit={(name) => setUserName(name)} 
              />
            } 
          />
          <Route 
            path="/room/:roomId" 
            element={
              userName ? (
                <PokerRoom 
                  userName={userName} 
                  userId={userId}
                  setUserId={setUserId}
                />
              ) : (
                <Navigate to="/" replace />
              )
            } 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App; 