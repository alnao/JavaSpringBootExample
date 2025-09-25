package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Profile("sqlite")
public interface AnnotazioneMetadataSQLiteJpaRepository extends JpaRepository<AnnotazioneMetadataSQLiteEntity, String> {
    
    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.stato = :stato")
    List<AnnotazioneMetadataSQLiteEntity> findByStato(@Param("stato") String stato);

    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.dataInserimento BETWEEN :inizio AND :fine")
    List<AnnotazioneMetadataSQLiteEntity> findByDataInserimentoBetween(@Param("inizio") LocalDateTime inizio, @Param("fine") LocalDateTime fine);

    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.utenteCreazione = :utente")
    List<AnnotazioneMetadataSQLiteEntity> findByUtenteCreazione(@Param("utente") String utente);

    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.categoria = :categoria")
    List<AnnotazioneMetadataSQLiteEntity> findByCategoria(@Param("categoria") String categoria);

    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.pubblica = :pubblica")
    List<AnnotazioneMetadataSQLiteEntity> findByPubblica(@Param("pubblica") Boolean pubblica);

    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.priorita = :priorita")
    List<AnnotazioneMetadataSQLiteEntity> findByPriorita(@Param("priorita") Integer priorita);

    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.descrizione LIKE %:testo%")
    List<AnnotazioneMetadataSQLiteEntity> findByDescrizioneContaining(@Param("testo") String testo);

    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.tags LIKE %:tag%")
    List<AnnotazioneMetadataSQLiteEntity> findByTagsContaining(@Param("tag") String tag);

    @Query("SELECT COUNT(am) FROM AnnotazioneMetadataSQLiteEntity am WHERE am.utenteCreazione = :utente")
    long countByUtenteCreazione(@Param("utente") String utente);
}
