#!/bin/bash

# Define the deployment directory and ensure it exists
DEPLOY_DIR="/home/ec2-user/deployment"
if [ ! -d "$DEPLOY_DIR" ]; then
    echo "Deployment directory $DEPLOY_DIR does not exist. Exiting."
    exit 1
fi

# Navigate to the deployment directory
cd $DEPLOY_DIR || { echo "Failed to navigate to $DEPLOY_DIR"; exit 1; }

export PATH=$PATH:/usr/local/bin

# Source environment variables if the file exists
if [ -f ./set_env_vars.sh ]; then
    source ./set_env_vars.sh
else
    echo "set_env_vars.sh not found. Exiting."
    exit 1
fi

# Ensure the JAR is executable (if needed)
sudo chmod +x clutter-map-0.0.1-SNAPSHOT.jar

# Check if supervisord is running
if pgrep -x "supervisord" > /dev/null
then
    echo "Running reread and update on running supervisor."
    sudo supervisorctl reread
    sudo supervisorctl update
else
    echo "Supervisord is not running. Starting supervisord..."

    # Start the Spring Boot application using supervisor and the env variables
    supervisord -c "$DEPLOY_DIR/supervisord.conf"
    
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
    echo "Checking Spring Boot logs for errors..."
    # Check application logs
    tail -n 50 /home/ec2-user/logs/clutter-map.out.log
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
if sudo caddy fmt --overwrite; then
    echo "Caddyfile formatted successfully."
else
    echo "Failed to format Caddyfile. Exiting."
    exit 1
fi

# Start Caddy
if sudo caddy start --config "$DEPLOY_DIR/Caddyfile"; then
    echo "Caddy started successfully."
else
    echo "Failed to start Caddy. Exiting."
    exit 1
fi

# Ensure the script closes STDOUT properly by exiting with 0
exit 0