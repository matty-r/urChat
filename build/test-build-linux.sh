#!/bin/bash

# Save the current directory to a variable
initial_dir=$(pwd)

# Clone the repository into a temporary directory
temp_dir=$(mktemp -d)
git clone . "$temp_dir"
cd "$temp_dir"

# Compile the Java files for the main jar
find src -name "*.java" -exec javac -d "bin" {} +

cd "bin"

# Copy the images directory
cp -r "$initial_dir/src/images" "."

# Create a manifest file specifying the main class
echo "Main-Class: urChatBasic.frontend.DriverGUI" > ../manifest.txt

# Create the JAR file with the manifest and compiled class files, includes the lib and images directory in the created JAR file
jar -cfm "urChat.jar" ../manifest.txt .

# Delete all the files not needed to compile the test runner
rm -rf "images"
rm -rf "urChatBasic"

# Copy the lib directory
cp -r "$initial_dir/lib" "."

# Compile the Java files for the urTestRunner, using the urChat.jar as a source of the lib files
find ../tests -name "*.java" -exec javac -cp "urChat.jar:./lib/*" -d . {} +

# Move the main.jar back into the temp dir (we don't want it included in the test runner jar)
mv "urChat.jar" ../

# Create a manifest file for the test runner
echo "Main-Class: URTestRunner" > ../testmanifest.txt
echo "Class-Path: urChat.jar ./lib/*" >> ../testmanifest.txt

jar -cfm "urTestRunner.jar" ../testmanifest.txt .

mv "lib" ../
mv "urTestRunner.jar" ../

cd ../

# run with jacoco agent to build coverage.exec
java -javaagent:lib/jacocoagent.jar=destfile=coverage.exec -cp "urChat.jar:lib/*:urTestRunner.jar" URTestRunner

# build html report pointing to the source .java files
java -jar lib/jacococli.jar report coverage.exec --classfiles urChat.jar --html coverage --sourcefiles src/

mv "coverage" "$initial_dir"

# Clean up the temporary directory
cd "$initial_dir"

rm -rf "$temp_dir"
