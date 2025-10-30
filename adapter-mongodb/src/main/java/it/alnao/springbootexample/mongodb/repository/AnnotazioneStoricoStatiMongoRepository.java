package it.alnao.springbootexample.mongodb.repository;

import it.alnao.springbootexample.mongodb.entity.AnnotazioneStoricoStatiEntity;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository Spring Data MongoDB per lo storico dei cambi di stato delle annotazioni
 */
@Repository
@Profile("kube")
public interface AnnotazioneStoricoStatiMongoRepository extends MongoRepository<AnnotazioneStoricoStatiEntity, String> {
    
    /**
     * Trova tutti i cambi di stato per una specifica annotazione
     */
    List<AnnotazioneStoricoStatiEntity> findByIdAnnotazioneOrderByDataModificaDesc(String idAnnotazione);
    
    /**
     * Trova i cambi di stato per utente
     */
    List<AnnotazioneStoricoStatiEntity> findByUtenteOrderByDataModificaDesc(String utente);
    
    /**
     * Trova i cambi di stato in un periodo
     */
    List<AnnotazioneStoricoStatiEntity> findByDataModificaBetween(LocalDateTime inizio, LocalDateTime fine);
    
    /**
     * Trova i cambi di stato per un nuovo stato specifico
     */
    List<AnnotazioneStoricoStatiEntity> findByStatoNewOrderByDataModificaDesc(String statoNew);
    
    /**
     * Trova i cambi di stato per un vecchio stato specifico
     */
    List<AnnotazioneStoricoStatiEntity> findByStatoOldOrderByDataModificaDesc(String statoOld);
    
    /**
     * Trova i cambi di stato per annotazione e utente
     */
    List<AnnotazioneStoricoStatiEntity> findByIdAnnotazioneAndUtenteOrderByDataModificaDesc(String idAnnotazione, String utente);
}
