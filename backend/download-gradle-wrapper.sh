#!/bin/bash

echo "Downloading Gradle wrapper jar..."
mkdir -p gradle/wrapper

# Download the gradle-wrapper.jar
curl -L https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar \
     -o gradle/wrapper/gradle-wrapper.jar

if [ $? -eq 0 ]; then
    echo "✅ Gradle wrapper jar downloaded successfully!"
    echo ""
    echo "Now you can run: ./gradlew bootRun"
else
    echo "❌ Failed to download gradle wrapper jar"
    echo ""
    echo "Alternative: Install Gradle globally and run 'gradle wrapper'"
fi 