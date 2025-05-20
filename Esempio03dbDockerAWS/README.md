# Esempio03dbDockerAWS
Progetto di esempio che crea i componenti
- un DB Mysql con una tabella "Persone"
- un backend con java spring boot, questo espone CRUD-API con protocollo Rest
- un frontend in javascript che consuma la API per visualizzare e modificare l'elenco delle persone
Il progetto è pensato per funzionare con **docker-compose**, **Kubernetes** con **Minikube** e AWS EKS.


## Comandi base
- Compilazione del `backend-springboot` con java (see https://spring.io/guides/gs/accessing-data-mysql).
    ```
    cd backend-springboot
    mvn clean
    mvn install
    mvn package
    ```
    - Per questo progetto è indispensabile avere la versione 17 di Java, con precedenti versioni non funziona.
- creazione in locale con `docker-compose`:
    ```
    docker-compose up --build
    ```
- verifica e pulizia
    ```
    docker ps
    docker exec -it d4e51218bb07 bash
        mysql -h"mysql-server" -p"3307" -u"root" -p"alnaoBellissimo"
        curl localhost:8080/api/persone
        curl localhost:8080/api/persone/info
    docker-compose down --volumes
    docker image prune
    ```
- push su DockerHub necessaria per Kubernetes e EKS:
    ```
    docker login
    # Backend
    docker build -t alnao/j-esempio02-backend-springboot -f ./backend-springboot/Dockerfile-backend ./backend-springboot/
    docker push alnao/j-esempio02-backend-springboot

    # Frontend
    docker build -t alnao/j-esempio02-frontend-bootstrap -f ./frontend-bootstrap/Dockerfile-frontend ./frontend-bootstrap/
    docker push alnao/j-esempio02-frontend-bootstrap
    ```
- avvio minikube con storage class (*perso un sacco di tempo per questo tema*)
    ```
    minikube addons list | grep storage
    minikube addons enable default-storageclass

    minikube start --driver=docker --memory=2048 --cpus=2
    minikube addons list | grep storage
    ```
- creazione Mysql su kubernetes in locale con Minikube:
    ```
    kubectl apply -f ./kubernetes/mysql-pvc.yaml
    kubectl apply -f ./kubernetes/mysql.yaml

    kubectl describe pvc mysql-pvc
    kubectl describe pvc mysql-pvc
    kubectl describe deployment  mysql-app
    kubectl describe service mysql-service
    ```
- creazione backend    
    ```
    kubectl apply -f ./kubernetes/springboot-app.yaml
    kubectl get services
    ```

- comandi di verifica
    ```    
    kubectl get events -A
    
    kubectl get services
    kubectl get pods
    kubectl get pods --field-selector=status.phase=Pending
    kubectl describe node mysql-xxxxxxxxxxxxxxx

    mysql -h"mysql-service" -p"3306" -u"root" -p"alnaoMagnifico"

    minikube service springboot-app --url
    > http://192.168.49.2:31081
    ```
    - nota: endpoint è mysql-service perchè il metadata del service
    - nota2: porta è 3306 NON so il perchè ma è così, *perso un sacco di tempo per questo motivo*
- creazione frontend
    ```
    kubectl apply -f ./kubernetes/frontend.yaml
    kubectl get services
    kubectl get pods

    minikube service frontend-bootstrap --url
    > http://192.168.49.2:31082

    ```
    - nota: nella configuazione messo l'endpoint con localhost perchè il frontend è in javascript quindi client e viene eseguito sul browser, l'immagine docker del webserver non si collega direttamente al backend
    - nota: c'è un proxy per evitare che il browser chiami direttamente il backend (conoscendo l'url), cioè il browser chiama `<frontend>/api/persone` che poi viene indirizzato a `<backend>/api/persone`
- Comandi vari
    ```
    kubectl get services
    kubectl get deployments
    ```
- Verifica connesione dal frontend al backend
    ```
    apk add curl
    curl http://springboot-app:8080/api/users
    ```
- Verifica log del proxy backend
    ```
    kubectl logs frontend-bootstrap-6c95494cf8-f4dlx -c nginx-proxy
    ```
- Cancellazione di tutte le componenti su minikube
    ```
    kubectl delete configmap frontend-config
    kubectl delete service frontend-bootstrap
    kubectl delete deployment frontend-bootstrap

    kubectl delete service springboot-app
    kubectl delete deployment springboot-app

    kubectl delete service mysql-service
    kubectl delete deployment mysql-app

    kubectl delete pvc mysql-pvc
    kubectl delete pv mysql-pv
    ```
