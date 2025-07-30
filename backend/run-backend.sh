#!/bin/bash

echo "Starting Scrum Poker Backend..."

# Set JAVA_HOME to the Java 17 installation
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java from: $(which java)"
echo "Java version:"
java -version

echo ""
echo "Running Spring Boot application..."
./gradlew bootRun 