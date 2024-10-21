#!/bin/bash

# # Navigate to the deployment directory
cd /home/ec2-user/deployment

export PATH=$PATH:/usr/local/bin

# End any existing supervisor processes
sudo pkill supervisord

# Start the Spring Boot application using supervisor
sudo supervisord -c supervisord.conf

# Restart caddy (used to reverse proxy for HTTP)
sudo pkill caddy
sudo caddy fmt --overwrite
sudo caddy start --config ./Caddyfile