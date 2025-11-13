package it.alnao.springbootexample.sqlite.service;

import it.alnao.springbootexample.core.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementazione in-memory del servizio di lock per profilo SQLite.
 * Utilizza ConcurrentHashMap per gestire lock locali (non distribuiti).
 * 
 * ATTENZIONE: Questa implementazione NON è adatta per ambienti multi-istanza.
 * Funziona solo in ambiente single-instance come il profilo sqlite.
 */
@Service
@Profile("sqlite")
public class InMemoryLockService implements LockService {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryLockService.class);
    
    private final ConcurrentHashMap<UUID, String> locks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lockTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lockTimeouts = new ConcurrentHashMap<>();
    
    @Override
    public boolean acquireLock(UUID annotazioneId, String utente, long timeoutSeconds) {
        // Pulisci eventuali lock scaduti
        cleanExpiredLocks();
        
        String existingOwner = locks.putIfAbsent(annotazioneId, utente);
        
        if (existingOwner == null) {
            // Lock acquisito con successo
            lockTimestamps.put(annotazioneId, System.currentTimeMillis());
            lockTimeouts.put(annotazioneId, timeoutSeconds * 1000); // Converti in millisecondi
            logger.info("Lock in-memory acquisito su annotazione {} da utente {} con timeout di {} secondi", 
                annotazioneId, utente, timeoutSeconds);
            return true;
        } else if (existingOwner.equals(utente)) {
            // Lo stesso utente può ri-acquisire il lock
            lockTimestamps.put(annotazioneId, System.currentTimeMillis());
            lockTimeouts.put(annotazioneId, timeoutSeconds * 1000); // Aggiorna il timeout
            logger.info("Lock in-memory ri-acquisito su annotazione {} da utente {} con timeout di {} secondi", 
                annotazioneId, utente, timeoutSeconds);
            return true;
        } else {
            logger.warn("Impossibile acquisire lock in-memory su annotazione {} per utente {}, già posseduto da {}", 
                annotazioneId, utente, existingOwner);
            return false;
        }
    }
    
    @Override
    public void releaseLock(UUID annotazioneId, String utente) {
        String owner = locks.get(annotazioneId);
        
        if (owner != null && owner.equals(utente)) {
            locks.remove(annotazioneId);
            lockTimestamps.remove(annotazioneId);
            lockTimeouts.remove(annotazioneId);
            logger.info("Lock in-memory rilasciato su annotazione {} da utente {}", annotazioneId, utente);
        } else {
            logger.warn("Tentativo di rilasciare lock in-memory non posseduto su annotazione {} da utente {}", 
                annotazioneId, utente);
        }
    }
    
    @Override
    public boolean isLocked(UUID annotazioneId) {
        cleanExpiredLocks();
        return locks.containsKey(annotazioneId);
    }
    
    @Override
    public Optional<String> getOwner(UUID annotazioneId) {
        cleanExpiredLocks();
        return Optional.ofNullable(locks.get(annotazioneId));
    }
    
    /**
     * Pulisce i lock scaduti (oltre il timeout configurato)
     */
    private void cleanExpiredLocks() {
        long now = System.currentTimeMillis();
        
        // Itera su una copia delle chiavi per evitare ConcurrentModificationException
        locks.keySet().forEach(annotazioneId -> {
            Long timestamp = lockTimestamps.get(annotazioneId);
            if (timestamp == null) {
                // Lock senza timestamp, rimuovi per sicurezza
                locks.remove(annotazioneId);
                lockTimeouts.remove(annotazioneId);
                logger.warn("Rimosso lock in-memory senza timestamp per annotazione {}", annotazioneId);
                return;
            }
            
            Long timeout = lockTimeouts.get(annotazioneId);
            // Se non c'è timeout configurato, usa 5 minuti come default
            long effectiveTimeout = (timeout != null) ? timeout : (5 * 60 * 1000);
            
            if (now - timestamp > effectiveTimeout) {
                String owner = locks.remove(annotazioneId);
                lockTimestamps.remove(annotazioneId);
                lockTimeouts.remove(annotazioneId);
                logger.info("Lock in-memory auto-rilasciato su annotazione {} (timeout di {}ms scaduto), era posseduto da {}", 
                    annotazioneId, effectiveTimeout, owner);
            }
        });
    }
}
