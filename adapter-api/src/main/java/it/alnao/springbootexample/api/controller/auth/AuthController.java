package it.alnao.springbootexample.api.controller.auth;

import it.alnao.springbootexample.api.dto.auth.*;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.service.auth.UserService;
import it.alnao.springbootexample.core.service.auth.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Controller per la gestione dell'autenticazione ibrida (locale + OAuth2).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints per autenticazione locale e OAuth2")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Login con credenziali locali.
     */
    @PostMapping("/login")
    @Operation(summary = "Login locale", description = "Autenticazione con username e password")
    @ApiResponse(responseCode = "200", description = "Login effettuato con successo")
    @ApiResponse(responseCode = "401", description = "Credenziali non valide")
    public ResponseEntity<JwtResponse> loginLocal(@Valid @RequestBody LoginRequest request) {
        logger.info("POST /api/auth/login - Tentativo di login per username: {}", request.getUsername());
        try {
            logger.info("POST /api/auth/login - Inizio autenticazione per username: {}", request.getUsername());
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            logger.info("POST /api/auth/login - Autenticazione completata per username: {}", request.getUsername());
            
            logger.info("POST /api/auth/login - Ricerca utente per username: {}", request.getUsername());
            User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Utente non trovato"));
            logger.info("POST /api/auth/login - Utente trovato: {} {}", user.getUsername() , user.getAccountType() );
            
            logger.info("POST /api/auth/login - Controllo account locale per username: {}", request.getUsername());
            if (!user.isLocalAccount()) {
                logger.warn("POST /api/auth/login - Utente {} registrato con provider esterno", request.getUsername());
                throw new BadCredentialsException("Utente registrato con provider esterno");
            }
            logger.info("POST /api/auth/login - Account locale confermato per username: {}", request.getUsername());
            
            logger.info("POST /api/auth/login - Generazione token JWT per username: {}", request.getUsername());
            String token = jwtService.generateToken(user);
            logger.info("POST /api/auth/login - Token JWT generato per username: {}", request.getUsername());
            
            logger.info("POST /api/auth/login - Aggiornamento ultimo login per username: {}", request.getUsername());
            userService.updateLastLogin(user.getId());
            logger.info("POST /api/auth/login - Ultimo login aggiornato per username: {}", request.getUsername());
            
            JwtResponse response = new JwtResponse(token, user.getUsername(), user.getEmail(), 
                                                 user.getAccountType(), 86400L); // 24 ore
            
            logger.info("POST /api/auth/login - Login completato con successo per username: {}", request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            logger.warn("POST /api/auth/login - Credenziali non valide per username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("POST /api/auth/login - Errore durante il login per username: {}", request.getUsername(), e);
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Registrazione utente locale.
     */
    @PostMapping("/register")
    @Operation(summary = "Registrazione locale", description = "Creazione nuovo account locale")
    @ApiResponse(responseCode = "201", description = "Utente creato con successo")
    @ApiResponse(responseCode = "400", description = "Dati non validi")
    @ApiResponse(responseCode = "409", description = "Email o username già esistenti")
    public ResponseEntity<UserProfileResponse> registerLocal(@Valid @RequestBody RegisterRequest request) {
        logger.info("POST /api/auth/register - Tentativo di registrazione per username: {}, email: {}", 
                    request.getUsername(), request.getEmail());
        
        // Verifica che email non sia già usata
        if (userService.existsByEmail(request.getEmail())) {
            logger.warn("POST /api/auth/register - Email già esistente: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        if (userService.existsByUsername(request.getUsername())) {
            logger.warn("POST /api/auth/register - Username già esistente: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        User user = userService.createLocalUser(request.getUsername(), request.getEmail(), request.getPassword());
        
        // Imposta nome e cognome se forniti
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        logger.info("POST /api/auth/register - Registrazione completata con successo per username: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserProfileResponse.from(user));
    }
    
    /**
     * Ottiene il profilo dell'utente corrente.
     */
    @GetMapping("/me")
    @Operation(summary = "Profilo utente", description = "Informazioni sull'utente autenticato")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication auth) {
        logger.info("GET /api/auth/me - Richiesta profilo per utente: {}", auth.getName());
        User user = userService.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }
    
    /**
     * Lista dei provider OAuth2 disponibili.
     */
    @GetMapping("/providers")
    @Operation(summary = "Provider OAuth2", description = "Lista dei provider di autenticazione disponibili")
    public ResponseEntity<List<OAuth2ProviderInfo>> getAvailableProviders() {
        logger.info("GET /api/auth/providers - Richiesta lista provider OAuth2");
        List<OAuth2ProviderInfo> providers = Arrays.asList(
            new OAuth2ProviderInfo("google", "Google", "/oauth2/authorization/google"),
            new OAuth2ProviderInfo("github", "GitHub", "/oauth2/authorization/github"),
            new OAuth2ProviderInfo("microsoft", "Microsoft", "/oauth2/authorization/microsoft")
        );
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Refresh del token JWT.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Rinnovo del token JWT")
    public ResponseEntity<JwtResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("POST /api/auth/refresh - Richiesta refresh token");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("POST /api/auth/refresh - Header Authorization mancante o non valido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String refreshToken = authHeader.substring(7);
        
        return jwtService.refreshToken(refreshToken)
            .map(newToken -> {
                logger.info("POST /api/auth/refresh - Token rinnovato con successo");
                JwtResponse response = new JwtResponse();
                response.setToken(newToken);
                response.setTokenType("Bearer");
                response.setExpiresIn(86400L);
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    /**
     * Logout (invalida i token dell'utente).
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalida tutti i token dell'utente")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> logout(Authentication auth) {
        logger.info("POST /api/auth/logout - Logout per utente: {}", auth.getName());
        User user = userService.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        jwtService.invalidateAllUserTokens(user.getId());
        
        logger.info("POST /api/auth/logout - Logout completato per utente: {}", auth.getName());
        return ResponseEntity.ok().build();
    }
}
