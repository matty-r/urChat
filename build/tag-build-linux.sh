#!/bin/bash

# Save the current directory to a variable
current_dir=$(pwd)

# Get the latest Git tag
latest_tag=$(git describe --tags --abbrev=0)

# Clone the repository into a temporary directory
temp_dir=$(mktemp -d)
git clone . "$temp_dir"
cd "$temp_dir"

# Checkout the latest Git tag in the temporary directory
git checkout --quiet "$latest_tag"

# Update the UR_VERSION in Constants.java
sed -i "s/public static String UR_VERSION.*/public static String UR_VERSION = \"rel-$latest_tag\";/" src/urChatBasic/base/Constants.java

# Compile the Java files, excluding specific files or directories
find src -name "*.java" ! -name "UIManagerDefaults.java" ! -path "*/tests/*" | xargs javac -cp lib/*:. -d bin

# Copy the images directory
mkdir -p bin/images
cp -r src/images/* bin/images/

# Create a manifest file specifying the main class
echo "Main-Class: urChatBasic.frontend.DriverGUI" > manifest.txt

# Create the JAR file with the manifest and compiled class files
mkdir -p "$current_dir/release"
jar -cfm "$current_dir/release/urchat.jar" manifest.txt -C "$temp_dir/bin" .

# Clean up the temporary directory
cd ..
rm -rf "$temp_dir"
