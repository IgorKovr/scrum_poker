#!/bin/bash

# Start nginx in background
nginx -g 'daemon off;' &

# Start Spring Boot application
java -jar /app/app.jar 