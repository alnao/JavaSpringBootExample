# Esempio02db
Progetto base creato e scaricato dal sito [start.spring.io](https://start.spring.io/) selezionando la versione "3.3.0" con "Maven" e "Java17".
Poi aggiunte le classi Controller, Repository, Entity e Service.

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
    curl http://localhost:5051/api/articoli/check
    curl http://localhost:5051/api/articoli/all
    ```
* per eseguire la sequenza di test-unit
    ```
    mvn test
    ```

## Docker 
* definizione nel docker-compose
    ```
    services:
    mysql:
        image: mysql:8.0
        environment:
        MYSQL_ROOT_PASSWORD: xxxxxx
        MYSQL_DATABASE: MyWeb
        MYSQL_USER: root
        MYSQL_PASSWORD: xxxxxx
        ports:
        - "3306:3306"
        volumes:
        - mysql_data:/var/lib/mysql

    app:
        build:
        context: .
        dockerfile: Dockerfile
        ports:
        - "5555:5051"
        depends_on:
        - mysql
        environment:
        SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/MyWeb
        SPRING_DATASOURCE_USERNAME: root
        SPRING_DATASOURCE_PASSWORD: xxxx

    volumes:
    mysql_data:
    ```
* nota: la porta 3306 del sistema deve essere libera cioè non deve essere in esecuzione nessun server mysql
* definizione del **Dockerfile**
    ```
    FROM eclipse-temurin:17-jdk-alpine
    WORKDIR /app
    COPY target/*.jar app.jar
    ENTRYPOINT ["java","-jar","app.jar"]
    ```
* per creare l'immagine
    ```
    docker build -t esempio01base:1.0-SNAPSHOT .
    ```
* per eseguire l'immagine esponendo il servizio su porta 5555
    ```
    docker-compose up --build
    curl http://localhost:5555/api/articoli/check
    curl http://localhost:5555/api/articoli/all
    ```
* comando docker utili
    ```
    docker-compose down
    docker-compose logs
    docker-compose exec mysql mysql -u your_username -p
    ```


# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*


