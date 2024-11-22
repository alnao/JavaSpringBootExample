FROM openjdk:17.0.1-jdk-slim
#FROM ex openjdk:8-jre-alpine3.9 openjdk:11-jre-slim 
 
# copy the packaged jar file into our docker image
COPY target/esempio01base-0.0.1-SNAPSHOT.jar /esempio01base.jar
 
# set the startup command to execute the jar
CMD ["java", "-jar", "/esempio01base.jar"]


# mvn clean package
# mcn install
# $ docker build -t esempio01base:1.0-SNAPSHOT .
# $ docker run -d -p 5555:5051 esempio01base:1.0-SNAPSHOT
# curl http://localhost:5555/api/response
# docket ps
# docker stop <container_id>

