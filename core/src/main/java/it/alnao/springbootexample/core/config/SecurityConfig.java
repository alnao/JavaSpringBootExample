package it.alnao.springbootexample.core.config;

import it.alnao.springbootexample.core.security.JwtAuthenticationEntryPoint;
import it.alnao.springbootexample.core.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configurazione Spring Security per JWT Authentication.
 * Configurazione standard che può essere riutilizzata in tutti gli adapter.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    public static class JwtConfig {
        private final String secret;
        private final Long expiration;
        private final Long refreshExpiration;

        public JwtConfig(String secret, Long expiration, Long refreshExpiration) {
            this.secret = secret;
            this.expiration = expiration;
            this.refreshExpiration = refreshExpiration;
        }
        public String getSecret() { return secret; }
        public Long getExpiration() { return expiration; }
        public Long getRefreshExpiration() { return refreshExpiration; }
    }

    @Value("${gestione-annotazioni.jwt.secret:mySecretKey1234567890abcdefghijklmnopqrstuvwxyz}")
    private String jwtSecret;

    @Value("${gestione-annotazioni.jwt.expiration:86400}")
    private Long jwtExpiration;

    @Value("${gestione-annotazioni.jwt.refresh-expiration:604800}")
    private Long jwtRefreshExpiration;

    @Bean
    public JwtConfig jwtConfig() {
        return new JwtConfig(jwtSecret, jwtExpiration, jwtRefreshExpiration);
    }

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // Static resources (CSS, JS, images, etc.)
                        .requestMatchers("/", "/index.html", "/*.html").permitAll()
                        .requestMatchers("/js/**", "/css/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/static/**", "/public/**").permitAll()
                        // Public endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/oauth2/**").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        // OAuth2 endpoints
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        // Protected endpoints
                        .requestMatchers("/api/auth/profile", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // OAuth2 Login configuration
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/auth/oauth2/success", true)
                        .failureUrl("/api/auth/oauth2/failure")
                );

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS" , "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
