#!/bin/bash

# Nota questo script non è autonomo: richiede che il sistema di gestione annotazioni sia in esecuzione eseguito da altri

# Script di test per la funzionalità di prenotazione annotazioni

BASE_URL="http://localhost:8082/api"
#se mi arriva un parametro lo uso come base url
if [ ! -z "$1" ]; then
  BASE_URL="$1/api"
fi

ANNOTATION_ID=""

# Colori per output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Prenotazione Annotazioni${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. Login utente admin
echo -e "${YELLOW}1. Login utente admin...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Errore: Login fallito${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Login admin effettuato con successo${NC}"
echo ""

# 2. Login utente alnao
echo -e "${YELLOW}2. Login utente alnao...${NC}"
LOGIN_RESPONSE_2=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"alnao","password":"bellissimo"}')

TOKEN_2=$(echo $LOGIN_RESPONSE_2 | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN_2" ]; then
    echo -e "${RED}✗ Errore: Login fallito per alnao${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Login alnao effettuato con successo${NC}"
echo ""

# 3. Creazione annotazione di test
echo -e "${YELLOW}3. Creazione annotazione di test...${NC}"
TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
VALORE_NOTA="Annotazione per test di prenotazione creata $TIME"
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/annotazioni" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "utente": "admin", 
    "valoreNota": "'"$VALORE_NOTA"'",
    "descrizione": "Test prenotazione",
    "categoria": "TEST",
    "pubblica": true
  }')

ANNOTATION_ID=$(echo $CREATE_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)

if [ -z "$ANNOTATION_ID" ]; then
    echo -e "${RED}✗ Errore: Creazione annotazione fallita${NC}"
    echo "Risposta: $CREATE_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✓ Annotazione creata con ID: $ANNOTATION_ID${NC}"
echo ""

# 4. Verifica stato prenotazione (dovrebbe essere libera)
echo -e "${YELLOW}4. Verifica stato iniziale prenotazione...${NC}"
STATO_RESPONSE=$(curl -s -X GET "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota/stato" \
  -H "Authorization: Bearer $TOKEN")

echo "Risposta: $STATO_RESPONSE"
echo ""

# 5. Prenotazione annotazione da parte di admin
echo -e "${YELLOW}5. Prenotazione annotazione da parte di admin...${NC}"
PRENOTA_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"utente":"admin"}')

HTTP_CODE=$(echo "$PRENOTA_RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$PRENOTA_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Prenotazione effettuata con successo${NC}"
    echo "Risposta: $BODY"
else
    echo -e "${RED}✗ Errore nella prenotazione (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

# 6. Verifica stato prenotazione (dovrebbe essere bloccata da admin)
echo -e "${YELLOW}6. Verifica stato prenotazione (dovrebbe essere bloccata)...${NC}"
STATO_RESPONSE=$(curl -s -X GET "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota/stato" \
  -H "Authorization: Bearer $TOKEN")

echo "Risposta: $STATO_RESPONSE"
echo ""

# 7. Tentativo di prenotazione da parte di alnao (dovrebbe fallire)
echo -e "${YELLOW}7. Tentativo di prenotazione da parte di alnao (dovrebbe fallire)...${NC}"
PRENOTA_CONFLICT=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_2" \
  -d '{"utente":"alnao"}')

HTTP_CODE=$(echo "$PRENOTA_CONFLICT" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$PRENOTA_CONFLICT" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" = "409" ]; then
    echo -e "${GREEN}✓ Correttamente bloccato (HTTP 409)${NC}"
    echo "Risposta: $BODY"
else
    echo -e "${RED}✗ Errore: prenotazione non dovrebbe essere possibile (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

# 8. Tentativo di modifica da parte di alnao (dovrebbe fallire)
echo -e "${YELLOW}8. Tentativo di modifica da parte di alnao (dovrebbe fallire)... a volte ci mette un po'... ${NC}"
UPDATE_CONFLICT=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X PUT "$BASE_URL/annotazioni/$ANNOTATION_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_2" \
  -d '{
    "id": "'$ANNOTATION_ID'",
    "utente": "alnao",
    "valoreNota": "Tentativo modifica da alnao",
    "descrizione": "Test modifica bloccata",
    "categoria": "TEST"
  }')

