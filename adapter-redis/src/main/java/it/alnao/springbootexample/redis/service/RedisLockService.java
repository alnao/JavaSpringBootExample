package it.alnao.springbootexample.redis.service;

import it.alnao.springbootexample.core.service.LockService;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementazione Redis del servizio di lock distribuiti.
 * Utilizza Redisson per gestire lock distribuiti su annotazioni.
 */
@Service
@Profile({"kube", "aws", "azure"})
public class RedisLockService implements LockService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisLockService.class);
    private static final String LOCK_PREFIX = "annotation:lock:";
    private static final String OWNER_MAP = "annotation:lock:owners";
    
    @Autowired
    private RedissonClient redisson;
    
    @Override
    public boolean acquireLock(UUID annotazioneId, String utente, long timeoutSeconds) {
        String lockKey = LOCK_PREFIX + annotazioneId.toString();
        RLock lock = redisson.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(timeoutSeconds, timeoutSeconds, TimeUnit.SECONDS);
            
            if (acquired) {
                // Salva il proprietario del lock
                RMap<String, String> ownerMap = redisson.getMap(OWNER_MAP);
                ownerMap.put(annotazioneId.toString(), utente);
                
                logger.info("Lock acquisito su annotazione {} da utente {}", annotazioneId, utente);
            } else {
                logger.warn("Impossibile acquisire lock su annotazione {} per utente {}", annotazioneId, utente);
            }
            
            return acquired;
            
        } catch (InterruptedException e) {
            logger.error("Errore durante acquisizione lock su annotazione {}: {}", annotazioneId, e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void releaseLock(UUID annotazioneId, String utente) {
        String lockKey = LOCK_PREFIX + annotazioneId.toString();
        RLock lock = redisson.getLock(lockKey);
        RMap<String, String> ownerMap = redisson.getMap(OWNER_MAP);
        
        try {
            // Verifica il proprietario prima di rilasciare
            String currentOwner = ownerMap.get(annotazioneId.toString());
            
            if (currentOwner != null && !currentOwner.equals(utente)) {
                logger.warn("Tentativo di rilasciare lock su annotazione {} da utente {} ma il proprietario è {}", 
                    annotazioneId, utente, currentOwner);
                return;
            }
            
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                ownerMap.remove(annotazioneId.toString());
                logger.info("Lock rilasciato su annotazione {} da utente {}", annotazioneId, utente);
            } else if (lock.isLocked()) {
                // Lock posseduto da altro thread, forza il rilascio se l'utente è il proprietario
                if (currentOwner != null && currentOwner.equals(utente)) {
                    lock.forceUnlock();
                    ownerMap.remove(annotazioneId.toString());
                    logger.info("Lock forzato rilascio su annotazione {} da utente {}", annotazioneId, utente);
                } else {
                    logger.warn("Tentativo di rilasciare lock non posseduto su annotazione {} da utente {}", 
                        annotazioneId, utente);
                }
            } else {
                // Lock già rilasciato, rimuovi solo il proprietario se presente
                if (currentOwner != null) {
                    ownerMap.remove(annotazioneId.toString());
                    logger.info("Rimosso proprietario per lock già rilasciato su annotazione {}", annotazioneId);
                }
            }
        } catch (IllegalMonitorStateException e) {
            logger.warn("Lock già rilasciato su annotazione {}, pulizia proprietario", annotazioneId);
            ownerMap.remove(annotazioneId.toString());
        }
    }
    
    @Override
    public boolean isLocked(UUID annotazioneId) {
        String lockKey = LOCK_PREFIX + annotazioneId.toString();
        RLock lock = redisson.getLock(lockKey);
        
        // Verifica se il lock Redisson è attivo
        if (!lock.isLocked()) {
            // Se il lock non è attivo, pulisci eventuale proprietario residuo
            RMap<String, String> ownerMap = redisson.getMap(OWNER_MAP);
            String owner = ownerMap.remove(annotazioneId.toString());
            if (owner != null) {
                logger.info("Rimosso proprietario residuo {} per lock non più attivo su annotazione {}", 
                    owner, annotazioneId);
            }
            return false;
        }
        
        // Verifica se c'è un proprietario registrato
        RMap<String, String> ownerMap = redisson.getMap(OWNER_MAP);
        String owner = ownerMap.get(annotazioneId.toString());
        
        if (owner == null) {
            // Lock Redisson attivo ma nessun proprietario: forza rilascio
            logger.warn("Lock Redisson attivo su annotazione {} ma nessun proprietario registrato, forzo rilascio", 
                annotazioneId);
            try {
                lock.forceUnlock();
            } catch (IllegalMonitorStateException e) {
                logger.warn("Impossibile forzare unlock su annotazione {}: {}", annotazioneId, e.getMessage());
            }
            return false;
        }
        
        return true;
    }
    
    @Override
    public Optional<String> getOwner(UUID annotazioneId) {
        RMap<String, String> ownerMap = redisson.getMap(OWNER_MAP);
        String owner = ownerMap.get(annotazioneId.toString());
        
        // Se non c'è proprietario ma il lock è ancora attivo, pulisci
        if (owner == null) {
            String lockKey = LOCK_PREFIX + annotazioneId.toString();
            RLock lock = redisson.getLock(lockKey);
            if (lock.isLocked()) {
                logger.warn("Lock Redisson attivo su annotazione {} ma nessun proprietario, forzo rilascio", 
                    annotazioneId);
                try {
                    lock.forceUnlock();
                } catch (IllegalMonitorStateException e) {
                    logger.warn("Impossibile forzare unlock su annotazione {}: {}", annotazioneId, e.getMessage());
                }
            }
        }
        
        return Optional.ofNullable(owner);
    }
}
