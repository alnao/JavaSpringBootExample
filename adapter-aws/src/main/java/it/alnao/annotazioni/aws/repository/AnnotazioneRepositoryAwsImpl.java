package it.alnao.annotazioni.aws.repository;

import it.alnao.annotazioni.aws.entity.AnnotazioneDynamoEntity;
import it.alnao.annotazioni.port.domain.Annotazione;
import it.alnao.annotazioni.port.repository.AnnotazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("aws")
public class AnnotazioneRepositoryAwsImpl implements AnnotazioneRepository {

    @Autowired
    private AnnotazioneDynamoRepository dynamoRepository;

    @Override
    public Annotazione save(Annotazione annotazione) {
        AnnotazioneDynamoEntity entity = toEntity(annotazione);
        AnnotazioneDynamoEntity saved = dynamoRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Annotazione> findById(UUID id) {
        return dynamoRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public List<Annotazione> findAll() {
        return dynamoRepository.findAll().stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        dynamoRepository.deleteById(id.toString());
    }

    @Override
    public boolean existsById(UUID id) {
        return dynamoRepository.existsById(id.toString());
    }

    @Override
    public List<Annotazione> findByValoreNotaContaining(String valoreNota) {
        return dynamoRepository.findByValoreNotaContaining(valoreNota)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<Annotazione> findByVersioneNota(String versioneNota) {
        return dynamoRepository.findByVersioneNota(versioneNota)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return dynamoRepository.count();
    }

    private AnnotazioneDynamoEntity toEntity(Annotazione domain) {
        return new AnnotazioneDynamoEntity(
            domain.getId().toString(),
            domain.getVersioneNota(),
            domain.getValoreNota()
        );
    }

    private Annotazione toDomain(AnnotazioneDynamoEntity entity) {
        Annotazione domain = new Annotazione();
        domain.setId(UUID.fromString(entity.getId()));
        domain.setVersioneNota(entity.getVersioneNota());
        domain.setValoreNota(entity.getValoreNota());
        return domain;
    }
}
