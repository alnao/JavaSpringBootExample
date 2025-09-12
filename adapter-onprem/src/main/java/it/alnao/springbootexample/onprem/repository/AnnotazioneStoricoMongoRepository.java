package it.alnao.springbootexample.onprem.repository;

import it.alnao.springbootexample.onprem.entity.AnnotazioneStoricoEntity;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("onprem")
public interface AnnotazioneStoricoMongoRepository extends MongoRepository<AnnotazioneStoricoEntity, String> {
    List<AnnotazioneStoricoEntity> findByIdOriginale(String idOriginale);
}
