#!/bin/bash

# # Navigate to the deployment directory
cd /home/ec2-user/deployment

# Start the Spring Boot application using supervisor
supervisord -c supervisord.conf