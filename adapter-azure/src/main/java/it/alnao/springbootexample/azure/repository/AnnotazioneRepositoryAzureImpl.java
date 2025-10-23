package it.alnao.springbootexample.azure.repository;

import it.alnao.springbootexample.azure.entity.AnnotazioneCosmosEntity;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.ArrayList;

@Repository
@Primary
@Profile("azure")
public class AnnotazioneRepositoryAzureImpl implements AnnotazioneRepository {
    @Autowired
    private AnnotazioneCosmosRepository cosmosRepository;

    @Override
    public Annotazione save(Annotazione annotazione) {
        AnnotazioneCosmosEntity entity = toEntity(annotazione);
        AnnotazioneCosmosEntity saved = cosmosRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    public Optional<Annotazione> findById(UUID id) {
        return cosmosRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public List<Annotazione> findAll() {
        List<AnnotazioneCosmosEntity> entities = new ArrayList<>();
        cosmosRepository.findAll().forEach(entities::add);
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        cosmosRepository.deleteById(id.toString());
    }

    @Override
    public boolean existsById(UUID id) {
        return cosmosRepository.existsById(id.toString());
    }

    @Override
    public List<Annotazione> findByValoreNotaContaining(String valoreNota) {
        return cosmosRepository.findByValoreNotaContainingIgnoreCase(valoreNota)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Annotazione> findByVersioneNota(String versioneNota) {
        List<AnnotazioneCosmosEntity> entities = new ArrayList<>();
        cosmosRepository.findAll().forEach(entities::add);
        return entities.stream()
                .filter(entity -> versioneNota.equals(entity.getVersioneNota()))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return cosmosRepository.count();
    }

    private AnnotazioneCosmosEntity toEntity(Annotazione domain) {
        return new AnnotazioneCosmosEntity(
            domain.getId().toString(),
            domain.getVersioneNota(),
            domain.getValoreNota()
        );
    }

    private Annotazione toDomain(AnnotazioneCosmosEntity entity) {
        Annotazione domain = new Annotazione();
        domain.setId(UUID.fromString(entity.getId()));
        domain.setVersioneNota(entity.getVersioneNota());
        domain.setValoreNota(entity.getValoreNota());
        return domain;
    }
}
