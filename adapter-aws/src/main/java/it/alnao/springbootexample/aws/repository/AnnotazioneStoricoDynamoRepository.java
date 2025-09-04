package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoDynamoEntity;
import java.util.List;

public interface AnnotazioneStoricoDynamoRepository {
    AnnotazioneStoricoDynamoEntity save(AnnotazioneStoricoDynamoEntity entity);
    List<AnnotazioneStoricoDynamoEntity> findByIdOriginale(String idOriginale);
}
