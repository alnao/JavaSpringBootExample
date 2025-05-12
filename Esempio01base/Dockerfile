FROM openjdk:17.0.1-jdk-slim
#FROM ex openjdk:8-jre-alpine3.9 openjdk:11-jre-slim 
 
# copy the packaged jar file into our docker image
COPY target/esempio01base-1.1.0-SNAPSHOT.jar /esempio01base.jar
 
# set the startup command to execute the jar
CMD ["java", "-jar", "/esempio01base.jar"]


# mvn clean package
# mcn install
# $ docker build -t esempio01base:1.0-SNAPSHOT .
# $ docker run -d -p 5555:5051 esempio01base:1.0-SNAPSHOT
# curl http://localhost:5555/api/response
# docket ps
# docker stop <container_id>

# TO https://app.docker.com/
# SEE https://hub.docker.com/repository/docker/alnao/esempio01/general
# docker login
# docker tag esempio01:1.0 alnao/esempio01:1.0
# docker push alnao/esempio01:1.0

# deployments.yml e service.yml to Kubernetes
# kubectl apply -f deployments.yml
# kubectl apply -f service.yml
# kubectl get pods esempio01
# kubectl get all -l app=esempio01
