#!/bin/bash

# Navigate to the project directory
cd /home/ec2-user/deployment

# Install Java 21 if not installed
if ! java -version 2>&1 | grep -q "21"; then
    sudo yum install -y java-21-amazon-corretto-headless.x86_64 
fi

sudo yum install -y supervisor

# # Make sure Gradle wrapper has execute permission
# chmod +x ./gradlew
# Ensure the JAR is executable (if needed)
# chmod +x build/libs/clutter-map-0.0.1-SNAPSHOT.jar
chmod +x clutter-map-0.0.1-SNAPSHOT.jar