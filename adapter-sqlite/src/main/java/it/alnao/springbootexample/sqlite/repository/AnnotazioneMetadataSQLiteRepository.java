package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.port.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.port.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per AnnotazioneMetadata su SQLite.
 */
@Repository
@Profile("sqlite")
public interface AnnotazioneMetadataSQLiteRepository extends JpaRepository<AnnotazioneMetadataSQLiteEntity, String> {
    
    // Query personalizzate solo con entità
    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.id = :id")
    List<AnnotazioneMetadataSQLiteEntity> findByIdValue(@Param("id") String id);


    @Query("SELECT am FROM AnnotazioneMetadataSQLiteEntity am WHERE am.dataInserimento >= :dataInizio AND am.dataInserimento <= :dataFine")
    List<AnnotazioneMetadataSQLiteEntity> findByDataInserimentoBetween(@Param("dataInizio") java.time.LocalDateTime dataInizio, @Param("dataFine") java.time.LocalDateTime dataFine);

    @Query("SELECT COUNT(am) FROM AnnotazioneMetadataSQLiteEntity am WHERE am.id = :id")
    long countByIdValue(@Param("id") String id);

}
