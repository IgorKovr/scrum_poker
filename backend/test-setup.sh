#!/bin/bash

echo "=== Testing Backend Setup ==="
echo ""

# Check Java installation
echo "1. Checking Java installation..."
if [ -d "/opt/homebrew/Cellar/openjdk@17" ]; then
    echo "   ✅ Java 17 is installed via Homebrew"
    export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
    export PATH=$JAVA_HOME/bin:$PATH
    echo "   JAVA_HOME set to: $JAVA_HOME"
    echo "   Java version: $(java -version 2>&1 | head -n 1)"
else
    echo "   ❌ Java 17 not found in expected location"
fi

echo ""
echo "2. Checking Gradle wrapper..."
if [ -f "./gradlew" ]; then
    echo "   ✅ Gradle wrapper script exists"
else
    echo "   ❌ Gradle wrapper script missing"
fi

if [ -f "./gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "   ✅ Gradle wrapper jar exists"
else
    echo "   ❌ Gradle wrapper jar missing"
fi

echo ""
echo "3. Testing Gradle..."
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
./gradlew --version

echo ""
echo "4. Checking project structure..."
if [ -f "build.gradle.kts" ]; then
    echo "   ✅ build.gradle.kts exists"
else
    echo "   ❌ build.gradle.kts missing"
fi

if [ -d "src/main/kotlin" ]; then
    echo "   ✅ Kotlin source directory exists"
    echo "   Kotlin files: $(find src/main/kotlin -name "*.kt" | wc -l | xargs)"
else
    echo "   ❌ Kotlin source directory missing"
fi

echo ""
echo "=== Setup Summary ==="
echo "If all checks pass, you should be able to run: ./gradlew bootRun"
echo "Make sure to set JAVA_HOME and PATH as shown above." 