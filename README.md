# JavaSpringBootExample
<a href="https://www.alnao.it/javaee/"> 
        <img src="https://img.shields.io/badge/alnao-.it-blue?logo=amazoncloudwatch&logoColor=A6C9E2" height="25px">
        <img src="https://img.shields.io/badge/Java-ED8B00?style=plastic&logo=openjdk&logoColor=white" height="25px"/>
        <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=plastic&logo=SpringBoot&logoColor=white" height="25px" />
</a>


# Progetti:
- **Esempio01base**: esempio base con singola API, Docker-file per eseguire il servizio in immagine docker e su Kubernetes/Minikube
- **Esempio02db**: esempio di CRUD con una tabella *articoli* su MySql, Docker-compose per eseguire il micro-servizio e il DMBS su docker
- **Esempio03dbDockerAWS**: esempio di CRUD con tabella *persone* su MySql, con microservizio e microfrontend in javascript di esempio
	- il backend e il frontend sono disponibili su **DockerHub** a `https://hub.docker.com/repositories/alnao`
	- esecuzione con Minikube per eseguire tutto in locale con anche MySql dentro un immagine docker
	- esecuzione su cluster **AWS-EKS** con creato tramite AWS-CLI
	- esecuzione con CloudFormation su `https://github.com/alnao/AwsCloudFormationExamples/tree/master/Esempio27eks` (con un docker-compose dedicato)
	- esecuzione su cluster **AWS-EKS** con Heml-Chart e ArgoCD
- **Esempio04dbNoSql**: esempio di CRUD *users* su database DynamoDb e DocumentMongo
	- possibilità di eseguire in locale il progetto con Docker-compose e script di avvio *makefile*, avvio anche di Prometheus/Grafana per il monitoraggio
	- possibilità di eseguire su AWS il progetto usando i servizi ECR, DocumentDb, Dynamo e **ECS** con FARGATE con script AWS-CLI senza CloudFormation. *Non funziona la parte con Mongo su DocumentDB*
- **Esempio05testJunit5Mokito**: esempio di CRUD *users* su database MySql 
	- per ogni classe è presente un Test con JUnit5 e con Mokito dove necessario nei Service
	- presente anche un `docker-compose` per eseguire **SonarQube** per la verifica del codice (coverage, smell, bug...)
	- presente la documentazione creata con Swagger
- **Esempio06cacheAndScheduling**: esempio di microservio con Cache e sistema di Scheduling
	- progetto *in sviluppo*
- **Esempio07basicAuth**: esempio di microservizio con servizio di Login di tipo *basic auth*, 
	- esempio con più rotte con permessi diversi su utenti diversi
	- esempio di Swagger configurato con la BasicAuth
- **Esempio08loginJwt**: esempio di microservizio con servizio di Login e servizio di accesso a tabella in maniera sicura
	- progetto *in sviluppo*


## Progetti in revisione:
- ExampleMicro06cache
- ExampleMicro08gestJwt
- ExampleMicro09feign
- ExampleMicro10cloudConfig
- ExampleMicro11asyncCommon
- ExampleMicro11asyncConsumerMagazzino
- ExampleMicro11asyncProducerOrdini
- ExampleMicro12eurekaServer
- ExampleMicro13actuator
- ExampleMicro14ribbon
- ExampleMicro14ribbonClient
- ExampleMicro15zuul
- ExampleMicro16hystrix
- ExampleMicro17turbine


# Comandi base
Maven è uno strumento di automazione della build e gestione dei progetti per progetti Java. Serve a semplificare il processo di compilazione, gestione delle dipendenze, esecuzione dei test e packaging del software, garantendo coerenza e riproducibilità nelle build.
- To compile
	```
	mvn -version
	mvn clean install
	```
	oppure per pulire la cache di maven
	```
	mvn clean dependency:purge-local-repository install
	```
- To run in local
	```
	mvn spring-boot:run
	```
	or 
	```
	java -jar target/*.jar
	```


