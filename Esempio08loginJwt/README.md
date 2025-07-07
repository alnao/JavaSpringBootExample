- ciao, il mio obbiettivo è creare con Java spring boot un microservizio che faccia basic-login, utenti in una tabella Postgres (users con nome e cognome e mail e password e flagAttivo), un service di CROD e un service di login, poi vorrei anche un frontend in react con grafica bootstrap per testare il tutto. facciamo anche una tabella "contenuti" e un CRUD che permetta di gestirli, le API devono essere permesse solo se l'utente è loggato. Intanto dammi la struttura del progetto e il pom
- creami tutti i mkdir e creat file per questa struttura del progetto 
- ciao, ho un microservizio con java spring boot, che usa postgres, creami il docker-compose
- così facendo il mydatabase viene creato da solo? avrei bisogno di due tabelle "contenuti" con i campi (id,titolo,descrizione,contenuto,dataCreazione,dataModifica,pubblicato,autore) e "users" con i campi (id,nome,cognome,password,email,flagAttivo,dataCreazione,dataUltimoAccesso)

Ciao! Ti aiuto a creare la struttura del progetto per il tuo microservizio con autenticazione e gestione contenuti.
Struttura del Progetto
my-microservice/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── microservice/
│   │   │               ├── MicroserviceApplication.java
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── JwtConfig.java
│   │   │               │   └── CorsConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── UserController.java
│   │   │               │   └── ContentController.java
│   │   │               ├── dto/
│   │   │               │   ├── LoginRequest.java
│   │   │               │   ├── LoginResponse.java
│   │   │               │   ├── UserDto.java
│   │   │               │   └── ContentDto.java
│   │   │               ├── entity/
│   │   │               │   ├── User.java
│   │   │               │   └── Content.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   └── ContentRepository.java
│   │   │               ├── service/
│   │   │               │   ├── AuthService.java
│   │   │               │   ├── UserService.java
│   │   │               │   ├── ContentService.java
│   │   │               │   └── JwtService.java
│   │   │               ├── security/
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── CustomUserDetailsService.java
│   │   │               └── exception/
│   │   │                   ├── GlobalExceptionHandler.java
│   │   │                   └── CustomException.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── static/
│   │           └── (file del frontend React)
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── microservice/
│                       └── MicroserviceApplicationTests.java
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Login.js
│   │   │   ├── Dashboard.js
│   │   │   ├── UserList.js
│   │   │   ├── UserForm.js
│   │   │   ├── ContentList.js
│   │   │   ├── ContentForm.js
│   │   │   └── PrivateRoute.js
│   │   ├── services/
│   │   │   ├── authService.js
│   │   │   ├── userService.js
│   │   │   └── contentService.js
│   │   ├── contexts/
│   │   │   └── AuthContext.js
│   │   ├── App.js
│   │   └── index.js
│   ├── package.json
│   └── package-lock.json
├── pom.xml
└── README.md

curl -X POST http://localhost:8047/api/auth/register -H "Content-Type: application/json" -d '{"email": "alnao@alnao.xxx","password": "bellissimo", "nome":"Alberto","cognome":"Nao" }'
curl -X POST http://localhost:8047/api/auth/login -H "Content-Type: application/json" -d '{"email": "alnao@alnao.xxx","password": "bellissimo"}'
response=$(curl -X POST http://localhost:8047/api/auth/login -H "Content-Type: application/json" -d '{"email": "alnao@alnao.xxx","password": "bellissimo"}')
TOKEN=$(echo "$response" | jq -r '.token')
echo $TOKEN

curl -X GET http://localhost:8047/api/contents -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8047/api/contents  -H "Content-Type: application/json" -d '{"titolo": "Titolo bellissimo 2","descrizione": "questa descrizione proprio bellissima"}' -H "Authorization: Bearer $TOKEN"
curl -X GET http://localhost:8047/api/contents -H "Authorization: Bearer $TOKEN"

curl -X GET http://localhost:8047/api/contents -H "Authorization: Bearer $SENZA"
curl -X GET http://localhost:8047/api/contents


