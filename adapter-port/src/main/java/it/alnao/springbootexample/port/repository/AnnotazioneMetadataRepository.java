package it.alnao.springbootexample.port.repository;

import it.alnao.springbootexample.port.domain.AnnotazioneMetadata;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta per il repository dei metadati delle annotazioni (SQL)
 */
public interface AnnotazioneMetadataRepository {
    
    /**
     * Salva i metadati di un'annotazione
     */
    AnnotazioneMetadata save(AnnotazioneMetadata metadata);
    
    /**
     * Trova metadati per ID
     */
    Optional<AnnotazioneMetadata> findById(UUID id);
    
    /**
     * Trova tutti i metadati
     */
    List<AnnotazioneMetadata> findAll();
    
    /**
     * Trova metadati per utente creazione
     */
    List<AnnotazioneMetadata> findByUtenteCreazione(String utente);
    
    /**
     * Trova metadati per categoria
     */
    List<AnnotazioneMetadata> findByCategoria(String categoria);
    
    /**
     * Trova metadati per range di date
     */
    List<AnnotazioneMetadata> findByDataInserimentoBetween(LocalDateTime inizio, LocalDateTime fine);
    
    /**
     * Trova metadati pubblici
     */
    List<AnnotazioneMetadata> findByPubblica(Boolean pubblica);
    
    /**
     * Trova metadati per priorità
     */
    List<AnnotazioneMetadata> findByPriorita(Integer priorita);
    
    /**
     * Trova metadati che contengono testo nella descrizione
     */
    List<AnnotazioneMetadata> findByDescrizioneContaining(String testo);
    
    /**
     * Trova metadati che contengono un tag
     */
    List<AnnotazioneMetadata> findByTagsContaining(String tag);
    
    /**
     * Elimina metadati per ID
     */
    void deleteById(UUID id);
    
    /**
     * Verifica se esistono metadati
     */
    boolean existsById(UUID id);
    
    /**
     * Conta tutti i metadati
     */
    long count();
    
    /**
     * Conta metadati per utente
     */
    long countByUtenteCreazione(String utente);
}
