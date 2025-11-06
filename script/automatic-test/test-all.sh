# Script per chiamare tutti i test inclusi quelli per SQLite
#!/bin/bash
# set -e 

echo "Posizione script: $(dirname "$0")"
#cd "$(dirname "$0")/.."
echo "Directory di lavoro: $(pwd)"

# Script per eseguire il profilo `kube` eseguito in locale con docker compose
./script/automatic-test/test-kube-onprem-docker-compose.sh
# Script per eseguire il profilo `sqlite` eseguito in locale (con solo sqlite) senza docker
./script/automatic-test/test-sqlite-onprem.sh
# Script per eseguire il profilo `kube` eseguito in locale (con Postgresql e MongoDB) con docker compose
./script/automatic-test/test-aws-onprem.sh
# Script per eseguire il profilo `kube` eseguito in locale con **minikube** e **kubernetes**
./script/automatic-test/test-minikube.sh

echo "---------------------------------------------------------"
echo "✅ Tutti i test sono completati con successo!"