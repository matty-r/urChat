#!/bin/bash

# Save the current directory to a variable
current_dir=$(pwd)

# Clone the repository into a temporary directory
temp_dir=$(mktemp -d)
git clone . "$temp_dir"
cd "$temp_dir"

# Compile the Java files for the main jar
find src -name "*.java" -exec javac -cp "lib/*" -d "bin/" {} +

# Copy the images directory
cp -r "images" "bin/"

# Copy the lib directory
cp -r "lib" "bin/"

cd "bin"

# Create a manifest file specifying the main class
echo "Main-Class: urChatBasic.frontend.DriverGUI" > ../manifest.txt

# Create the JAR file with the manifest and compiled class files, includes the lib and images directory in the created JAR file
jar -cfm "urChat.jar" ../manifest.txt .

# Delete all the files we don't want included in the urTestRunner.jar
rm -rf "images"
rm -rf "urChatBasic"

# Compile the Java files for the urTestRunner, using the urChat.jar as a source of the lib files
find ../tests -name "*.java" -exec javac -cp "urChat.jar:lib/*" -d . {} +

# Move the main.jar back into the temp dir
mv "urChat.jar" ../

rm -rf "lib"

# Create a manifest file for the test runner
echo "Main-Class: TestRunner" > ../testmanifest.txt

jar -cfm "urTestRunner.jar" ../testmanifest.txt .

mv "urTestRunner.jar" ../

# Clean up the temporary directory
cd ..
rm -rf "$temp_dir"
