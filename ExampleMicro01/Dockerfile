FROM openjdk:11-jre-slim 
#FROM openjdk:8-jre-alpine3.9
 
# copy the packaged jar file into our docker image
COPY target/ExampleMicro1-0.0.1-SNAPSHOT.jar /ExampleMicro1.jar
 
# set the startup command to execute the jar
CMD ["java", "-jar", "/ExampleMicro1.jar"]


# mvn clean package
# mcn install
# $ docker build -t examplemicro1:1.0-SNAPSHOT .
# $ docker run -d -p 5051:5052 examplemicro1:1.0-SNAPSHOT
# http://localhost:5051/api/demoms/response
# docket ps
# docker stop <container_id>

