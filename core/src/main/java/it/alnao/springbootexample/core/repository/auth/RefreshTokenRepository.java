package it.alnao.springbootexample.core.repository.auth;

import it.alnao.springbootexample.core.domain.auth.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface per la gestione dei refresh token.
 */
public interface RefreshTokenRepository {
    
    /**
     * Salva o aggiorna un refresh token.
     */
    RefreshToken save(RefreshToken refreshToken);
    
    /**
     * Trova un refresh token per token.
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Trova tutti i refresh token di un utente.
     */
    List<RefreshToken> findByUserId(String userId);
    
    /**
     * Elimina un refresh token.
     */
    void delete(RefreshToken refreshToken);
    
    /**
     * Elimina un refresh token per token.
     */
    void deleteByToken(String token);
    
    /**
     * Elimina tutti i refresh token di un utente.
     */
    void deleteByUserId(String userId);
    
    /**
     * Elimina tutti i token scaduti.
     */
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
    
    /**
     * Trova tutti i refresh token.
     */
    List<RefreshToken> findAll();
}
