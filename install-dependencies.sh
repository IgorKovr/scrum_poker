#!/bin/bash

echo "=== Scrum Poker Dependencies Installation Script ==="
echo ""

# Detect OS
OS="$(uname -s)"

case "${OS}" in
    Darwin*)    OS='Mac';;
    Linux*)     OS='Linux';;
    *)          OS="UNKNOWN:${OS}"
esac

echo "Detected OS: ${OS}"
echo ""

if [ "$OS" = "Mac" ]; then
    echo "Checking for Homebrew..."
    if ! command -v brew &> /dev/null; then
        echo "Homebrew is not installed. Would you like to install it? (y/n)"
        read -r response
        if [ "$response" = "y" ]; then
            echo "Installing Homebrew..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        else
            echo "Please install Homebrew manually or use official installers for Node.js and Java."
            exit 1
        fi
    fi

    echo ""
    echo "Installing Node.js 18..."
    brew install node@18
    
    echo ""
    echo "Installing Java JDK 17..."
    brew install openjdk@17
    
    echo ""
    echo "Setting up Java PATH..."
    echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
    
    echo ""
    echo "Installation complete! Please run:"
    echo "  source ~/.zshrc"
    echo ""
    echo "Then verify with:"
    echo "  node --version"
    echo "  java --version"
    
elif [ "$OS" = "Linux" ]; then
    echo "For Linux, please use your package manager:"
    echo ""
    echo "Ubuntu/Debian:"
    echo "  sudo apt update"
    echo "  sudo apt install nodejs npm openjdk-17-jdk"
    echo ""
    echo "RHEL/CentOS/Fedora:"
    echo "  sudo dnf install nodejs npm java-17-openjdk-devel"
    echo ""
    echo "Arch:"
    echo "  sudo pacman -S nodejs npm jdk17-openjdk"
else
    echo "Unknown OS. Please install Node.js 18+ and Java JDK 17+ manually."
    echo "Visit:"
    echo "  - https://nodejs.org/"
    echo "  - https://www.oracle.com/java/technologies/downloads/"
fi 