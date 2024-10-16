#!/bin/bash

# Navigate to the Java Spring app directory
cd /home/ec2-user/clutter-map/back-end

# Ensure the JAR is executable (if needed)
chmod +x build/libs/clutter-map-0.0.1-SNAPSHOT.jar

# Start the Spring Boot application using supervisor
supervisord -c supervisord.conf