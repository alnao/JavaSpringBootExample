package it.alnao.springbootexample.azure.repository;

import it.alnao.springbootexample.azure.entity.AnnotazioneMetadataSqlServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnotazioneMetadataSqlServerRepository extends JpaRepository<AnnotazioneMetadataSqlServerEntity, String> {
    List<AnnotazioneMetadataSqlServerEntity> findByDescrizioneContainingIgnoreCase(String descrizione);
    List<AnnotazioneMetadataSqlServerEntity> findByCategoria(String categoria);
    List<AnnotazioneMetadataSqlServerEntity> findByPubblica(Boolean pubblica);
    List<AnnotazioneMetadataSqlServerEntity> findByStato(String stato);
    List<AnnotazioneMetadataSqlServerEntity> findByPriorita(Integer priorita);
    List<AnnotazioneMetadataSqlServerEntity> findByUtenteCreazione(String utenteCreazione);
    long countByUtenteCreazione(String utenteCreazione);
    List<AnnotazioneMetadataSqlServerEntity> findByDataInserimentoBetween(LocalDateTime dataInizio, LocalDateTime dataFine);
}
