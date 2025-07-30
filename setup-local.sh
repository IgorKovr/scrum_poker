#!/bin/bash

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}🔧 Scrum Poker Local Setup${NC}"
echo "================================"

# Check for Java
echo -e "\n${YELLOW}Checking Java installation...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed!${NC}"
    echo "Please install Java 17 or higher"
    echo "macOS: brew install openjdk@17"
    exit 1
else
    java_version=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✅ Java found: $java_version${NC}"
fi

# Check for Node.js
echo -e "\n${YELLOW}Checking Node.js installation...${NC}"
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed!${NC}"
    echo "Please install Node.js 18 or higher"
    echo "macOS: brew install node"
    exit 1
else
    node_version=$(node --version)
    echo -e "${GREEN}✅ Node.js found: $node_version${NC}"
fi

# Backend setup
echo -e "\n${YELLOW}Setting up Backend...${NC}"
cd backend
if [ ! -f "gradlew" ]; then
    echo -e "${RED}❌ gradlew not found!${NC}"
    exit 1
fi
chmod +x gradlew
echo -e "${GREEN}✅ Backend ready${NC}"

# Frontend setup
echo -e "\n${YELLOW}Setting up Frontend...${NC}"
cd ../frontend
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi
echo -e "${GREEN}✅ Frontend ready${NC}"

cd ..

echo -e "\n${BLUE}================================${NC}"
echo -e "${GREEN}✅ Setup Complete!${NC}"
echo -e "${BLUE}================================${NC}"
echo -e "\nTo start the application, run:"
echo -e "${YELLOW}./run-local.sh${NC}"
echo -e "\nOr run services separately:"
echo -e "Backend:  ${YELLOW}cd backend && ./gradlew bootRun${NC}"
echo -e "Frontend: ${YELLOW}cd frontend && npm run dev${NC}" 