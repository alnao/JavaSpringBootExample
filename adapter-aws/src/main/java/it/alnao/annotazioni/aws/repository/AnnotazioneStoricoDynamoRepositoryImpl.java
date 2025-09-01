package it.alnao.annotazioni.aws.repository;

import it.alnao.annotazioni.aws.entity.AnnotazioneStoricoDynamoEntity;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AnnotazioneStoricoDynamoRepositoryImpl implements AnnotazioneStoricoDynamoRepository {
    private final Map<String, List<AnnotazioneStoricoDynamoEntity>> storage = new HashMap<>();

    @Override
    public AnnotazioneStoricoDynamoEntity save(AnnotazioneStoricoDynamoEntity entity) {
        storage.computeIfAbsent(entity.getIdOriginale(), k -> new ArrayList<>()).add(entity);
        return entity;
    }

    @Override
    public List<AnnotazioneStoricoDynamoEntity> findByIdOriginale(String idOriginale) {
        return storage.getOrDefault(idOriginale, Collections.emptyList());
    }
}