- creazione cluster su EKS
    - TODO
- docker action CI/CD
    - TODO


- secret
    - TODO
    ```
    kubectl create secret generic mysql-root-pass --from-literal=password=rootpassword123

    apiVersion: v1
    kind: Secret
    metadata:
    name: mysql-root-pass
    type: Opaque
    data:
    password: cm9vdHBhc3N3b3JkMTIz  # base64 encoded "rootpassword123"
    kubectl apply -f mysql-secret.yaml
    ```

# IA



Ciao vorrei creare un microservizio in java spring boot che esegue un crud su una tabella mysql, schema "informazioni" con tabella "Persone" che contiene Nome e Cognome e eta, venga versionato su un repository git con un suo dockerfile e docker-compose con docker hub, nel template ci deve essere un sever mysql, vorrei anche un piccolo frontend per gestire la tabella con libreria grafica bootstrap, vorrei una API rest anche per creare schema e la tabella se non esistono, una seconda API rest per svuotare la tabella completamente, queste due API non devono essere chiamate da frontend, vorrei poterlo eseguire e provare in locale sul mio pc eseguendo mysql in docker, vorrei creare una pipeline che esegue il rilascio in aws con servizi meno costosi possibile, se ti serve use kubernetes, minikube, heml, jenkins, git. 
    - allora modifica il docker-compose in modo che le porte esposte siano la 3307 per mysql, 5083 per il microservizio e 5084 per il frontend
    - ora modifica il backend in modo che la porta sia un parametro che arriva dal docker-compose
    - nel progetto frontend, voglio che la porta della API sia dinamica e usa "envsubst"
    - nel microservizio vorrei che aspetti 2 minuti prima di far partire il microservizio
    - nel docker-compose voglio aggiungere una immagine che esegua un file sql prima del lancio del microservizio
    - voglio togliere il "db-init" che non funziona ed eseguire il sql con un comando nel docker file del backend
    - come faccio a passare i parametri "environment" nel dockerfile verso il microservio nel file application.properties usando il comando sh che hai creato e come verifico che i parametri sono correttamente caricati?
- frontend
    - adesso vorrei fare questo: nel frontend il file config.js con dentro la API_URL che viene passata come parametro dal docker-compose e deve essere usato dal html, usa il comando "envsubst" nel file start.sh del frontend 
    - l'istruzione fetch va in errore, possiamo usare altro ? tipo axios
    - ho un errore CORS ERROR, come risolvo?
    - dammi tutta la pagina index perchè che voglio tornare alla versione con fetch
- FUNZIONA
- versione frontend-java poi tolto perchè non funziona
    - adesso vorrei aggiungere un frontend in java per gestire la tabella sempre con lista, form per modifica e inserimento, aggiungendolo nel docker-compose esponendolo sulla porta   5085 nel host, usa la libreria grafica bootstrap come grafica
    - facciamo due modifiche: usiamo il pom.xml e vorrei aggiungere nel environment l'endpoint del backend
    - avviando l'applicazione mi dice "Caused by: java.lang.NoClassDefFoundError: javafx/application/Application"
    - al posto della javafx vorrei una libreria web , decidi tu quale ma la parte grafica deve rimanere su bootstrap
    - non voglio usare javascript ma voglio usare solo jsp
    - controlla l'applicazione perchè non ci sono la jsp nel jar inoltre non mi hai dato il web.xml
    - Causato da: java.lang.NoClassDefFoundError: jakarta/servlet/Servlet
    - usando l'opzione 2 che mi piace molto ho questo errore "Exception in thread "main" java.lang.SecurityException: Invalid signature file digest for Manifest main attributes"
- minikube
    - tante iterazioni per capire che bisogna fare un pcv separato e ben configurato
- Proxy: allora questa situazione: tutto su minikube un backend in java script boot e un frontend in javascript esposto con nginx ma il problema è che i browser che eseguono i javascript devono avere l'endpoint del backend, quello che vorrei è che il frontend esegua un "proxy" in modo che che il javascript punti come API allo stesso url del backend che poi venga eseguito un redirect/proxy verso il backend, si può fare?
    - `kubectl create configmap nginx-proxy-config --from-file=default.conf=./frontend-bootstrap/nginx.conf`
    - ho perso un sacco di tempo per la regola *ConfigMap* ma alla fine funziona, vedi il `default.conf`
- AWS
    - TODO
    - dimentica la versione frontend java in jsp perchè non funziona e non mi interessa più, ora vorrei eseguire il tutto su AWS usando EKS , intanto con Mysql su un'immagine docker come è già, come procediamo?



# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*



