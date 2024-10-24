#!/bin/bash

# Define the deployment directory and ensure it exists
DEPLOY_DIR="/home/ec2-user/deployment"
SUPERVISOR_CONF="$DEPLOY_DIR/supervisord.conf"
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

# --------- SUPERVISOR --------- #
# Ensure the directory for Supervisor's PID file exists
SUPERVISOR_DIR="/home/ec2-user/supervisor"
if [ ! -d "$SUPERVISOR_DIR" ]; then
    echo "Creating directory for Supervisor PID file..."
    mkdir -p "$SUPERVISOR_DIR"
fi

# Stop Supervisor gracefully (stopping all processes it manages)
echo "Stopping all Supervisor processes..."
sudo supervisorctl stop all || { echo "Failed to stop Supervisor processes."; }

# Stop the Supervisor process itself
echo "Stopping supervisord..."
sudo pkill supervisord  # Kill supervisord

# Wait a bit to ensure supervisord has stopped completely
sleep 3

# Start Supervisor again with the latest configuration
echo "Starting supervisord with the latest configuration..."
supervisord -c "$SUPERVISOR_CONF"

# Wait for supervisord to fully start
sleep 3

# Verify that supervisord started successfully
if pgrep -x "supervisord" > /dev/null; then
    echo "Supervisord restarted successfully."
else
    echo "Failed to start supervisord."
    exit 1
fi

echo "Supervisor has been restarted and is running."

# --------- WAIT FOR APP --------- #
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

# --------- CADDY --------- #
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
if [ -f "$DEPLOY_DIR/Caddyfile" ]; then
    echo "Formatting the Caddyfile..."
    if sudo caddy fmt --overwrite; then
        echo "Caddyfile formatted successfully."
    else
        echo "Failed to format Caddyfile. Exiting."
        exit 1
    fi
else
    echo "Caddyfile not found. Exiting."
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