#!/bin/bash

# AI Resume Analyzer - Setup Script

echo "================================"
echo "AI Resume Analyzer Setup"
echo "================================"

# Check Java version
echo "Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F'"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERROR: Java 17+ required (found version $JAVA_VERSION)"
    exit 1
fi
echo "✓ Java version OK: $JAVA_VERSION"

# Check Maven version
echo "Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed"
    exit 1
fi
echo "✓ Maven found"

# Create configuration if it doesn't exist
if [ ! -f "src/main/resources/application.properties" ]; then
    echo "Creating configuration file..."
    cp src/main/resources/application.properties.template src/main/resources/application.properties
    echo "⚠ Please edit src/main/resources/application.properties with your API key"
fi

# Build project
echo "Building project..."
mvn clean install

if [ $? -eq 0 ]; then
    echo "================================================"
    echo "✓ Build successful!"
    echo "================================================"
    echo ""
    echo "Next steps:"
    echo "1. Edit src/main/resources/application.properties"
    echo "2. Set your Gemini API key"
    echo "3. Run: mvn spring-boot:run"
    echo "4. Access: http://localhost:8080"
else
    echo "✗ Build failed!"
    exit 1
fi
