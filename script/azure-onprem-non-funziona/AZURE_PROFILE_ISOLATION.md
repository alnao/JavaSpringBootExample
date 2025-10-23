# 🔒 Isolamento Profili Azure - Architettura Esagonale

## 📋 Problema Identificato

In un progetto Spring Boot con **architettura esagonale** e **multi-profilo**, l'inclusione di dipendenze Azure nel classpath causava errori anche quando si utilizzavano profili diversi (onprem, aws, sqlite):

```
BeanInstantiationException: Failed to instantiate [com.azure.core.credential.TokenCredential]
Caused by: java.lang.NoClassDefFoundError: com/azure/core/implementation/util/HttpUtils
```

### Causa Root
- Il modulo `adapter-azure` era dichiarato come `<optional>true</optional>` nel `pom.xml` dell'application
- Ma Spring Boot eseguiva comunque l'**autoconfiguration** di Azure trovando le classi nel classpath
- Le annotazioni `@Profile("azure")` sulle configurazioni custom non erano sufficienti a bloccare l'autoconfiguration di Spring Cloud Azure

---

## ✅ Soluzione Implementata: Maven Profile Isolation

### 1. **Modifica del POM dell'Application**

**Prima** (❌ PROBLEMA):
```xml
<dependencies>
    <!-- ... altre dipendenze ... -->
    <dependency>
        <groupId>it.alnao.springbootexample</groupId>
        <artifactId>adapter-azure</artifactId>
        <optional>true</optional>  <!-- ❌ Viene sempre incluso nel classpath -->
    </dependency>
</dependencies>
```

**Dopo** (✅ SOLUZIONE):
```xml
<dependencies>
    <!-- ... altre dipendenze ... -->
    <!-- adapter-azure viene incluso SOLO nel profilo azure - vedi <profiles> sotto -->
</dependencies>

<profiles>
    <profile>
        <id>azure</id>
        <dependencies>
            <dependency>
                <groupId>it.alnao.springbootexample</groupId>
                <artifactId>adapter-azure</artifactId>
                <optional>false</optional>  <!-- ✅ Incluso SOLO quando -Pazure -->
            </dependency>
        </dependencies>
    </profile>
    
    <profile>
        <id>onprem</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <dependencies>
            <dependency>
                <groupId>it.alnao.springbootexample</groupId>
                <artifactId>adapter-onprem</artifactId>
                <optional>false</optional>
            </dependency>
        </dependencies>
    </profile>
    
    <!-- ... altri profili ... -->
</profiles>
```

### 2. **Configurazioni YAML di Sicurezza** (Opzionale ma Consigliato)

Anche se le classi Azure non sono nel classpath, manteniamo le esclusioni per sicurezza:

**`application-onprem.yml`**:
```yaml
spring:
  autoconfigure:
    exclude:
      - com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration
      - com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration
      - com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosAutoConfiguration
      - org.springframework.boot.autoconfigure.cosmos.CosmosAutoConfiguration
      - com.azure.spring.cloud.autoconfigure.AzureContextAutoConfiguration

azure:
  cosmos:
    enabled: false
```

**Applica le stesse esclusioni a**:
- `application-aws.yml`
- `application-sqlite.yml`

---

## 🚀 Come Usare i Profili

### 1. Compilazione con Maven

```bash
# Profilo ONPREM (default)
mvn clean package

# Profilo AZURE
mvn clean package -Pazure

# Profilo AWS
mvn clean package -Paws

# Profilo SQLite
mvn clean package -Psqlite
```

### 2. Esecuzione dell'Applicazione

```bash
# Con Maven (dev)
mvn spring-boot:run -Pazure -Dspring-boot.run.profiles=azure

# Con JAR compilato
java -jar application/target/application-*.jar --spring.profiles.active=azure

# Con Docker (assicurati di compilare con il profilo corretto)
mvn clean package -Pazure
docker build -t myapp:azure .
docker run -e SPRING_PROFILES_ACTIVE=azure myapp:azure
```

### 3. Docker Compose Multi-Profilo

Puoi avere diversi `docker-compose.yml`:

**`docker-compose-azure.yml`**:
```yaml
services:
  app:
    build:
      context: .
      args:
        MAVEN_PROFILE: azure  # Compila con -Pazure
    environment:
      - SPRING_PROFILES_ACTIVE=azure
    depends_on:
      - cosmosdb
      - sqlserver
```

**`docker-compose-onprem.yml`**:
```yaml
services:
  app:
    build:
      context: .
      args:
        MAVEN_PROFILE: onprem  # Compila con -Ponprem
    environment:
      - SPRING_PROFILES_ACTIVE=onprem
    depends_on:
      - postgres
      - mongodb
      - kafka
```

---

## 🏗️ Architettura Esagonale e Isolamento

### Principio di Design

