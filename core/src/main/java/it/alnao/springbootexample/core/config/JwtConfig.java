package it.alnao.springbootexample.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configurazione JWT e PasswordEncoder separata da SecurityConfig 
 * per permettere l'uso in applicazioni non-web (es. JavaFX desktop).
 */
@Configuration
public class JwtConfig {
    
    @Value("${gestione-annotazioni.jwt.secret:mySecretKey1234567890abcdefghijklmnopqrstuvwxyz}")
    private String jwtSecret;

    @Value("${gestione-annotazioni.jwt.expiration:86400}")
    private Long jwtExpiration;

    @Value("${gestione-annotazioni.jwt.refresh-expiration:604800}")
    private Long jwtRefreshExpiration;

    @Bean
    public JwtConfigBean jwtConfigBean() {
        return new JwtConfigBean(jwtSecret, jwtExpiration, jwtRefreshExpiration);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean che contiene i parametri di configurazione JWT.
     */
    public static class JwtConfigBean {
        private final String secret;
        private final Long expiration;
        private final Long refreshExpiration;

        public JwtConfigBean(String secret, Long expiration, Long refreshExpiration) {
            this.secret = secret;
            this.expiration = expiration;
            this.refreshExpiration = refreshExpiration;
        }

        public String getSecret() {
            return secret;
        }

        public Long getExpiration() {
            return expiration;
        }

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }
    }
}
