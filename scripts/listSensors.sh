#! /bin/bash

PORT="9090"
URL="http://localhost:$PORT"

curl -v -X GET "$URL/sensors" -H 'accept: application/json' | jq