```
┌─────────────────────────────────────────────────────────┐
│                    APPLICATION                          │
│  (Dipende solo da CORE + adapter attivo via profilo)   │
└─────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┬──────────────┐
        │                 │                 │              │
   ┌────▼────┐       ┌────▼────┐      ┌────▼────┐   ┌─────▼─────┐
   │  AZURE  │       │ ONPREM  │      │   AWS   │   │  SQLITE   │
   │ Adapter │       │ Adapter │      │ Adapter │   │  Adapter  │
   └─────────┘       └─────────┘      └─────────┘   └───────────┘
   (CosmosDB)        (Postgres +      (DynamoDB +   (SQLite)
   (SQL Server)       MongoDB +        MySQL)
   (Event Hubs)       Kafka)
```

### Vantaggi di Questa Soluzione

1. ✅ **Classpath Pulito**: Le dipendenze Azure NON sono nel classpath quando si usa onprem/aws/sqlite
2. ✅ **Zero Conflitti**: Nessun tentativo di autoconfiguration indesiderata
3. ✅ **Build Ottimizzate**: JAR più piccoli (non include dipendenze inutilizzate)
4. ✅ **Sicurezza**: Riduce la superficie di attacco (no librerie Azure in prod se non serve)
5. ✅ **Performance**: Startup più veloce (meno classi da scansionare)
6. ✅ **Manutenibilità**: Chiaro quali dipendenze servono per quale profilo

---

## 🧪 Verifica dell'Isolamento

### Test 1: Classpath Check
```bash
# Compila senza profilo Azure
mvn clean package -Ponprem

# Verifica che azure NON sia nel JAR
jar tf application/target/application-*.jar | grep azure
# Risultato atteso: NESSUN file azure presente
```

### Test 2: Startup Test
```bash
# Avvia con profilo onprem
java -jar application/target/application-*.jar --spring.profiles.active=onprem

# Verifica nei log che NON ci siano:
# - AzureTokenCredentialAutoConfiguration
# - AzureCosmosAutoConfiguration
# - Riferimenti a com.azure.*
```

### Test 3: Dependency Tree
```bash
# Controlla le dipendenze effettive per profilo
mvn dependency:tree -Ponprem > deps-onprem.txt
mvn dependency:tree -Pazure > deps-azure.txt

# Confronta i file - adapter-azure deve apparire SOLO in deps-azure.txt
```

---

## 📝 Note Importanti

### 1. **IDE Configuration**
Quando lavori con IntelliJ IDEA o Eclipse:
- Assicurati di **selezionare il profilo Maven corretto** nel pannello Maven
- IntelliJ: View → Tool Windows → Maven → Profiles
- Eclipse: Project → Properties → Maven → Active Maven Profiles

### 2. **Dockerfile Multi-Stage Build**
Se usi Docker, modifica il Dockerfile per accettare il profilo come build arg:

```dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS build
ARG MAVEN_PROFILE=onprem
WORKDIR /app
COPY . .
RUN mvn clean package -P${MAVEN_PROFILE} -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/application/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build:
```bash
docker build --build-arg MAVEN_PROFILE=azure -t myapp:azure .
docker build --build-arg MAVEN_PROFILE=onprem -t myapp:onprem .
```

### 3. **CI/CD Pipeline**
Esempio GitHub Actions:

```yaml
jobs:
  build-onprem:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build with onprem profile
        run: mvn clean package -Ponprem
      
  build-azure:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build with azure profile
        run: mvn clean package -Pazure
```

---

## 🔍 Troubleshooting

### Problema: "Class not found" quando uso profilo azure
**Causa**: Hai compilato con profilo sbagliato  
**Soluzione**: `mvn clean package -Pazure`

### Problema: Ancora errori Azure con profilo onprem
**Causa**: Classi Azure cached nel target/  
**Soluzione**: `mvn clean` poi ricompila con profilo corretto

### Problema: IDE non riconosce le classi Azure
**Causa**: IDE usa profilo Maven sbagliato  
**Soluzione**: Attiva il profilo "azure" nel pannello Maven dell'IDE

---

## 📚 Riferimenti

- [Maven Profiles Documentation](https://maven.apache.org/guides/introduction/introduction-to-profiles.html)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Spring Cloud Azure AutoConfiguration](https://microsoft.github.io/spring-cloud-azure/)

---

## ✅ Checklist Finale

- [x] Rimosso `adapter-azure` dalle dipendenze globali
- [x] Aggiunto `adapter-azure` nel profilo Maven `azure`
- [x] Mantenute esclusioni autoconfiguration negli altri profili (sicurezza)
- [x] Testato build con `-Ponprem` (nessun azure nel classpath)
- [x] Testato build con `-Pazure` (azure presente)
- [x] Verificato startup senza errori TokenCredential

---

**Risultato**: 🎉 Isolamento completo! Ogni profilo carica SOLO le dipendenze necessarie!
