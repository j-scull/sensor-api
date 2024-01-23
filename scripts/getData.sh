#!/bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123

curl -v "$URL/data/$SENSOR_ID/date?dateTime=2024-01-23T22:29:00.000Z" | jq