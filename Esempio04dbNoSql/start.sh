#!/bin/bash
# start.sh

echo "> Starting Microservice Environment..."

# Crea le directory necessarie
mkdir -p config/grafana/dashboards
mkdir -p config/grafana/datasources

# dynamodb-data
# mkdir -p dynamodb-data
# chmod 777 dynamodb-data  # O usa chown con il tuo utente/gruppo

# Costruisce e avvia i servizi
echo "> Building Docker images..."
docker-compose build

echo "> Starting services..."
docker-compose up -d

echo "> Waiting for services to be ready..."
sleep 30

# Setup DynamoDB
echo "> Setting up DynamoDB tables..."
if command -v aws &> /dev/null; then
    make setup-dynamo
else
    echo "AWS CLI not found. Please install it to setup DynamoDB tables automatically."
fi

echo "> Environment is ready!"
echo ""
echo "> Available services:"
echo "   - Microservice:            http://localhost:8080"
echo "   - DynamoDB Admin:          http://localhost:8001"
echo "   - MongoDB Express:         http://localhost:8081 (admin/pass)"
echo "   - Prometheus:              http://localhost:9090 (status -> targets )"
echo "   - Grafana:                 http://localhost:3000 (admin/admin)"
echo ""
echo "> Test the services:"
echo "   make test-dynamo"
echo "   make test-mongo"
echo ""
echo "> View logs:"
echo "   make logs"