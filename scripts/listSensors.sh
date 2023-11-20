#! /bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123
YEAR=2023
MONTH=11
DATE=17
HOUR=20

curl -v -X GET "$URL/sensors" -H 'accept: application/json' | jq
