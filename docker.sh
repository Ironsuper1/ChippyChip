#!/bin/bash

# Start the containers in detached mode
docker-compose up -d

# Get the container ID of the service you want to execute the command in
CONTAINER_ID=$(docker-compose ps -q app)

# Execute bash command in the container
docker exec -it "$CONTAINER_ID" bash