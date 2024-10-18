#!/bin/bash

# # Navigate to the deployment directory
cd /home/ec2-user/deployment

export PATH=$PATH:/usr/local/bin

# Start the Spring Boot application using supervisor
supervisord -c supervisord.conf