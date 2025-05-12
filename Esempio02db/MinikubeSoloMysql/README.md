# Esempio02db Minikube SOLO Mysql

In questo documento viene descritto come configurare e avviare un'istanza di MySQL 8 all'interno di un cluster Kubernetes locale creato con Minikube .
Prompt iniziale per IA ```Dammi un esempio per far partire un servizio su minikube con mysql```.

* Requisiti minimi
    - Minikube
    - kubectl
    - Docker (opzionale ma consigliato)


* Componenti
    - Deployment
    - Service
    - ConfigMap (per la configurazione)
    - PersistentVolumeClaim (per dati persistenti)

* Comando iniziale per avviare minukube
    ```
    minikube delete --all --purge
    minikube start --driver=docker --memory=2048 --cpus=2
    minikube status
    kubectl get nodes
    ```
* File creati
    - mysql-deployment.yaml
    - mysql-pvc.yaml
    - mysql-service.yaml
* Comandi per l'avvio 
    ```
    kubectl apply -f mysql-pvc.yaml
    kubectl apply -f mysql-deployment.yaml
    kubectl apply -f mysql-service.yaml
    ```
* Comandi di verifica:
    ```
    kubectl get pods,pvc,services
    minikube ip
    ```
* Comando per collegarsi al DB
    ``` 
    kubectl exec -it pod/mysql-8dfb596b-b659s -- mysql -u root -p
    ```
    Inserisci la password (password123) e sarai dentro il prompt di MySQL.
* Client da riga di comando
    ```
    kubectl run mysql-client --image=mysql:8 -it --rm --restart=Never -- mysql -h mysql -u root -p
    ```    
* Pulizia finale
    ```
    kubectl delete service mysql
    kubectl delete deployment mysql
    kubectl delete pvc mysql-pvc
    ```




# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*


