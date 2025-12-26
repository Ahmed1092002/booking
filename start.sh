#!/bin/bash

# Start Redis in the background
echo "Starting Redis..."
redis-server --daemonize yes

# Start the Java Application
echo "Starting Spring Boot App..."
java -jar app.jar
