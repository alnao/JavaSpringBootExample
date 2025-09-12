package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.sqlite.entity.AnnotazioneStoricoStatiSQLiteEntity;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository JPA per lo storico dei cambi di stato delle annotazioni su SQLite
 */
@Repository
@Profile("sqlite")
public interface AnnotazioneStoricoStatiSQLiteRepository extends JpaRepository<AnnotazioneStoricoStatiSQLiteEntity, String> {
    
    /**
     * Trova tutti i cambi di stato per una specifica annotazione
     */
    List<AnnotazioneStoricoStatiSQLiteEntity> findByIdAnnotazioneOrderByDataModificaDesc(String idAnnotazione);
    
    /**
     * Trova i cambi di stato per utente
     */
    List<AnnotazioneStoricoStatiSQLiteEntity> findByUtenteOrderByDataModificaDesc(String utente);
    
    /**
     * Trova i cambi di stato in un periodo
     */
    List<AnnotazioneStoricoStatiSQLiteEntity> findByDataModificaBetween(LocalDateTime inizio, LocalDateTime fine);
    
    /**
     * Trova i cambi di stato per un nuovo stato specifico
     */
    List<AnnotazioneStoricoStatiSQLiteEntity> findByStatoNewOrderByDataModificaDesc(String statoNew);
    
    /**
     * Trova i cambi di stato per un vecchio stato specifico
     */
    List<AnnotazioneStoricoStatiSQLiteEntity> findByStatoOldOrderByDataModificaDesc(String statoOld);
    
    /**
     * Trova i cambi di stato per annotazione e utente
     */
    List<AnnotazioneStoricoStatiSQLiteEntity> findByIdAnnotazioneAndUtenteOrderByDataModificaDesc(String idAnnotazione, String utente);
    
    /**
     * Conta i cambi di stato per una annotazione
     */
    long countByIdAnnotazione(String idAnnotazione);
    
    /**
     * Conta i cambi di stato per un utente
     */
    long countByUtente(String utente);
}
