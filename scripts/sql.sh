#!/bin/bash

# Load .env from the same directory as the script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$REPO_ROOT/.env"

if [ -f "$ENV_FILE" ]; then
    export $(grep -v '^#' "$ENV_FILE" | xargs)
else
    echo "Error: .env file not found at $ENV_FILE"
    exit 1
fi

if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
    echo "Error: MYSQL_ROOT_PASSWORD is not set in .env"
    exit 1
fi

# Check if expect is installed
if ! command -v expect &> /dev/null; then
    echo "Error: 'expect' is not installed."
    echo "Install it with: sudo pacman -S expect   (Arch)"
    echo "                 sudo apt install expect  (Debian/Ubuntu)"
    exit 1
fi

# Check if the db container is running
cd "$SCRIPT_DIR"
RUNNING=$(docker compose ps --status running --quiet db 2>/dev/null)

if [ -z "$RUNNING" ]; then
    echo "MySQL service is not running. Starting..."
    docker compose up -d db

    echo "Waiting for MySQL to become healthy..."
    RETRIES=30
    COUNT=0
    while [ $COUNT -lt $RETRIES ]; do
        HEALTH=$(docker inspect --format='{{.State.Health.Status}}' "$(docker compose ps -q db)" 2>/dev/null)
        if [ "$HEALTH" = "healthy" ]; then
            echo "MySQL is healthy and ready."
            break
        fi
        COUNT=$((COUNT + 1))
        echo "  Still waiting... ($COUNT/$RETRIES)"
        sleep 3
    done

    if [ $COUNT -eq $RETRIES ]; then
        echo "Error: MySQL did not become healthy in time. Check logs with: docker compose logs db"
        exit 1
    fi
else
    echo "MySQL service is already running."
fi

echo "Opening MySQL console..."
clear
echo "--------------------------"
echo "To exit console run 'exit'"
echo "--------------------------"
echo " "
expect -c "
    spawn docker compose exec db mysql -u root -p
    expect \"Enter password:\"
    send \"${MYSQL_ROOT_PASSWORD}\r\"
    expect \"mysql>\"
    send \"use ${MYSQL_DATABASE};\r\"
    interact
"
