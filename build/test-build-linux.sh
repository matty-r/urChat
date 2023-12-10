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
jar -cfm "urchat.jar" ../manifest.txt .

# Delete all the files not needed to compile the test runner
rm -rf "images"
rm -rf "urChatBasic"

# Copy the lib directory
cp -r "$initial_dir/lib" "$temp_dir/"

# Copy the test libs
cp -r "$temp_dir/lib/test/" "$temp_dir/bin/"

# Compile the Java files for the urTestRunner, using the urchat.jar as a source of the lib files
find ../tests -name "*.java" -exec javac -cp "urchat.jar:test/*" -d . {} +

# Extract the test libs to be included in urTestRunner.jar
mkdir -p "$temp_dir/extracted_libs"
cd "$temp_dir/extracted_libs"

for file in "$temp_dir"/lib/test/*.jar; do
    jar xf "$file"
done

cd "$temp_dir/bin"

# Move the main.jar back into the temp dir (we don't want it included in urTestRunner)
mv "urchat.jar" "$temp_dir"

# Delete the test libs
rm -rf "test"

# Create a manifest file for urTestRunner
echo "Main-Class: URTestRunner" > ../testmanifest.txt
echo "Class-Path: urchat.jar test/*" >> ../testmanifest.txt

# Compile to urTestRunner.jar using the testmanifest.txt, the contents of the current directory (/bin) plus the extracted_libs
jar -cfm "urTestRunner.jar" "$temp_dir/testmanifest.txt" . -C "$temp_dir/extracted_libs/" .

mv "urTestRunner.jar" "$temp_dir"

cd "$temp_dir"

mkdir -p "report"

# run with jacoco agent to build coverage.exec
java -javaagent:lib/coverage/jacocoagent.jar=destfile=coverage.exec -cp "urchat.jar:urTestRunner.jar" org.testng.TestNG ./build/testng_release.xml

# build html report pointing to the source .java files
java -jar lib/coverage/jacococli.jar report coverage.exec --classfiles urchat.jar --html report --sourcefiles src/

# Move the JARs to the release directory
mkdir -p "$initial_dir/release"
mv "$temp_dir/urchat.jar" "$initial_dir/release"
mv "$temp_dir/urTestRunner.jar" "$initial_dir/release"

# Jacoco Output
mv "report" "$initial_dir"

# TestNG Output
mv "test-output" "$initial_dir"

# Clean up the temporary directory
cd "$initial_dir"

rm -rf "$temp_dir"
