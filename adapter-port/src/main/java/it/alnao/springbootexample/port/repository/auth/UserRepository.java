package it.alnao.springbootexample.port.repository.auth;

import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.domain.auth.AccountType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface per la gestione degli utenti.
 * Supporta sia utenti locali che OAuth2.
 */
public interface UserRepository {
    
    /**
     * Salva o aggiorna un utente.
     */
    User save(User user);
    
    /**
     * Trova un utente per ID.
     */
    Optional<User> findById(String id);
    
    /**
     * Trova un utente per username (solo account locali).
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Trova un utente per email.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Trova un utente per email e tipo di account.
     */
    Optional<User> findByEmailAndAccountType(String email, AccountType accountType);
    
    /**
     * Trova un utente per external ID e tipo di account.
     */
    Optional<User> findByExternalIdAndAccountType(String externalId, AccountType accountType);
    
    /**
     * Verifica se esiste un utente con la email specificata.
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica se esiste un utente con lo username specificato.
     */
    boolean existsByUsername(String username);
    
    /**
     * Trova tutti gli utenti.
     */
    List<User> findAll();
    
    /**
     * Trova utenti per tipo di account.
     */
    List<User> findByAccountType(AccountType accountType);
    
    /**
     * Trova utenti attivi.
     */
    List<User> findByEnabled(boolean enabled);
    
    /**
     * Elimina un utente per ID.
     */
    void deleteById(String id);
    
    /**
     * Elimina un utente.
     */
    void delete(User user);
    
    /**
     * Conta il numero totale di utenti.
     */
    long count();
    
    /**
     * Conta gli utenti per tipo di account.
     */
    long countByAccountType(AccountType accountType);
}
