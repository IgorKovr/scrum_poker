#!/bin/bash

# Set Java environment
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

clear
echo "🚀 Starting Scrum Poker Backend..."
echo "================================="
echo "Building and starting the application..."
echo ""

# Build the application first (suppress output)
./gradlew build --quiet > /dev/null 2>&1

# Run the application directly with java
java -jar build/libs/scrum-poker-backend-0.0.1-SNAPSHOT.jar 