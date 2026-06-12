#!/bin/bash
# HotelOS - Quick Start Script
# Usage: ./start.sh

echo "═══════════════════════════════════════"
echo "  HotelOS - Starting All Services"
echo "═══════════════════════════════════════"

# Step 1: Build
echo "[1/2] Building all modules..."
mvn clean install -DskipTests -q
if [ $? -ne 0 ]; then
    echo "BUILD FAILED. Check errors above."
    exit 1
fi
echo "✓ Build successful"

# Step 2: Start services in background
echo "[2/2] Starting services..."

cd reception-service    && mvn spring-boot:run -q &
sleep 3
cd ../housekeeping-service && mvn spring-boot:run -q &
sleep 2
cd ../room-service       && mvn spring-boot:run -q &
sleep 2
cd ../maintenance-service && mvn spring-boot:run -q &
sleep 2
cd ../dashboard          && mvn spring-boot:run -q &
cd ..

echo ""
echo "═══════════════════════════════════════"
echo "  All services started!"
echo "  Dashboard: http://localhost:8085/login"
echo "  Login: staff / hotel123"
echo "═══════════════════════════════════════"

wait
