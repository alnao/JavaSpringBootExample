package it.alnao.springbootexample.port.repository.auth;

import it.alnao.springbootexample.port.domain.auth.UserProvider;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface per la gestione dei provider OAuth2 degli utenti.
 */
public interface UserProviderRepository {
    
    /**
     * Salva o aggiorna un provider utente.
     */
    UserProvider save(UserProvider userProvider);
    
    /**
     * Trova un provider per ID.
     */
    Optional<UserProvider> findById(String id);
    
    /**
     * Trova i provider di un utente.
     */
    List<UserProvider> findByUserId(String userId);
    
    /**
     * Trova un provider specifico di un utente.
     */
    Optional<UserProvider> findByUserIdAndProvider(String userId, String provider);
    
    /**
     * Trova un utente per provider e provider user ID.
     */
    Optional<UserProvider> findByProviderAndProviderUserId(String provider, String providerUserId);
    
    /**
     * Verifica se un utente ha un provider specifico.
     */
    boolean existsByUserIdAndProvider(String userId, String provider);
    
    /**
     * Elimina un provider per ID.
     */
    void deleteById(String id);
    
    /**
     * Elimina tutti i provider di un utente.
     */
    void deleteByUserId(String userId);
    
    /**
     * Elimina un provider specifico di un utente.
     */
    void deleteByUserIdAndProvider(String userId, String provider);
    
    /**
     * Trova tutti i provider.
     */
    List<UserProvider> findAll();
}
