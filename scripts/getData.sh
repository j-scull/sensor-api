#!/bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123
YEAR=2024
MONTH=01
DATE=7
HOUR=22

curl -v -X POST "$URL/data/$SENSOR_ID/date" -d '{"year": "2024", "month":"01", "date":"07"}' -H 'accept: application/json' -H 'Content-Type: application/json' | jq
#curl -v -X POST "$URL/data/$SENSOR_ID/date" -d '{"year": "2023", "month":"12", "date":"17", "hour":"22"}' -H 'accept: application/json' -H 'Content-Type: application/json' | jq