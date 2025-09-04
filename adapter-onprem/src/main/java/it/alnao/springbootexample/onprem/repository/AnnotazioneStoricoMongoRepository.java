package it.alnao.springbootexample.onprem.repository;

import it.alnao.springbootexample.onprem.entity.AnnotazioneStoricoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotazioneStoricoMongoRepository extends MongoRepository<AnnotazioneStoricoEntity, String> {
    List<AnnotazioneStoricoEntity> findByIdOriginale(String idOriginale);
}
