#!/bin/bash

# Navigate to the project directory
cd /home/ec2-user/cluttermap-app/back-end

# Install Java 21 if not installed
if ! java -version 2>&1 | grep -q "21"; then
    sudo yum install -y java-21-amazon-corretto-headless.x86_64 
fi

# Make sure Gradle wrapper has execute permission
chmod +x ./gradlew