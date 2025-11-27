package it.alnao.springbootexample.javafx.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Disabilita Spring Security per l'applicazione JavaFX desktop
 */
@Configuration
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class
})
public class SecurityDisabledConfig {
    // Configurazione vuota per disabilitare Security
}
