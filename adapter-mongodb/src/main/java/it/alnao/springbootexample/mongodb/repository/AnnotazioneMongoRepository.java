package it.alnao.springbootexample.mongodb.repository;

import it.alnao.springbootexample.mongodb.entity.AnnotazioneEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotazioneMongoRepository extends MongoRepository<AnnotazioneEntity, String> {
    
    @Query("{'valoreNota': {$regex: ?0, $options: 'i'}}")
    List<AnnotazioneEntity> findByValoreNotaContainingIgnoreCase(String valoreNota);
    
    List<AnnotazioneEntity> findByVersioneNota(String versioneNota);
    
    @Query("{'valoreNota': {$regex: ?0, $options: 'i'}, 'versioneNota': ?1}")
    List<AnnotazioneEntity> findByValoreNotaContainingIgnoreCaseAndVersioneNota(String valoreNota, String versioneNota);
}
