#!/bin/bash

echo "=== Scrum Poker Application Launcher ==="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Set up Java for backend
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Set up Node.js for frontend
export PATH="/opt/homebrew/opt/node@18/bin:$PATH"

echo -e "${YELLOW}Starting Backend...${NC}"
echo "Opening new terminal for backend..."
osascript -e 'tell app "Terminal" to do script "cd '$PWD'/backend && export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home && export PATH=$JAVA_HOME/bin:$PATH && ./gradlew bootRun"' &

echo ""
echo -e "${YELLOW}Starting Frontend...${NC}"
echo "Opening new terminal for frontend..."
osascript -e 'tell app "Terminal" to do script "cd '$PWD'/frontend && export PATH=\"/opt/homebrew/opt/node@18/bin:$PATH\" && npm run dev"' &

echo ""
echo -e "${GREEN}✅ Both applications are starting in separate terminals!${NC}"
echo ""
echo "Backend will be available at: http://localhost:8080"
echo "Frontend will be available at: http://localhost:3000"
echo ""
echo "Please wait a few seconds for both applications to start..."
echo ""
echo "To stop the applications, close the terminal windows or press Ctrl+C in each window." 