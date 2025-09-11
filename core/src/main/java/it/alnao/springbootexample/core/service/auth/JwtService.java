package it.alnao.springbootexample.core.service.auth;

import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import it.alnao.springbootexample.core.domain.auth.User;

import java.util.Optional;

/**
 * Service interface per la gestione dei JWT token.
 */
public interface JwtService {
    
    /**
     * Genera un token JWT per un utente.
     */
    String generateToken(User user);
    
    /**
     * Genera un refresh token per un utente.
     */
    RefreshToken generateRefreshToken(User user);
    
    /**
     * Valida un token JWT.
     */
    boolean validateToken(String token, User user);
    
    /**
     * Estrae lo username dal token JWT.
     */
    String getUsernameFromToken(String token);
    
    /**
     * Estrae l'ID utente dal token JWT.
     */
    String getUserIdFromToken(String token);
    
    /**
     * Verifica se un token è scaduto.
     */
    boolean isTokenExpired(String token);
    
    /**
     * Rinnova un token usando il refresh token.
     */
    Optional<String> refreshToken(String refreshToken);
    
    /**
     * Invalida un refresh token.
     */
    void invalidateRefreshToken(String refreshToken);
    
    /**
     * Invalida tutti i refresh token di un utente.
     */
    void invalidateAllUserTokens(String userId);
    
    /**
     * Pulisce i token scaduti.
     */
    void cleanExpiredTokens();
}
