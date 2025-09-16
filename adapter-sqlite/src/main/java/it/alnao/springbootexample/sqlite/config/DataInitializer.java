package it.alnao.springbootexample.sqlite.config;

import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import it.alnao.springbootexample.sqlite.service.AnnotazioneServiceSQLiteImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Profile("sqlite")
public class DataInitializer implements ApplicationRunner {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataInitializerProperties properties;

    @Autowired
    public DataInitializer(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder,
                          DataInitializerProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
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