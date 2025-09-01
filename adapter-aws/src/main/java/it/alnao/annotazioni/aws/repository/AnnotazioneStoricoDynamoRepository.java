package it.alnao.annotazioni.aws.repository;

import it.alnao.annotazioni.aws.entity.AnnotazioneStoricoDynamoEntity;
import java.util.List;

public interface AnnotazioneStoricoDynamoRepository {
    AnnotazioneStoricoDynamoEntity save(AnnotazioneStoricoDynamoEntity entity);
    List<AnnotazioneStoricoDynamoEntity> findByIdOriginale(String idOriginale);
}
