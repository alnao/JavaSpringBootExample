package it.alnao.springbootexample.sqlite.config;

import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import it.alnao.springbootexample.sqlite.service.AnnotazioneServiceSQLiteImpl;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@Profile("sqlite")
public class DataInitializer implements ApplicationRunner {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataInitializerProperties properties;

    public DataInitializer(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder,
                          DataInitializerProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    /**
     * Controllo eseguito durante l'avvio del contesto, prima che il web server si metta in ascolto:
     * con la creazione utenti attiva ogni utente iniziale deve avere una password, fornita dalla
     * variabile d'ambiente APP_USER_<USERNAME>_PASSWORD. Il messaggio riporta solo i nomi delle variabili.
     */
    @PostConstruct
    public void verificaCredenziali() {
        if (!properties.getUsers().isEnabled()) {
            return;
        }
        List<String> variabiliMancanti = properties.getUsers().getDefaultUsers().stream()
                .filter(userConfig -> userConfig.getPassword() == null || userConfig.getPassword().isBlank())
                .map(userConfig -> nomeVariabilePassword(userConfig.getUsername()))
                .toList();
        if (!variabiliMancanti.isEmpty()) {
            throw new IllegalStateException("Credenziale mancante: " + String.join(", ", variabiliMancanti));
        }
    }

    static String nomeVariabilePassword(String username) {
        String nome = username == null ? "" : username.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
        return "APP_USER_" + nome + "_PASSWORD";
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("DataInitializer start con le properties {}", properties.getUsers().toString());
        if (properties.getUsers().isEnabled()) {
            createDefaultUsers();
        }
    }

    private void createDefaultUsers() {
        for (DataInitializerProperties.UserConfig userConfig : properties.getUsers().getDefaultUsers()) {
            logger.info("Verifica esistenza utente: " + userConfig.getUsername());
            if (!userRepository.existsByUsername(userConfig.getUsername())) {
                logger.info("Creazione utente: " + userConfig.getUsername());
                User user = new User();
                user.setId(UUID.randomUUID().toString());
                user.setUsername(userConfig.getUsername());
                user.setEmail(userConfig.getEmail());
                user.setPassword(passwordEncoder.encode(userConfig.getPassword()));
                user.setRole(UserRole.valueOf(userConfig.getRole()));
                user.setAccountType(AccountType.LOCAL);
                user.setEnabled(true);
                user.setEmailVerified(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                
                userRepository.save(user);
                logger.info("Utente creato: " + userConfig.getUsername() + " / " + UserRole.valueOf(userConfig.getRole()) );
            }
        }
        logger.info("Inizializzazione utenti completata");
    }
}