#!/bin/bash

# Navigate to the project directory
cd /home/ec2-user/deployment

# Install Java 21 if not installed
if ! java -version 2>&1 | grep -q "21"; then
    sudo yum install -y java-21-amazon-corretto-headless.x86_64 
fi

if ! command -V pip 2>&1; then
    sudo yum install python3-pip
fi

if ! command -v supervisord 2>&1; then
    sudo pip install supervisor
fi

if ! command -v /usr/local/bin/supervisord &> /dev/null; then
    sudo pip3 install supervisor
fi

if ! command -v 2>&1; then
    sudo yum -y install yum-plugin-copr
    sudo yum -y copr enable @caddy/caddy epel-8-$(arch)
    sudo yum -y install caddy
    sudo setcap cap_net_bind_service=+ep $(which caddy)
fi

# Ensure the JAR is executable (if needed)
sudo chmod +x clutter-map-0.0.1-SNAPSHOT.jar