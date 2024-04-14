# Esempio01base
Creato e scaricato dal sito [start.spring.io](https://start.spring.io/) selezionando la versione "3.3.0" con "Maven" e "Java17".

## Comandi base
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





# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*


