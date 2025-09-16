package it.alnao.springbootexample.sqlite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
@Profile("sqlite")
//@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app.init")
public class DataInitializerProperties {
    
    private Users users = new Users();
/*  NOTA: versione alternativa che prevede un file "defaultUsers.yaml" in resources del adapter-sqlite, 
    ma commentata perchè il blocco è stato posizionato nel application.yaml globale dell'applicazione
    e quindi non serve più.

    private final ResourceLoader resourceLoader;
    private static final Logger logger = LoggerFactory.getLogger(DataInitializerProperties.class);
    public DataInitializerProperties(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    @PostConstruct
    public void initUsers() {
        logger.info("Inizializzazione utenti di default...");
        this.users = caricaUtentiDaYaml();
        logger.info("Caricati {} utenti di default", users.getDefaultUsers().size());
    }

    private Users caricaUtentiDaYaml() {
        try {
            // Carica il file defaultUsers.yaml dalla cartella resources
            InputStream inputStream = resourceLoader.getResource("classpath:defaultUsers.yaml").getInputStream();
            Yaml yaml = new Yaml();
            Users loadedUsers = yaml.loadAs(inputStream, Users.class);
            logger.info("Utenti caricati da defaultUsers.yaml: {}", loadedUsers);
            return loadedUsers;
        } catch (Exception e) {
            logger.error("Errore caricamento defaultUsers.yaml", e);
            return this.users; // fallback su configurazione vuota
        }
    }
    
*/

    
    public Users getUsers() { return users; }
    public void setUsers(Users users) { this.users = users; }
    
    public static class Users {
        private boolean enabled = true;
        private List<UserConfig> defaultUsers = new ArrayList<>();
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public List<UserConfig> getDefaultUsers() { return defaultUsers; }
        public void setDefaultUsers(List<UserConfig> defaultUsers) { this.defaultUsers = defaultUsers; }

        @Override
        public String toString() {
            return "Users{" +
                    "enabled=" + enabled +
                    ", defaultUsers=" + defaultUsers +
                    '}';
        }
    }
    
    public static class UserConfig {
        private String username;
        private String email;
        private String password;
        private String role = "USER";
        
        // Getters e setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        @Override
        public String toString() {
            return "UserConfig{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", password='" + password + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
}