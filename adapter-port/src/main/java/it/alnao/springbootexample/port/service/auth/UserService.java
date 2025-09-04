package it.alnao.springbootexample.port.service.auth;

import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.domain.auth.AccountType;

import java.util.List;
import java.util.Optional;

/**
 * Service interface per la gestione degli utenti.
 * Implementa la business logic per utenti locali e OAuth2.
 */
public interface UserService {
    
    /**
     * Crea un nuovo utente locale.
     */
    User createLocalUser(String username, String email, String password);
    
    /**
     * Crea un nuovo utente OAuth2.
     */
    User createOAuth2User(String email, String firstName, String lastName, 
                         AccountType accountType, String externalId);
    
    /**
     * Aggiorna un utente OAuth2 con le informazioni dal provider.
     */
    User updateOAuth2User(User user, String firstName, String lastName, String avatarUrl);
    
    /**
     * Trova un utente per ID.
     */
    Optional<User> findById(String userId);
    
    /**
     * Trova un utente per username.
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Trova un utente per email.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Trova un utente per email e provider.
     */
    Optional<User> findByEmailAndProvider(String email, String provider);
    
    /**
     * Trova un utente per external ID e account type.
     */
    Optional<User> findByExternalIdAndAccountType(String externalId, AccountType accountType);
    
    /**
     * Verifica se esiste un utente con l'email specificata.
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica se esiste un utente con lo username specificato.
     */
    boolean existsByUsername(String username);
    
    /**
     * Aggiorna l'ultimo login di un utente.
     */
    void updateLastLogin(String userId);
    
    /**
     * Collega un provider OAuth2 a un utente esistente.
     */
    void linkOAuth2Provider(User user, String provider, String externalId, String providerEmail);
    
    /**
     * Scollega un provider OAuth2 da un utente.
     */
    void unlinkOAuth2Provider(User user, String provider);
    
    /**
     * Cambia la password di un utente locale.
     */
    void changePassword(String userId, String newPassword);
    
    /**
     * Abilita o disabilita un utente.
     */
    void setUserEnabled(String userId, boolean enabled);
    
    /**
     * Verifica email di un utente.
     */
    void verifyEmail(String userId);
    
    /**
     * Trova tutti gli utenti.
     */
    List<User> findAllUsers();
    
    /**
     * Trova utenti per tipo di account.
     */
    List<User> findUsersByAccountType(AccountType accountType);
    
    /**
     * Statistiche utenti.
     */
    UserStatistics getUserStatistics();
    
    /**
     * Elimina un utente.
     */
    void deleteUser(String userId);
}
