#!/bin/bash

# 1. Start Redis (Background)
echo "Starting Redis..."
redis-server --daemonize yes

# 2. Start Nginx (Background)
echo "Starting Nginx..."
nginx

# 3. Start Java App (Background) - We need to wait for it before Nginx is useful, but Nginx handles Retries
echo "Starting Spring Boot on Port 8081..."
java -jar app.jar
