# Adapter JavaFX - Interfaccia Desktop

Modulo per l'interfaccia desktop JavaFX del sistema di gestione annotazioni.
- ⚠️ L'esecuzione di questo frontend funziona solo con sqlite, non è stato testato su altri profili! ⚠️


## 🎯 Descrizione

Questo adapter fornisce un'interfaccia grafica desktop per interagire con il sistema di gestione annotazioni, sfruttando l'architettura esagonale del progetto e riutilizzando completamente la logica di business del modulo `core`.

## 📋 Caratteristiche

- **Login e Autenticazione**: Schermata di login con supporto utenti
- **Gestione Annotazioni CRUD**: 
  - Creazione nuove annotazioni
  - Modifica annotazioni esistenti
  - Eliminazione con conferma
  - Visualizzazione dettagliata
- **Ricerca e Filtri**:
  - Ricerca full-text
  - Filtro per stato (Bozza, In Revisione, Approvata, Rifiutata, Archiviata)
  - Filtro per categoria
- **Gestione Metadata**:
  - Categoria personalizzabile
  - Priorità (0-10)
  - Tags
  - Visibilità pubblica/privata
- **Interfaccia Responsive**: Layout con SplitPane per ottimizzare lo spazio

## 🚀 Tecnologie

- **JavaFX 21**: Framework UI desktop
- **Spring Boot 3.3**: Dependency Injection e configurazione
- **FXML**: Dichiarazione interfacce grafiche
- **Maven**: Build e gestione dipendenze

## 📦 Struttura

```
adapter-javafx/
├── pom.xml
└── src/main/
    ├── java/it/alnao/springbootexample/javafx/
    │   ├── JavaFxApplication.java          # Entry point
    │   ├── config/
    │   │   └── JavaFxConfig.java           # Configurazione Spring
    │   ├── controller/
    │   │   ├── LoginController.java        # Controller login
    │   │   └── MainController.java         # Controller principale
    │   ├── model/
    │   │   └── AnnotazioneViewModel.java   # View model con properties
    │   └── service/
    │       └── ViewModelConverterService.java # Conversione domain/view
    └── resources/
        └── fxml/
            ├── login.fxml                  # UI Login
            └── main.fxml                   # UI Principale
```

## 🔧 Configurazione

### Prerequisiti

- **Java 17+** (configurato per Java 21)
- **Maven 3.8+**
- **JavaFX SDK** (scaricato automaticamente da Maven)

### Build

```bash
cd /path/to/JavaSpringBootExample
mvn clean install -pl adapter-javafx -am
```

### Configurazione Database

L'applicazione richiede un adapter di persistenza configurato. Esempio con SQLite:

**application.properties**:
```properties
spring.application.name=Gestione Annotazioni Desktop

# Profilo attivo (sqlite, postgresql, mongodb, etc.)
spring.profiles.active=sqlite

# SQLite Configuration
spring.datasource.url=jdbc:sqlite:data/annotazioni.db
spring.datasource.driver-class-name=org.sqlite.JDBC
```

## ▶️ Esecuzione

### Con Maven

```bash
mvn javafx:run -pl adapter-javafx
```

### Con Spring Boot Plugin

```bash
mvn spring-boot:run -pl adapter-javafx
```

### JAR Eseguibile

```bash
# Build JAR
mvn package -pl adapter-javafx

# Esegui
java -jar adapter-javafx/target/adapter-javafx-0.0.2.jar
```

## 🎨 Utilizzo

### 1. Login

- Avvia l'applicazione
- Inserisci credenziali (default: `admin` / `password`)
- Click su "Accedi"

### 2. Gestione Annotazioni

**Creare una nuova annotazione**:
1. Click su pulsante "Nuova" nella toolbar
2. Compila i campi nel pannello destro:
   - Valore Nota (obbligatorio)
   - Descrizione (obbligatorio)
   - Categoria, Priorità, Tags, Stato (opzionali)
3. Click "Salva"

**Modificare un'annotazione**:
1. Seleziona un'annotazione dalla tabella
2. Click "Modifica"
3. Modifica i campi desiderati
4. Click "Salva"

**Eliminare un'annotazione**:
1. Seleziona un'annotazione dalla tabella
2. Click "Elimina"
3. Conferma l'eliminazione

### 3. Ricerca e Filtri

