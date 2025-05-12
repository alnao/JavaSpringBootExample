# JavaSpringBootExample
<a href="https://www.alnao.it/javaee/"> 
        <img src="https://img.shields.io/badge/alnao-.it-blue?logo=amazoncloudwatch&logoColor=A6C9E2" height="25px">
        <img src="https://img.shields.io/badge/Java-ED8B00?style=plastic&logo=openjdk&logoColor=white" height="25px"/>
        <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=plastic&logo=SpringBoot&logoColor=white" height="25px" />
</a>

# Progetti:
- **Esempio01base**: esempio base con singola API, Docker-file per eseguire il servizio in immagine docker e su Kubernetes/Minikube
- **Esempio02db**: esempio di CRUD con una tabella *articoli* su MySql, Docker-compose per eseguire il micro-servizio e il DMBS su docker

## Progetti in revisione:
- ExampleMicro03postgres
- ExampleMicro04mongo
- ExampleMicro05dynamo
- ExampleMicro06cache
- ExampleMicro07basicAuth
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
- ExampleMicro18TDDJunit4
- ExampleMicro19TDDJunit5
- ExampleMicro20mockito

# Comandi base

To compile
 
```
mvn -version
mvn clean install
```

To run in local

```
mvn spring-boot:run
```


or 
```
java -jar target/*.jar
```


## To test

```
http://localhost:5051/api/response
```


## Docker (esempio01base)

**Dockerfile**

```
FROM openjdk:17.0.1-jdk-slim
COPY target/esempio01base-0.0.1-SNAPSHOT.jar /esempio01base.jar
CMD ["java", "-jar", "/esempio01base.jar"]
```


To create docker image

```
docker build -t esempio01base:1.0-SNAPSHOT .
```


To run image 

```
docker run -d -p 5555:5051 esempio01base:1.0-SNAPSHOT
```

url is on 5555 port (port mapping in run command) 

```
http://localhost:5555/api/response
```


To check docker-container

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


## Swagger (ExampleMicro5dynamo)


Add at POM.xml

```
	<dependency>
	    <groupId>org.springdoc</groupId>
	    <artifactId>springdoc-openapi-ui</artifactId>
	    <version>1.6.4</version>
	</dependency>
```
Add into properties file

```
	springdoc.api-docs.path=/api-docs
```

Check documentation in json format:
```
http://localhost:5051/api-docs
```
or in yaml format
```
http://localhost:5051/api-docs.yaml
```

Complete documentation web-site
```
http://localhost:5051/swagger-ui.html
```

See swagget3 documentation on 
```
https://swagger.io/specification/
```




# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*




