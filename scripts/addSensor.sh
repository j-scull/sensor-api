#!/bin/bash

PORT="9090"
URL="http://localhost:$PORT"

curl -v "$URL/sensors/add" -H 'accept: application/json' -H 'Content-Type: application/json' \
  -d '{ "sensorId": "123", "location": "somewhere" }'