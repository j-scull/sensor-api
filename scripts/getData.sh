#!/bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123

curl -v -X POST "$URL/data/$SENSOR_ID/date" -d '{"dateTime": "2024-01-19T18:29:00.000Z" }' -H 'accept: application/json' -H 'Content-Type: application/json' | jq