#!/bin/sh
set -e

# Sostituisce le variabili d'ambiente nei template
envsubst < /usr/share/nginx/html/config.js.template > /usr/share/nginx/html/config.js

# Avvia nginx
nginx -g 'daemon off;'