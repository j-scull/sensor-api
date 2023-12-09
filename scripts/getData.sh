#!/bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123
YEAR=2023
MONTH=12
DATE=08
HOUR=22

#curl -v -X GET "$URL/getData/$SENSOR_ID?year=$YEAR&month=$MONTH&date=$DATE&hour=$HOUR" -H 'accept: application/json' | jq

curl -v -X GET "$URL/getData/$SENSOR_ID?year=$YEAR&month=$MONTH&date=$DATE" -H 'accept: application/json' | jq