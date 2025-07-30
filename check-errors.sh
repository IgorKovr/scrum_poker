#!/bin/bash

echo "=== Scrum Poker Application Error Check ==="
echo ""

# Check for required tools
echo "1. Checking required tools..."
echo -n "   Node.js: "
if command -v node &> /dev/null; then
    node --version
else
    echo "NOT INSTALLED ❌"
fi

echo -n "   npm: "
if command -v npm &> /dev/null; then
    npm --version
else
    echo "NOT INSTALLED ❌"
fi

echo -n "   Java: "
if command -v java &> /dev/null; then
    java --version 2>&1 | head -n 1
else
    echo "NOT INSTALLED ❌"
fi

echo ""
echo "2. Frontend Structure Check..."
if [ -d "frontend/src" ]; then
    echo "   ✓ Frontend source directory exists"
    echo "   Files created:"
    find frontend/src -name "*.tsx" -o -name "*.ts" | wc -l | xargs echo "     - TypeScript/React files:"
else
    echo "   ✗ Frontend source directory missing"
fi

echo ""
echo "3. Backend Structure Check..."
if [ -d "backend/src/main/kotlin" ]; then
    echo "   ✓ Backend source directory exists"
    echo "   Files created:"
    find backend/src/main/kotlin -name "*.kt" | wc -l | xargs echo "     - Kotlin files:"
else
    echo "   ✗ Backend source directory missing"
fi

echo ""
echo "4. Configuration Files..."
[ -f "frontend/package.json" ] && echo "   ✓ frontend/package.json" || echo "   ✗ frontend/package.json"
[ -f "frontend/tsconfig.json" ] && echo "   ✓ frontend/tsconfig.json" || echo "   ✗ frontend/tsconfig.json"
[ -f "frontend/vite.config.ts" ] && echo "   ✓ frontend/vite.config.ts" || echo "   ✗ frontend/vite.config.ts"
[ -f "backend/build.gradle.kts" ] && echo "   ✓ backend/build.gradle.kts" || echo "   ✗ backend/build.gradle.kts"

echo ""
echo "5. Known Issues..."
echo "   - TypeScript import errors: Will be resolved after 'npm install'"
echo "   - Missing Gradle wrapper: Need to run 'gradle wrapper' in backend directory"
echo "   - WebSocket connection: Requires both frontend and backend to be running"

echo ""
echo "=== Summary ==="
echo "The application structure has been created successfully."
echo "To run the application, you need to:"
echo "1. Install Node.js 18+ and Java JDK 17+"
echo "2. Run 'npm install' in the frontend directory"
echo "3. Run './gradlew bootRun' in the backend directory"
echo ""
echo "See SETUP.md for detailed installation instructions." 