- **Ricerca testuale**: Inserisci testo nel campo "Ricerca" e click "Cerca"
- **Filtro Stato**: Seleziona uno stato dal dropdown
- **Filtro Categoria**: Seleziona una categoria dal dropdown
- I filtri si applicano automaticamente alla selezione

## 🔗 Integrazione con altri Adapter

L'adapter JavaFX utilizza:
- **`core`**: Servizi di business logic (`AnnotazioneService`)
- **Adapter persistenza** (es. `adapter-sqlite`, `adapter-postgresql`): Storage dati

### Esempio Configurazione Multi-Adapter

**pom.xml** (in `adapter-javafx`):
```xml
<dependencies>
    <!-- Core module -->
    <dependency>
        <groupId>it.alnao.springbootexample</groupId>
        <artifactId>core</artifactId>
    </dependency>
    
    <!-- Adapter persistenza (scegline uno) -->
    <dependency>
        <groupId>it.alnao.springbootexample</groupId>
        <artifactId>adapter-sqlite</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

## 🧪 Testing

```bash
mvn test -pl adapter-javafx
```

## 📝 Note di Sviluppo

### Aggiungere nuove funzionalità

1. **Nuova schermata**: Crea file FXML in `resources/fxml/`
2. **Nuovo controller**: Aggiungi in `controller/` con annotazione `@Component`
3. **Navigazione**: Usa `FXMLLoader` e `Stage.setScene()`

### Esempio apertura nuova finestra

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/nuova.fxml"));
loader.setControllerFactory(springContext::getBean);
Parent root = loader.load();
Stage stage = new Stage();
stage.setScene(new Scene(root));
stage.show();
```

### Threading

JavaFX richiede operazioni UI sul thread principale:
```java
Platform.runLater(() -> {
    label.setText("Aggiornamento UI");
});
```

## 🐛 Troubleshooting

**Errore "JavaFX runtime components are missing"**:
- Verifica versione Java supporta JavaFX
- Usa OpenJDK con JavaFX incluso o aggiungi dipendenze esplicite

**Errore caricamento FXML**:
- Verifica path file FXML (case-sensitive)
- Controlla fx:controller corrisponda alla classe

**Errore Spring context**:
- Verifica `@ComponentScan` includa package corretti
- Controlla dipendenze adapter persistenza

## 📚 Risorse

- [JavaFX Documentation](https://openjfx.io/)
- [Scene Builder](https://gluonhq.com/products/scene-builder/) - Editor visuale FXML
- [Spring Boot + JavaFX](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html)





# &lt; AlNao /&gt;
Tutti i codici sorgente e le informazioni presenti in questo repository sono frutto di un attento e paziente lavoro di sviluppo da parte di AlNao, che si è impegnato a verificarne la correttezza nella misura massima possibile. Qualora parte del codice o dei contenuti sia stato tratto da fonti esterne, la relativa provenienza viene sempre citata, nel rispetto della trasparenza e della proprietà intellettuale. 


Alcuni contenuti e porzioni di codice presenti in questo repository sono stati realizzati anche grazie al supporto di strumenti di intelligenza artificiale, il cui contributo ha permesso di arricchire e velocizzare la produzione del materiale. Ogni informazione e frammento di codice è stato comunque attentamente verificato e validato, con l’obiettivo di garantire la massima qualità e affidabilità dei contenuti offerti. 


Per ulteriori dettagli, approfondimenti o richieste di chiarimento, si invita a consultare il sito [AlNao.it](https://www.alnao.it/).


## License
Made with ❤️ by <a href="https://www.alnao.it">AlNao</a>
&bull; 
Public projects 
<a href="https://www.gnu.org/licenses/gpl-3.0"  valign="middle"> <img src="https://img.shields.io/badge/License-GPL%20v3-blue?style=plastic" alt="GPL v3" valign="middle" /></a>
*Free Software!*


Il software è distribuito secondo i termini della GNU General Public License v3.0. L'uso, la modifica e la ridistribuzione sono consentiti, a condizione che ogni copia o lavoro derivato sia rilasciato con la stessa licenza. Il contenuto è fornito "così com'è", senza alcuna garanzia, esplicita o implicita.


The software is distributed under the terms of the GNU General Public License v3.0. Use, modification, and redistribution are permitted, provided that any copy or derivative work is released under the same license. The content is provided "as is", without any warranty, express or implied.




