#! /bin/bash

PORT="9090"
URL="http://localhost:$PORT"
SENSOR_ID="123"
FROM_YEAR="2023"
FROM_MONTH="12"
FROM_DATE="01"
FROM_HOUR="00"
UNTIL_YEAR="2023"
UNTIL_MONTH="12"
UNTIL_DATE="15"
UNTIL_HOUR="23"

# With hour specified
#curl -v -X POST "$URL/data/$SENSOR_ID/date/range" \
# -d '{"from": {"year": "2023", "month":"12", "date":"10", "hour":"22"}, "until": {"year": "2023", "month":"12", "date":"10", "hour":"23"}}' \
# -H 'accept: application/json' -H 'Content-Type: application/json' | jq

# Without hour specified
curl -v -X POST "$URL/data/$SENSOR_ID/date/range" \
 -d '{"from": {"year": "2023", "month":"12", "date":"08"}, "until": {"year": "2023", "month":"12", "date":"10"}}' \
 -H 'accept: application/json' -H 'Content-Type: application/json' | jq