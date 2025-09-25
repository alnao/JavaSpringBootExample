package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.sqlite.entity.AnnotazioneInviata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@Profile("sqlite")
public interface AnnotazioneInviataRepository extends JpaRepository<AnnotazioneInviata, Long> {
    
    /**
     * Trova annotazioni inviate per ID annotazione
     */
    List<AnnotazioneInviata> findByAnnotazioneId(UUID annotazioneId);
    
    /**
     * Trova annotazioni inviate in un periodo
     */
    List<AnnotazioneInviata> findByDataInvioBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Trova annotazioni per stato invio
     */
    List<AnnotazioneInviata> findByStatoInvio(String statoInvio);
    
    /**
     * Trova le ultime N annotazioni inviate
     */
    List<AnnotazioneInviata> findTop10ByOrderByDataInvioDesc();
    
    /**
     * Conta le annotazioni inviate per stato
     */
    long countByStatoInvio(String statoInvio);
}
