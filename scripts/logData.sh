#! /bin/bash

# Test logData endpoint

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123

curl -v -X 'POST' "$URL/logData" -H 'accept: application/json' -H 'Content-Type: application/json' \
  -d '{"sensorId":"f248610b-fcfa-43ff-bf53-a2d7df35be05", "temperature":9, "humidity":87}'

