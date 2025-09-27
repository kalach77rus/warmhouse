#!/bin/bash

echo "=== Rebuilding services with detailed logging ==="

# Stop the services
echo "Stopping services..."
docker-compose stop modules-gateway temperature-api

# Clean and rebuild the services
echo "Cleaning and rebuilding services..."
docker-compose build --no-cache modules-gateway temperature-api

# Start the services
echo "Starting services..."
docker-compose up -d modules-gateway temperature-api

echo "Services rebuilt and restarted with detailed logging!"
echo ""
echo "=== Testing Instructions ==="
echo "1. Wait for services to start (about 30 seconds)"
echo "2. Test the problematic request:"
echo "   curl -v -X GET 'http://localhost:8083/api/v1/modules/temperature-module-factory-001/proxy/temperature?location=kitchen'"
echo ""
echo "3. Check logs:"
echo "   docker-compose logs -f modules-gateway"
echo "   docker-compose logs -f temperature-api"
echo ""
echo "4. Look for these log patterns:"
echo "   - '=== INCOMING REQUEST ==='"
echo "   - '=== PROXY REQUEST START ==='"
echo "   - '=== TEMPERATURE API REQUEST ==='"
echo "   - 'Transfer-Encoding' headers"
echo "   - 'Invalid character in chunk size' errors"
