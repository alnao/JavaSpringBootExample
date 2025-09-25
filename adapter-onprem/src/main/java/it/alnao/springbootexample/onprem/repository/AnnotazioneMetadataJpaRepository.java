package it.alnao.springbootexample.onprem.repository;

import it.alnao.springbootexample.onprem.entity.AnnotazioneMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnotazioneMetadataJpaRepository extends JpaRepository<AnnotazioneMetadataEntity, String> {
    
    List<AnnotazioneMetadataEntity> findByDescrizioneContainingIgnoreCase(String descrizione);
    
    List<AnnotazioneMetadataEntity> findByCategoria(String categoria);
    
    List<AnnotazioneMetadataEntity> findByPubblica(Boolean pubblica);
    
    List<AnnotazioneMetadataEntity> findByStato(String stato);
    
    List<AnnotazioneMetadataEntity> findByPriorita(Integer priorita);
    
    List<AnnotazioneMetadataEntity> findByUtenteCreazione(String utenteCreazione);
    
    long countByUtenteCreazione(String utenteCreazione);
    
    @Query("SELECT a FROM AnnotazioneMetadataEntity a WHERE a.tags LIKE %:tag%")
    List<AnnotazioneMetadataEntity> findByTagsContaining(@Param("tag") String tag);
    
    @Query("SELECT a FROM AnnotazioneMetadataEntity a WHERE a.dataInserimento BETWEEN :dataInizio AND :dataFine")
    List<AnnotazioneMetadataEntity> findByDataInserimentoBetween(
        @Param("dataInizio") LocalDateTime dataInizio, 
        @Param("dataFine") LocalDateTime dataFine
    );
    
    @Query("SELECT a FROM AnnotazioneMetadataEntity a WHERE " +
           "(:descrizione IS NULL OR LOWER(a.descrizione) LIKE LOWER(CONCAT('%', :descrizione, '%'))) AND " +
           "(:categoria IS NULL OR a.categoria = :categoria) AND " +
           "(:pubblica IS NULL OR a.pubblica = :pubblica) AND " +
           "(:priorita IS NULL OR a.priorita = :priorita)")
    List<AnnotazioneMetadataEntity> findWithFilters(
        @Param("descrizione") String descrizione,
        @Param("categoria") String categoria,
        @Param("pubblica") Boolean pubblica,
        @Param("priorita") Integer priorita
    );
}