## Docker (esempio01base)
Un Dockerfile è un file di testo che contiene una serie di istruzioni per costruire un'immagine Docker, ovvero un pacchetto eseguibile che include tutto il necessario per eseguire un'applicazione. Permette di definire in modo riproducibile e automatizzato l'ambiente di runtime, le dipendenze e il codice della tua applicazione.
- **Dockerfile** di esempiop per eseguire un microservizio sviluppato con Spring boot:
	```
	FROM openjdk:17.0.1-jdk-slim
	COPY target/esempio01base-0.0.1-SNAPSHOT.jar /esempio01base.jar
	CMD ["java", "-jar", "/esempio01base.jar"]
	```
- To create docker image
	```
	docker build -t esempio01base:1.0-SNAPSHOT .
	```
- To run image 
	```
	docker run -d -p 5555:5051 esempio01base:1.0-SNAPSHOT
	```
	url is on 5555 port (port mapping in run command) 
	```
	http://localhost:5555/api/response
	```
- To check docker-container
	```
	docker ps
	docker logs <container_id>
	docker port <container_id>
	docker stop <container_id>
	docker rm <container_id>
	docker container prune 
	docker image ls
	docker image rm <image_id>
	```


## Swagger
**Swagger** (ora parte della suite OpenAPI Specification) è un insieme di strumenti open-source che aiuta a progettare, costruire, documentare e consumare API RESTful. Dovrebbe essere usato nei nostri progetti perché genera automaticamente una documentazione interattiva e aggiornata delle API direttamente dal codice sorgente, facilitando la collaborazione tra sviluppatori frontend e backend e migliorando la testabilità delle API.


- Aggiungere nel file `pom.xml` dei progetti
	```
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-api</artifactId>
			<version>2.5.0</version> <!-- Controlla la versione più recente su Maven Central -->
		</dependency>
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.5.0</version> <!-- Controlla la versione più recente su Maven Central -->
		</dependency>
	```
- Add into properties file
	```
		springdoc.api-docs.path=/api-docs
	```
- 
	Check documentation in json format:
	```
	http://localhost:xxxx/api-docs
	```
	or in yaml format
	```
	http://localhost:xxxx/api-docs.yaml
	```
- Complete documentation web-site
	```
	http://localhost:xxxx/swagger-ui.html
	```
- Create class configuration for additional information, see `Esempio05testJunit5Mokito/src/main/java/it/alnao/esempio05/config/OpenApiConfig.java` 
- Use annotations, for examples:
	- Classe tag
		```
		@Tag(name = "Login", description = "API per l'autenticazione degli utenti") // Aggiunge un tag per raggruppare gli endpoint nella UI di Swagger
		public class LoginController { ... }
		```
	- Method operation and parameters:
		``` 
		@Operation(summary = "Effettua il login di un utente",
				description = "Autentica un utente fornendo nome utente e password.") // Descrizione dell'operazione
		@ApiResponses(value = { // Possibili risposte dell'API
				@ApiResponse(responseCode = "200", description = "Login riuscito"),
				@ApiResponse(responseCode = "401", description = "Credenziali non valide")
		})
		@PostMapping
		public ResponseEntity<String> login(
				@Parameter(description = "Nome utente per il login", required = true) // Descrizione del parametro 'nome'
				@RequestParam String nome,
				@Parameter(description = "Password dell'utente", required = true) // Descrizione del parametro 'password'
				@RequestParam String password) { ... }
		```

# Actuator
Spring Boot **Actuator** fornisce endpoint pronti all'uso per monitorare e gestire la tua applicazione in produzione, offrendo informazioni sullo stato di salute, metriche, configurazione e altro. Lo si usa per ottenere visibilità interna sull'applicazione senza dover scrivere codice di monitoraggio ad hoc, facilitando il debugging e l'operatività.
- Add in 'pom.xml'
	```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
	```
- See url 
	- `http://localhost:<port>/actuator`
	- `http://localhost:<port>/actuator/health`
- In `docker-compose` add *health check*:
	```
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8045/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
	```
- In *AWS ECS Task Definition* could add *healt check*:
	```
	"healthCheck": {
		"command": [
			"CMD-SHELL",
			"curl http://localhost:8080/actuator/health || exit 1"
		],
		"interval": 30,
		"timeout": 10,
		"retries": 5
	},	
	```


# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*




