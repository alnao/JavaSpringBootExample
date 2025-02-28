# Esempio01base
Progetto base creato e scaricato dal sito [start.spring.io](https://start.spring.io/) selezionando la versione "3.3.0" con "Maven" e "Java17".

Per questo progetto è indispensabile avere la versione 17 di Java, con precedenti versioni non funziona.


## Comandi base
* per compilare
    ```
    mvn -version
    mvn clean install
    ```
* per eseguire in locale usando maven
    ```
    mvn spring-boot:run
    ```
* per eseguire in locale usando il comando java
    ```
    java -jar target/*.jar
    ```
* per eseguire test di chiamata alla API esposta
    ```
    curl http://localhost:5051/api/response
    ```
* per eseguire la sequenza di test-unit
    ```
    mvn test
    ```

## Docker (esempio01base)

* definizione del **Dockerfile**
    ```
    FROM openjdk:17.0.1-jdk-slim
    COPY target/esempio01base-0.0.1-SNAPSHOT.jar /esempio01base.jar
    CMD ["java", "-jar", "/esempio01base.jar"]
    ```
* per creare l'immagine
    ```
    docker build -t esempio01base:1.0-SNAPSHOT .
    ```
* per eseguire l'immagine esponendo il servizio su porta 5555
    ```
    docker run -d -p 5555:5051 esempio01base:1.0-SNAPSHOT
    curl http://localhost:5555/api/response
    ```
* comando docker utili
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


