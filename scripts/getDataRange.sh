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
curl -v "$URL/getData/$SENSOR_ID/range?fromYear=$FROM_YEAR&fromMonth=$FROM_MONTH&fromDate=$FROM_DATE&fromHour=$FROM_HOUR&untilYear=$UNTIL_YEAR&untilMonth=$UNTIL_MONTH&untilDate=$UNTIL_DATE&untilHour=$UNTIL_HOUR" \
 -H 'accept: application/json' | jq

# Without hour specified
#curl -v "$URL/getData/$SENSOR_ID/range?fromYear=$FROM_YEAR&fromMonth=$FROM_MONTH&fromDate=$FROM_DATE&untilYear=$UNTIL_YEAR&untilMonth=$UNTIL_MONTH&untilDate=$UNTIL_DATE" \
# -H 'accept: application/json' | jq