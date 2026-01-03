#!/bin/bash

# Test script for Gemini recommendation API
# This script tests the POST /api/recommendations endpoint

echo "Testing Gemini Recommendation API..."

# Test 1: Request with latitude/longitude
echo "Test 1: Request with lat/lon coordinates"
curl -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 12.9716,
    "longitude": 77.5946,
    "season": "monsoon",
    "farmSize": "small"
  }' | jq .

echo -e "\n\n"

# Test 2: Request with district name
echo "Test 2: Request with district name"
curl -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "district": "BANGALORE",
    "preferredCrops": ["Rice", "Wheat"],
    "soilType": "clay",
    "season": "kharif"
  }' | jq .

echo -e "\n\n"

# Test 3: Health check
echo "Test 3: Health check"
curl -X GET http://localhost:8080/api/recommendations/health

echo -e "\n\nTests completed!"