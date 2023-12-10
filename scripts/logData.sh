#! /bin/bash

# Test logData endpoint

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123

curl -v -X 'POST' "$URL/data" -H 'accept: application/json' -H 'Content-Type: application/json' \
  -d '{"sensorId":"123", "temperature":9, "humidity":87}'

