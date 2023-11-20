#! /bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123

curl -v -X GET "$URL/sensor/$SENSOR_ID" -H 'accept: application/json' | jq
