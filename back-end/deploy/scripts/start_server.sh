#!/bin/bash

# Navigate to the deployment directory
cd /home/ec2-user/deployment

export PATH=$PATH:/usr/local/bin

# End any existing supervisor processes
sudo pkill supervisord

source ./set_env_vars.sh

# Start the Spring Boot application using supervisor and the env variables
echo "Starting Spring Boot app..."
supervisord -c ./supervisord.conf

echo "Waiting for Spring Boot app to start..."
timeout=180  # Max wait time of 3 minutes
while ! curl -s http://localhost:8080 > /dev/null && [ $timeout -gt 0 ]; do
    echo "Spring Boot app is not ready yet. Waiting..."
    sleep 5  # Wait for 5 seconds before checking again
    timeout=$((timeout-5))
done

# Check if timeout ran out
if [ $timeout -le 0 ]; then
    echo "Spring Boot app failed to start within the timeout period."
    exit 1
fi

# Once the app is running, start Caddy
echo "Spring Boot app is up. Starting Caddy..."

# Restart caddy (used to reverse proxy for HTTP)
sudo pkill caddy
sudo caddy fmt --overwrite
sudo caddy start --config ./Caddyfile

echo "Caddy started successfully."