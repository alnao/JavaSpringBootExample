package it.alnao.springbootexample.mongodb.repository;

import it.alnao.springbootexample.mongodb.entity.AnnotazioneStoricoEntity;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("kube")
public interface AnnotazioneStoricoMongoRepository extends MongoRepository<AnnotazioneStoricoEntity, String> {
    List<AnnotazioneStoricoEntity> findByIdOriginale(String idOriginale);
}
