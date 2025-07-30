#!/bin/bash

# Set Java environment
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

clear
echo "🚀 Starting Scrum Poker Backend..."
echo "================================="
echo ""

# Run with quiet mode and filter output
./gradlew bootRun --quiet 2>&1 | grep -v "EXECUTING" | grep -v "Task :" 