## JavaSpringBootExample ExampleMicro1

To compile
 
```
mvn -version
mvn clean install
```

To run in local

```
java -jar target/*.jar

```

To access 

```
http://localhost:5051/api/demoms/riposta
```


### Docker


Create Dockerfile

```
FROM openjdk:11-jre-slim 
COPY target/ExampleMicro1-0.0.1-SNAPSHOT.jar /ExampleMicro1.jar
CMD ["java", "-jar", "/ExampleMicro1.jar"]
```


To create docker image

```
docker build -t examplemicro1:1.0-SNAPSHOT .
```


To run image 

```
docker run -d -p 5052:5051 examplemicro1:1.0-SNAPSHOT
```

url is on 5052 port (port mapping in run command) 

```
http://localhost:5052/api/demoms/response
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

 
