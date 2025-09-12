package it.alnao.springbootexample.core.security;

import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.service.auth.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementazione di UserDetailsService per l'autenticazione locale.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    private final UserService userService;
    
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Caricamento utente per username: {}", username);
        
        User user = userService.findByUsername(username)
            .orElseThrow(() -> {
                logger.warn("Utente non trovato: {}", username);
                return new UsernameNotFoundException("Utente non trovato: " + username);
            });
        
        if (!user.isLocalAccount()) {
            logger.warn("Tentativo di login locale per utente OAuth2: {}", username);
            throw new UsernameNotFoundException("Utente non autorizzato per login locale: " + username);
        }
        
        logger.info("Utente trovato: {}, enabled: {}", username, user.isEnabled());
        
        return createUserDetails(user);
    }
    
    private UserDetails createUserDetails(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Aggiungi il ruolo utente
        authorities.add(new SimpleGrantedAuthority("" + user.getRole().name())); //ex "ROLE_" +
        
        logger.info("Authorities per utente {}: {}", user.getUsername(), authorities);
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(!user.isEnabled())
            .credentialsExpired(false)
            .disabled(!user.isEnabled())
            .build();
    }
}
