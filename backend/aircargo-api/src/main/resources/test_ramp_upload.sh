#!/usr/bin/env bash

# IDs reales de prueba consistentes con tu esquema jerárquico
FLIGHT_ID="33500000-0000-0000-0000-000000000000"
AIRLINE_ID="11111111-1111-1111-1111-111111111111"
FILE_PATH="/home/manolov/Downloads/manifiesto_vuelo_767.xlsx"

echo "[*] Transmitiendo manifiesto de rampa al microservicio de Load Planning..."
echo "----------------------------------------------------------------------"

curl -X POST "http://localhost:8080/api/load-planning/flight/${FLIGHT_ID}/upload-manifest" \
  -F "airlineId=${AIRLINE_ID}" \
  -F "file=@${FILE_PATH}" \
  -v

echo -e "\n----------------------------------------------------------------------"
echo "[*] Petición finalizada."
