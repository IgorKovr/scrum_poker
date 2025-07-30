#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting Scrum Poker Local Development${NC}"
echo "================================================"

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Shutting down services...${NC}"
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    exit
}

trap cleanup EXIT INT TERM

# Start backend
echo -e "\n${GREEN}Starting Backend...${NC}"
cd backend
./gradlew bootRun &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo -e "${YELLOW}Waiting for backend to start...${NC}"
sleep 10

# Start frontend
echo -e "\n${GREEN}Starting Frontend...${NC}"
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo -e "\n${BLUE}================================================${NC}"
echo -e "${GREEN}✅ Scrum Poker is running!${NC}"
echo -e "${BLUE}================================================${NC}"
echo -e "Frontend: ${GREEN}http://localhost:3000${NC}"
echo -e "Backend:  ${GREEN}http://localhost:8080${NC}"
echo -e "Health:   ${GREEN}http://localhost:8080/health${NC}"
echo -e "${BLUE}================================================${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}\n"

# Wait for processes
wait 