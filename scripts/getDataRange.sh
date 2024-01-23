#! /bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID="123"

curl -v "$URL/data/$SENSOR_ID/range?from=2024-01-23T22:05:00.000Z&until=2024-01-23T23:00:00.000Z" | jq