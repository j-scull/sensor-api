#! /bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID=123
START="1700092800"
STOP="1700179200"

curl -v "$URL/getData/$SENSOR_ID/range?start=$START&stop=$STOP" -H 'accept: application/json' | jq