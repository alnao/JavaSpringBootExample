package it.alnao.springbootexample.core.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta per il servizio di gestione lock distribuiti.
 * Previene modifiche concorrenti alla stessa annotazione.
 */
public interface LockService {
    
    /**
     * Acquisisce un lock su un'annotazione per un utente specifico
     * 
     * @param annotazioneId ID dell'annotazione da bloccare
     * @param utente Username che richiede il lock
     * @param timeoutSeconds Timeout massimo di attesa per acquisire il lock
     * @return true se il lock è stato acquisito, false altrimenti
     */
    boolean acquireLock(UUID annotazioneId, String utente, long timeoutSeconds);
    
    /**
     * Rilascia un lock su un'annotazione
     * 
     * @param annotazioneId ID dell'annotazione da sbloccare
     * @param utente Username che possiede il lock
     */
    void releaseLock(UUID annotazioneId, String utente);
    
    /**
     * Verifica se un'annotazione è attualmente bloccata
     * 
     * @param annotazioneId ID dell'annotazione da verificare
     * @return true se l'annotazione è bloccata, false altrimenti
     */
    boolean isLocked(UUID annotazioneId);
    
    /**
     * Ottiene il proprietario del lock su un'annotazione
     * 
     * @param annotazioneId ID dell'annotazione
     * @return Optional con username del proprietario del lock, vuoto se non bloccata
     */
    Optional<String> getOwner(UUID annotazioneId);
}
