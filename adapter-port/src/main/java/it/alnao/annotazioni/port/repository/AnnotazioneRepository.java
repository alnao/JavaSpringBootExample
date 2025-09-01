package it.alnao.annotazioni.port.repository;

import it.alnao.annotazioni.port.domain.Annotazione;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta per il repository delle annotazioni (NoSQL)
 */
public interface AnnotazioneRepository {
    
    /**
     * Salva un'annotazione
     */
    Annotazione save(Annotazione annotazione);
    
    /**
     * Trova un'annotazione per ID
     */
    Optional<Annotazione> findById(UUID id);
    
    /**
     * Trova tutte le annotazioni
     */
    List<Annotazione> findAll();
    
    /**
     * Trova annotazioni per versione
     */
    List<Annotazione> findByVersioneNota(String versione);
    
    /**
     * Trova annotazioni che contengono del testo
     */
    List<Annotazione> findByValoreNotaContaining(String testo);
    
    /**
     * Elimina un'annotazione per ID
     */
    void deleteById(UUID id);
    
    /**
     * Verifica se esiste un'annotazione
     */
    boolean existsById(UUID id);
    
    /**
     * Conta tutte le annotazioni
     */
    long count();
}
