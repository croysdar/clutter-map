#!/bin/bash

# Navigate to the deployment directory
cd /home/ec2-user/deployment

export PATH=$PATH:/usr/local/bin

source ./set_env_vars.sh

# Check if supervisord is running
if pgrep -x "supervisord" > /dev/null
then
    echo "Running reread and update on running supervisor."
    sudo supervisorctl reread
    sudo supervisorctl update
else
    echo "Supervisord is not running. Starting supervisord..."

    # Start the Spring Boot application using supervisor and the env variables
    supervisord -c ./supervisord.conf
    
    # Verify that supervisord started successfully
    if pgrep -x "supervisord" > /dev/null
    then
        echo "Supervisord started successfully."
    else
        echo "Failed to start supervisord."
        exit 1
    fi
fi

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

# Gracefully stop Caddy if it's running
if pgrep -x "caddy" > /dev/null
then
    echo "Caddy is already running.  Attempting to stop gracefully..."
    sudo caddy stop || {
        echo "Failed to stop Caddy via API, using pkill instead..."
        sudo pkill caddy
    }
else
    echo "Caddy is not running. Starting Caddy..."
fi

# Format the Caddyfile and start Caddy
sudo caddy fmt --overwrite
sudo caddy start --config ./Caddyfile

if pgrep -x "caddy" > /dev/null
then
    echo "Caddy started successfully."
else
    echo "Failed to start Caddy."
    exit 1
fi