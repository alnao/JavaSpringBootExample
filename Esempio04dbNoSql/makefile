# Makefile
.PHONY: help build up down logs clean test setup-dynamo

up:
	chmod +x ./start.sh
	./start.sh

down: ## Ferma tutti i servizi
	docker-compose down

help: ## Mostra questo help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

build: ## Costruisce l'immagine Docker
	docker-compose build

up-docker: ## Avvia tutti i servizi
	docker-compose up -d

up-dynamo: ## Avvia solo i servizi DynamoDB
	docker-compose up -d mongodb dynamodb-local dynamodb-admin microservice-dynamo prometheus grafana

up-mongo: ## Avvia solo i servizi MongoDB
	docker-compose up -d mongodb mongo-express microservice-mongo prometheus grafana

logs: ## Mostra i log di tutti i servizi
	docker-compose logs -f

logs-app: ## Mostra i log del microservizio
	docker-compose logs -f microservice-dynamo microservice-mongo

clean: ## Rimuove tutti i container, volumi e immagini
	docker-compose down -v --rmi all

setup-dynamo: ## Configura DynamoDB LOCALE con tabelle e dati di esempio
	chmod +x config/setup-dynamodb-local-us-east-1.sh 
	./config/setup-dynamodb-local-us-east-1.sh 

test-dynamo: ## Testa il microservizio DynamoDB
	curl -X POST http://localhost:8070/apid/users \
		-H "Content-Type: application/json" \
		-d '{"name":"Test User","email":"test@example.com"}'
	curl http://localhost:8070/apid/users

test-mongo: ## Testa il microservizio MongoDB
	curl -X POST http://localhost:8070/apim/users \
		-H "Content-Type: application/json" \
		-d '{"name":"Test User","email":"test@example.com"}'
	curl http://localhost:8070/apim/users

health: ## Controlla lo stato di salute dei servizi
	curl http://localhost:8070/actuator/health
#	curl http://localhost:8072/actuator/health