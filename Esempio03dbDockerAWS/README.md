# Esempio03dbDockerAWS
Progetto di esempio che crea i componenti
- un DB Mysql con una tabella "Persone"
- un backend con java spring boot, questo espone CRUD-API con protocollo Rest
- un frontend in javascript che consuma la API per visualizzare e modificare l'elenco delle persone


Il progetto è pensato per funzionare con **docker-compose**, **Kubernetes** con **Minikube** e **AWS EKS**:
- il backend e il frontend sono disponibili su DockerHub a `https://hub.docker.com/repositories/alnao`
- esecuzione con Minikube per eseguire tutto in locale con anche MySql dentro un immagine docker
- esecuzione su cluster **AWS-EKS** con creato tramite AWS-CLI
- esecuzione con CloudFormation su `https://github.com/alnao/AwsCloudFormationExamples/tree/master/Esempio27eks` (con un docker-compose dedicato)
- esecuzione su cluster **AWS-EKS** con Heml-Chart e ArgoCD


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
    - frontend `http://localhost:5084/`
    - backend `http://localhost:5080/api/persone`
    - backend `http://localhost:5080/api/persone/info`
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
- Esecuzione con Minikube in sistema locale
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
        > http://192.168.49.2:31083/api/persone

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
- Creazione cluster su EKS `aws-j-es03`
    - Creazione del cluster (vedi [documentazione ufficiale](https://docs.aws.amazon.com/eks/latest/userguide/create-cluster.html))
        - Impostazione account
            ```
            AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
            echo $AWS_ACCOUNT_ID

            DEFAULT_VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query "Vpcs[0].VpcId" --output text)
            echo $DEFAULT_VPC_ID

            SUBNET_IDS=($(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$DEFAULT_VPC_ID" --query "Subnets[*].SubnetId" --output text))
            export SUBNET_1=${SUBNET_IDS[0]}
            export SUBNET_2=${SUBNET_IDS[1]:-""}
            export SUBNET_3=${SUBNET_IDS[2]:-""}
            echo $SUBNET_1
            ```

        - Creazione regola IAM e security group (see [documentazione](https://docs.aws.amazon.com/it_it/eks/latest/userguide/getting-started-console.html))
            ```
            aws iam create-role --role-name aws-eks-j-es03-iam-role --assume-role-policy-document file://"./aws/eks-cluster-role-trust-policy.json"
            
            aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AmazonEKSClusterPolicy --role-name aws-eks-j-es03-iam-role

            aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy --role-name aws-eks-j-es03-iam-role
            aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly --role-name aws-eks-j-es03-iam-role
            aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy --role-name aws-eks-j-es03-iam-role

            aws iam update-assume-role-policy --role-name aws-eks-j-es03-iam-role --policy-document file://"./aws/eks-cluster-role-trust-policy.json"

            GROUP_JSON=$(aws ec2 create-security-group --group-name aws-eks-j-es03-sg --description "SG of aws-eks-j-es03")
            GROUP_ID=$(echo $GROUP_JSON | jq -r '.GroupId')
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 31082 --cidr 0.0.0.0/0
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 31083 --cidr 0.0.0.0/0
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 3306 --cidr 0.0.0.0/0
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 3307 --cidr 0.0.0.0/0

            # Permetti traffico interno tra nodi (tra nodi dello stesso Security Group):
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol all --source-group $GROUP_ID
            # Permetti traffico SSH (opzionale, se ti serve accedere ai nodi):
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 22 --cidr 0.0.0.0/0
            # Permetti traffico DNS (TCP/UDP 53):
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 53 --cidr 0.0.0.0/0
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol udp --port 53 --cidr 0.0.0.0/0
            # Permetti traffico HTTPS (porta 443):
            aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 443 --cidr 0.0.0.0/0
            # 3. Regole di EGRESS (di default sono bloccate, le abilitiamo tutte)
            aws ec2 authorize-security-group-egress --group-id $GROUP_ID --protocol -1 --port all --cidr 0.0.0.0/0

            ```
            - nota: nella creazione del security group non serve specificare la VPC `--vpc-id vpc-XXX` perchè usiamo la default
        - Creazione cluster EKS e NodeGroup
            - **Attenzione ai costi**
            - Costo del Control Planel (Master Nodes): $0.10/ora (circa $73 al mese ) per ogni cluster EKS 
            - I nodi worker non sono inclusi in EKS: li paghi separatamente in base al tipo di risorsa che usi quindi paghi le istanze EC2 esattamente come se fossero normali EC2 fuori da EKS
                - t3.micro in eu-central-1: ~$0.0104/ora → ~$7.50/mese
                - AWS Fargate (serverless): Primi 60 minuti gratuiti per pod al giorno, $0.04032/ora per CPU + RAM (fino a 0.25 vCPU / 0.5 GB, poi scalato)
            - Networking (VPC, ELB, ecc.): ALB ($0.0225/ora + $0.008/ora per ogni Listener), NLB ($0.022/ora + traffico elaborato) , VPC Endpoint, Traffico dati tra Availability Zones ($0.01/GB se presente)
            - Storage: gp2 General Purpose SSD ($0.10/GB/mese), io1 (Provisioned IOPS SSD), sc1 (Cold HDD)
            - LOG: CloudWatch Logs : $0.50/GB al mese + $0.10 per milione di richieste
            - 📈 Esempio di costo totale mensile
                - EKS Cluster: 1 cluster $73
                - Nodi Worker 2x t3.micro: $15
                - ALB attivo : $16
                - Storage 2x 10GB gp2: $2
                - Totali $106/mese
            ```
	        aws eks create-cluster --region eu-central-1 --name aws-j-es03-eks-cluster --kubernetes-version 1.32 --role-arn arn:aws:iam::$AWS_ACCOUNT_ID:role/aws-eks-j-es03-iam-role --resources-vpc-config subnetIds=$SUBNET_1,$SUBNET_2,$SUBNET_3,securityGroupIds=$GROUP_ID --kubernetes-network-config '{"serviceIpv4Cidr":"172.20.0.0/24"}'

            
            aws eks describe-cluster --name aws-j-es03-eks-cluster --region eu-central-1 --query "cluster.kubernetesNetworkConfig"
            
            aws eks create-nodegroup --cluster-name aws-j-es03-eks-cluster --region eu-central-1 --nodegroup-name aws-j-es03-eks-node-group --subnets $SUBNET_1 $SUBNET_2 $SUBNET_3 --node-role arn:aws:iam::$AWS_ACCOUNT_ID:role/aws-eks-j-es03-iam-role --instance-types t2.small --scaling-config minSize=1,maxSize=2,desiredSize=1 --tags "Project=aws-j-es03"
            ```
        - Configurazione *update-kubeconfig*
            ```
            aws eks update-kubeconfig --region eu-central-1 --name aws-j-es03-eks-cluster
            ```
            - Questo comando aggiunge una voce per il tuo cluster nel file `~/.kube/config`.
                ```
                Added new context arn:aws:eks:eu-central-1:565949435749:cluster/aws-j-es03-eks-cluster to /home/alnao/.kube/config
                ```
        - Errore "cni config uninitialized"
            - errore `Container runtime network not ready: cni config uninitialized`
            - nel nodo , see https://stackoverflow.com/questions/49112336/container-runtime-network-not-ready-cni-config-uninitialized
            - comando da lanciare quando il nodo è creato
                ```
                kubectl apply -f https://github.com/weaveworks/weave/releases/download/v2.8.1/weave-daemonset-k8s.yaml
                ```
        - Attenzione alle dimesioni EC2: see https://stackoverflow.com/questions/49112336/container-runtime-network-not-ready-cni-config-uninitialized
            - So if we see official Amazon doc, t3.micro maximum 2 interface you can use and 2 private IP. Roughly you might be getting around 4 IPs to use and 1st IP get used by Node etc, There will be also default system PODs running as Daemon set and so. Add new instance or upgrade to larger instance who can handle more pods.
            - The formula for defining the maximum number of Pods per EC2 Node instance is as follows: `N * (M-1) + 2` Where:
                - N is the number of Elastic Network Interfaces (ENI) of the instance type
                - M is the number of IP addresses per ENI
                - So for the instance you used which is t3.micro the number of pods that can be deployed are:
                - 2 * (2-1) + 2 = 4 Pods, the 4 pods capacity is already used by pods in kube-system
                - see https://github.com/aws/amazon-vpc-cni-k8s/blob/master/misc/eni-max-pods.txt
                    - t2.micro 4
                    - t2.nano 4
                    - t2.small 11
                    - t2.medium 17
                    - t2.xlarge 44
        - Installazione del eksctl e configurazione del disco (per risolvere errore Waiting for a volume to be created either by the external provisioner 'ebs.csi.aws.com' or manually by the system administrator. If volume creation is delayed, please verify that the provisioner is running and correctly registered.)
    - Soluzione dell'errore `api error AccessDenied: Not authorized to perform sts:AssumeRoleWithWebIdentity`
        ```
        aws eks create-addon --cluster-name aws-j-es03-eks-cluster --addon-name aws-ebs-csi-driver --region eu-central-1 
        CLUSTER_NAME=aws-j-es03-eks-cluster
        REGION=eu-central-1 
        OIDC_ID=$(aws eks describe-cluster --name $CLUSTER_NAME --region $REGION --query "cluster.identity.oidc.issuer" --output text | cut -d'/' -f5)
        aws iam list-open-id-connect-providers | grep $OIDC_ID

        aws iam create-open-id-connect-provider --url $(aws eks describe-cluster --name $CLUSTER_NAME --region $REGION --query "cluster.identity.oidc.issuer" --output text)  --thumbprint-list 9e99a48a9960b14926bb7f3b02e22da2b0ab7280 --client-id-list sts.amazonaws.com
        ```
        e secondo blocco

        ```
        CLUSTER_NAME=aws-j-es03-eks-cluster
        REGION=eu-central-1 
        # Rimuovi prima eventuali installazioni esistenti del driver EBS CSI
        kubectl delete deployment ebs-csi-controller -n kube-system 2>/dev/null || true
        kubectl delete daemonset ebs-csi-node -n kube-system 2>/dev/null || true

        # Se esiste già l'addon, rimuovilo prima di reinstallarlo
        aws eks delete-addon --cluster-name $CLUSTER_NAME --addon-name aws-ebs-csi-driver --region $REGION || true

        # Attendi qualche secondo per assicurarsi che la rimozione sia completata
        sleep 30

        # Crea un ruolo IAM specifico per l'addon EBS CSI
        # Ottieni l'URL dell'OIDC provider
        OIDC_PROVIDER=$(aws eks describe-cluster --name $CLUSTER_NAME --region $REGION --query "cluster.identity.oidc.issuer" --output text | sed -e "s/^https:\/\///")

        # Crea un nuovo ruolo IAM con un nome diverso per evitare confusione
        aws iam create-role --role-name EBSCSIDriverAddonRole --assume-role-policy-document file://"./aws/ebs-csi-addon-trust-policy.json"

        # Allega la policy gestita da AWS per l'EBS CSI driver
        aws iam attach-role-policy --role-name EBSCSIDriverAddonRole --policy-arn arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy

        # Installa l'addon con il nuovo ruolo IAM
        aws eks create-addon --cluster-name $CLUSTER_NAME --addon-name aws-ebs-csi-driver --service-account-role-arn arn:aws:iam::$ACCOUNT_ID:role/EBSCSIDriverAddonRole --region $REGION

        # Attendi che l'addon sia pronto
        sleep 30
        aws eks describe-addon --cluster-name $CLUSTER_NAME --addon-name aws-ebs-csi-driver --region $REGION
        ``` 
    - Creazione delle immagini
        ```
        kubectl apply -f ./kubernetes/mysql-pvc-aws.yaml
        kubectl apply -f ./kubernetes/mysql.yaml
        kubectl apply -f ./kubernetes/springboot-app.yaml
        kubectl apply -f ./kubernetes/frontend.yaml
        kubectl get services
        kubectl get pods
        ```
    - Per funzionare bisogna aggiungere il security group `aws-eks-j-es03-sg` all'istanza EC2, poi endpoint disponibile pubblicamente è: 
        ```
        http://<IP_PUBBLICO_EC2>:31082
        ```
    - Cancellazione di tutto
        ```
        kubectl delete configmap frontend-config
        kubectl delete service frontend-bootstrap
        kubectl delete deployment frontend-bootstrap

        kubectl delete service springboot-app
        kubectl delete deployment springboot-app

        kubectl delete service mysql-service
        kubectl delete deployment mysql-app
        kubectl delete pvc mysql-pvc
        kubectl delete sc ebs-sc
        
        aws eks delete-nodegroup --cluster-name aws-j-es03-eks-cluster --nodegroup-name aws-j-es03-eks-node-group --region eu-central-1
        
        # Aspettare che finisca la cancellazione, verificare su console-web
        aws eks delete-cluster --region eu-central-1 --name aws-j-es03-eks-cluster

        aws eks delete-addon --cluster-name $CLUSTER_NAME --addon-name aws-ebs-csi-driver --region $REGION
        aws eks describe-addon --cluster-name $CLUSTER_NAME --addon-name aws-ebs-csi-driver --region $REGION
        aws iam detach-role-policy --role-name EBSCSIDriverAddonRole --policy-arn arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy
        aws iam delete-role --role-name EBSCSIDriverAddonRole

        aws iam list-attached-role-policies --role-name aws-eks-j-es03-iam-role
        aws iam detach-role-policy --role-name aws-eks-j-es03-iam-role --policy-arn arn:aws:iam::aws:policy/AmazonEKSClusterPolicy
        aws iam detach-role-policy --role-name aws-eks-j-es03-iam-role --policy-arn arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy
        aws iam detach-role-policy --role-name aws-eks-j-es03-iam-role --policy-arn arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly
        aws iam detach-role-policy --role-name aws-eks-j-es03-iam-role --policy-arn arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy
        aws iam delete-role --role-name aws-eks-j-es03-iam-role
        

        aws iam delete-role --role-name AWSServiceRoleForAmazonEKS
        aws iam delete-role --role-name AWSServiceRoleForAmazonEKSNodegroup
        aws iam delete-role --role-name AWSServiceRoleForAutoScaling
        aws iam delete-role --role-name AWSServiceRoleForElasticLoadBalancing

        aws iam delete-policy --policy-arn arn:aws:iam::$AWS_ACCOUNT_ID:policy/aws-eks-j-es03-iam-policy

        aws ec2 delete-security-group --group-id aws-eks-j-es03-sg
        ```

- **Action CI/CD** con Argo e AWS EKS
    - Comandi per la creazione di un repository ECR:
        ```
        AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
        echo $AWS_ACCOUNT_ID
        REGION=eu-central-1 
        aws ecr create-repository --repository-name aws-eks-j-es03-repo --region $REGION
        ```
    - Comandi per la login e push su EKS
        ```
        cd backend-springboot
        # 1. Recupera il token di autenticazione e autentica il tuo client Docker nel registro ECR
        aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com
        # 2. Compila l'immagine Docker (assicurati di essere nella directory con il Dockerfile)
        docker build -f Dockerfile-backend -t aws-eks-j-es03-repo . 
        # 3. Tagga l'immagine per il tuo repository ECR (con vesione 1.0.0)
        docker tag aws-eks-j-es03-repo:latest $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/aws-eks-j-es03-repo:1.0.0
        # 4. Effettua il push dell'immagine in ECR
        docker push $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/aws-eks-j-es03-repo:1.0.0
        echo $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/aws-eks-j-es03-repo:1.0.0
        ```
        (questo ultimo valore stampato servirà nei passi successivi)
    - Creazione del HEML
        ```
        mkdir -p helm-charts/spring-boot-app
        cd helm-charts/spring-boot-app
        helm create . # Inizializza il chart nella directory corrente
        ```        
        - modifica del file `helm-charts/spring-boot-app/values.yaml`
            ```

            replicaCount: 1

            image:
                repository: 565949435749.dkr.ecr.eu-central-1.amazonaws.com/aws-eks-j-es03-repo
                pullPolicy: IfNotPresent
                tag: "1.0.0" # Assicurati che corrisponda alla tag dell'immagine che hai pushato

            # ... (altri valori generati da helm create) ...

            service:
                type: LoadBalancer # Cambia in LoadBalancer per esposizione pubblica diretta su AWS
                port: 80
                targetPort: 8080 # La porta del tuo Spring Boot
            # Nota: Se vuoi un Ingress, mantieni ClusterIP e configura l'Ingress in templates/
            # Per un Ingress su EKS, dovrai installare l'AWS Load Balancer Controller.

            ingress:
            enabled: false # Abilitalo solo se installi l'AWS Load Balancer Controller
            # ... (configurazione Ingress se abilitato) ...

            # ... (altri valori) ...   
            ```
        - **Importante**: usare LoadBalancer per il servizio, AWS creerà un Classic Load Balancer o Network Load Balancer che esporrà il tuo servizio. Questo ha costi associati. Per un controllo più granulare del routing e per terminare SSL, un Ingress con l'AWS Load Balancer Controller è la soluzione preferita in produzione.
        - push della modifica appena fatta
            ```
            cd ../.. # Torna alla root del tuo JavaSpringBootExample/backend-springboot
            git add helm-charts/spring-boot-app
            git commit -m "Add Helm chart for Spring Boot app"
            git push origin master # O 'main' se hai cambiato il nome del branch
            ```
    - Configurazione di **AWS-EKS** con `eksctl` (cche deve essere già presente)
        - Creazione del file `eks-cluster.yaml` (io l'ho messo dentro helm-charts per orgine)
            - **Importante**: questi passi creano una VPC dedicata e istanze EC2 con dei bei belli alti, prestare sempre attenzione!
            - **Importante**: in questo file devono essere indicati VPC e Subnet specifici (altrimenti vengono creati nuovi con tanti bei costi!)
        - Creazione del cluster 
            ```
            eksctl create cluster -f ./helm-charts/eks-cluster.yaml
            kubectl get nodes
            ```
            - **Importante**: questo passi ci mette anche 15 minuti se non sono indicate le VPC e le subnet!
            - in automatico è possibile usare FreeLens perchè kubectl aggiorna il file di configurazione, altrimenti lanciare il comando
                ```
                aws eks update-kubeconfig --region eu-central-1 --name aws-j-es03-eks-helm-cluster
                ```
    - Installazione Argo-CD su EKS
        ```
        kubectl create namespace argocd
        kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml --validate=false
        PASSWORD_ARGOCD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
        
        kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer"}}'
        kubectl get svc argocd-server -n argocd
        ```
        - Accedi all'URL ritornata dall'ultimo comando https://EXTERNAL-IP (ignora l'avviso di sicurezza SSL se non hai configurato un certificato). Effettua il login con `admin` e la password recuperata dal comando `echo $PASSWORD_ARGOCD`. Cambiala subito!
        - Noda: a me non va l'url pubblico ma solo localhost:xxxxx ritornato da FreeLens!
    - Configurazione di Argo-CD
        - Creazione del file `spring-boot-app-argocd-app.yaml` e successivo push nel repository
        - Comando di deploy
            ```
            kubectl apply -f helm-charts/spring-boot-app-argocd-app.yaml -n argocd
            ```
    - Sui file creati da HEML nel `Chart.yaml` e nelle sottocartella avevano il punto `.` al posto del nome dell'applicazione, ho dovuto sistemare i file a mano (sia Chart.yaml che tutti i file dentro template che avevano dei `..`) *ho perso ore per questo problema*.
    - Funziona anche se il microservizio non parte perchè non riesce a collegarsi al DB, *semplicemente perchè non esiste nessun DB in questo esempio*.
    - Pulizia finale di tutto
        ```
        kubectl delete -f helm-charts/spring-boot-app-argocd-app.yaml -n argocd
        eksctl delete cluster --region=eu-central-1 --name=aws-j-es03-eks-helm-cluster
            # ci mette un bel po' anche 15 minuti!

            kubectl delete -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml -n argocd
            kubectl delete namespace argocd
        
            aws ecr describe-repositories --query "repositories[*].repositoryName" --output text
            aws ecr delete-repository --repository-name aws-eks-j-es03-repo --region $REGION --force
        ```
        - verificare che è stato tutto rimosso: ECR, EKS, VPC, Subnet, EC2, ALB, ASG e il cluster EKS "aws-j-es03-eks-cluster-helm"


## Comandi di creazione Claude.ia
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
    - dimentica la versione frontend java in jsp perchè non funziona e non mi interessa più, ora vorrei eseguire il tutto su AWS usando EKS , intanto con Mysql su un'immagine docker come è già, come procediamo?
    - lavorato tantissimo per i vari problemi di configurazione
- CD/CI con gemini
    - ciao mi spieghi cosa è argo e come lo posso usare con Kubernetes?
    - vorrei provare argo e helm in un mio progetto kubernetes dove ho un microservizio in java spring boot, dammi l'elenco di tutti i passi che devo fare
    - immagina che voglio eseguire tutto questo su AWS, il mio repository è "https://github.com/alnao/JavaSpringBootExample/tree/master/Esempio03dbDockerAWS"
    - lavorato molto su alcuni errori di Helm e AWS ma poi andato tutto




# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*



