#!/bin/bash

echo "Checking if Java 21 is installed..."
if ! java -version 2>&1 | grep -q "21"; then
    echo "Java 21 not found. Installing Java 21..."
    sudo yum install -y java-21-amazon-corretto-headless.x86_64 &> /dev/null || { echo "Failed to install Java 21. Exiting."; exit 1; }
fi

echo "Checking if pip is installed..."
if ! command -V pip &> /dev/null; then
    echo "pip not found. Installing pip..."
    sudo yum install python3-pip &> /dev/null || { echo "Failed to install pip. Exiting."; exit 1; }
fi

echo "Checking if supervisor is installed..."
if ! command -v supervisord &> /dev/null; then
    echo "supervisor not found. Installing supervisor via pip..." || { echo "Failed to install supervisor. Exiting."; exit 1; }
    sudo pip install supervisor &> /dev/null
fi

echo "Checking if Caddy is installed..."
if ! command -v &> /dev/null; then
    echo "Caddy not found. Installing Caddy..."
    sudo yum -y install yum-plugin-copr &> /dev/null || { echo "Failed to install yum-plugin-copr. Exiting."; exit 1; }
    sudo yum -y copr enable @caddy/caddy epel-8-$(arch)  &> /dev/null || { echo "Failed to enable Caddy repo. Exiting."; exit 1; }
    sudo yum -y install caddy  &> /dev/null || { echo "Failed to install Caddy. Exiting."; exit 1; }
    echo "Setting Caddy to bind to port 80/443..."
    sudo setcap cap_net_bind_service=+ep $(which caddy)  &> /dev/null || { echo "Failed to set Caddy capabilities. Exiting."; exit 1; }
fi

echo "All dependencies are installed successfully."
exit 0