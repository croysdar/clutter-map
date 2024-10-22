#!/bin/bash

# Navigate to the deployment directory
cd /home/ec2-user/deployment

export PATH=$PATH:/usr/local/bin

# End any existing supervisor processes
sudo pkill supervisord

# set -a
source ./set_env_vars.sh
# set +a

# Start the Spring Boot application using supervisor and the env variables
# sudo bash -c "source ./set_env_vars.sh && supervisord -c ./supervisord.conf"
supervisord -c ./supervisord.conf

# Restart caddy (used to reverse proxy for HTTP)
sudo pkill caddy
sudo caddy fmt --overwrite
sudo caddy start --config ./Caddyfile