#!/bin/bash

echo "=== Testing chunked encoding fix ==="

# Stop the services
echo "Stopping services..."
docker-compose stop modules-gateway

# Rebuild only modules-gateway
echo "Rebuilding modules-gateway..."
docker-compose build --no-cache modules-gateway

# Start the service
echo "Starting modules-gateway..."
docker-compose up -d modules-gateway

echo "Waiting for service to start (10 seconds)..."
sleep 10

echo "=== Testing the fix ==="
echo "Testing: GET http://localhost:8083/api/v1/modules/temperature-module-factory-001/proxy/temperature?location=kitchen"
echo ""

# Test the request
curl -v -X GET "http://localhost:8083/api/v1/modules/temperature-module-factory-001/proxy/temperature?location=kitchen"

echo ""
echo "=== Check logs for cleaned headers ==="
echo "Look for 'Cleaned response headers:' and 'Final response headers:' in the logs"
echo ""
echo "Run: docker-compose logs -f modules-gateway"
