package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneMetadataMysqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnotazioneMetadataMysqlRepository extends JpaRepository<AnnotazioneMetadataMysqlEntity, String> {
    
    List<AnnotazioneMetadataMysqlEntity> findByDescrizioneContainingIgnoreCase(String descrizione);
    
    List<AnnotazioneMetadataMysqlEntity> findByCategoria(String categoria);
    
    List<AnnotazioneMetadataMysqlEntity> findByPubblica(Boolean pubblica);
    
    List<AnnotazioneMetadataMysqlEntity> findByStato(String stato);
    
    List<AnnotazioneMetadataMysqlEntity> findByPriorita(Integer priorita);
    
    List<AnnotazioneMetadataMysqlEntity> findByUtenteCreazione(String utenteCreazione);
    
    long countByUtenteCreazione(String utenteCreazione);
    
    @Query("SELECT a FROM AnnotazioneMetadataMysqlEntity a WHERE a.tags LIKE %:tag%")
    List<AnnotazioneMetadataMysqlEntity> findByTagsContaining(@Param("tag") String tag);
    
    @Query("SELECT a FROM AnnotazioneMetadataMysqlEntity a WHERE a.dataInserimento BETWEEN :dataInizio AND :dataFine")
    List<AnnotazioneMetadataMysqlEntity> findByDataInserimentoBetween(
        @Param("dataInizio") LocalDateTime dataInizio, 
        @Param("dataFine") LocalDateTime dataFine
    );
    
    @Query("SELECT a FROM AnnotazioneMetadataMysqlEntity a WHERE " +
           "(:descrizione IS NULL OR LOWER(a.descrizione) LIKE LOWER(CONCAT('%', :descrizione, '%'))) AND " +
           "(:categoria IS NULL OR a.categoria = :categoria) AND " +
           "(:pubblica IS NULL OR a.pubblica = :pubblica) AND " +
           "(:priorita IS NULL OR a.priorita = :priorita)")
    List<AnnotazioneMetadataMysqlEntity> findWithFilters(
        @Param("descrizione") String descrizione,
        @Param("categoria") String categoria,
        @Param("pubblica") Boolean pubblica,
        @Param("priorita") Integer priorita
    );
}