HTTP_CODE=$(echo "$UPDATE_CONFLICT" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$UPDATE_CONFLICT" | sed '/HTTP_CODE/d')
if [ "$HTTP_CODE" = "409" ]; then
    echo -e "${GREEN}✓ Modifica correttamente bloccata (HTTP 409)${NC}"
    echo "Risposta: $BODY"
else
    echo -e "${RED}✗ Errore: modifica non dovrebbe essere possibile (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

# 9. Modifica da parte di admin (dovrebbe avere successo)
echo -e "${YELLOW}9. Modifica da parte di admin (dovrebbe avere successo)... a volte ci mette un po'... ${NC}"
UPDATE_SUCCESS=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X PUT "$BASE_URL/annotazioni/$ANNOTATION_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "id": "'$ANNOTATION_ID'",
    "utente": "admin",
    "valoreNota": "Modifica da admin",
    "descrizione": "Test modifica con lock",
    "categoria": "TEST"
  }')

HTTP_CODE=$(echo "$UPDATE_SUCCESS" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$UPDATE_SUCCESS" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Modifica effettuata con successo${NC}"
else
    echo -e "${RED}✗ Errore nella modifica (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

# 10. Attesa 2 secondi e poi riprenoto e poi ri-aspetto 2 secondi!
echo -e "${YELLOW}10. Attesa 2 secondi...${NC}"
sleep 2
echo ""
echo -e "${YELLOW}10. Prenotazione annotazione da parte di admin...${NC}"
PRENOTA_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"utente":"admin"}')
HTTP_CODE=$(echo "$PRENOTA_RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$PRENOTA_RESPONSE" | sed '/HTTP_CODE/d')
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Prenotazione effettuata con successo${NC}"
    echo "Risposta: $BODY"
else
    echo -e "${RED}✗ Errore nella prenotazione (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""
echo -e "${YELLOW}10. Attesa 2 secondi...${NC}"
sleep 2

# 11. Rilascio prenotazione da parte di admin
echo -e "${YELLOW}11. Rilascio prenotazione da parte di admin...${NC}"
RILASCIO_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X DELETE "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"utente":"admin"}')

HTTP_CODE=$(echo "$RILASCIO_RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)

if [ "$HTTP_CODE" = "204" ]; then
    echo -e "${GREEN}✓ Lock rilasciato con successo${NC}"
else
    echo -e "${RED}✗ Errore nel rilascio (HTTP $HTTP_CODE)${NC}"
    BODY=$(echo "$RILASCIO_RESPONSE" | sed '/HTTP_CODE/d')
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

# 12. Verifica stato prenotazione (dovrebbe essere libera)
echo -e "${YELLOW}12. Verifica stato finale prenotazione (dovrebbe essere libera)...${NC}"
STATO_RESPONSE=$(curl -s -X GET "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota/stato" \
  -H "Authorization: Bearer $TOKEN")

echo "Risposta: $STATO_RESPONSE"
echo ""

# 13. Prenotazione da parte di alnao (ora dovrebbe avere successo)
echo -e "${YELLOW}13. Prenotazione da parte di alnao (ora dovrebbe avere successo) per 20 secondi...${NC}"
PRENOTA_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_2" \
  -d '{"utente":"alnao" , "secondi":20}')

HTTP_CODE=$(echo "$PRENOTA_RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$PRENOTA_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Prenotazione effettuata con successo${NC}"
    echo "Risposta: $BODY"
else
    echo -e "${RED}✗ Errore nella prenotazione (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

# 14. Test timeout - Prenotazione con scadenza automatica
echo -e "${YELLOW}14. Test timeout automatico (30 secondi)... aspetto 30 secondi e poi provo a bloccare con admin ${NC}"
sleep 30
PRENOTA_TIMEOUT=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/annotazioni/$ANNOTATION_ID/prenota" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"utente":"admin"}')  

HTTP_CODE=$(echo "$PRENOTA_TIMEOUT" | grep "HTTP_CODE" | cut -d':' -f2)
BODY=$(echo "$PRENOTA_TIMEOUT" | sed '/HTTP_CODE/d')
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ Prenotazione effettuata con successo dopo timeout${NC}"
    echo "Risposta: $BODY"
else
    echo -e "${RED}✗ Errore nella prenotazione dopo timeout (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

# Cleanup
echo -e "${YELLOW}15. Pulizia: eliminazione annotazione di test...${NC}"
DELETE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X DELETE "$BASE_URL/annotazioni/$ANNOTATION_ID" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$DELETE_RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)

if [ "$HTTP_CODE" = "204" ]; then
    echo -e "${GREEN}✓ Annotazione eliminata${NC}"
else
    echo -e "${RED}✗ Errore nell'eliminazione (HTTP $HTTP_CODE)${NC}"
    echo "Risposta: $BODY"
    exit 1
fi
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}✓ Test completati${NC} ✓"
echo -e "${BLUE}========================================${NC}"
