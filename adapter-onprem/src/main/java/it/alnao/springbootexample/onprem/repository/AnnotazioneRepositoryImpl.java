package it.alnao.springbootexample.onprem.repository;

import it.alnao.springbootexample.onprem.entity.AnnotazioneEntity;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("onprem")
public class AnnotazioneRepositoryImpl implements AnnotazioneRepository {

    @Autowired
    private AnnotazioneMongoRepository mongoRepository;

    @Override
    public Annotazione save(Annotazione annotazione) {
        AnnotazioneEntity entity = toEntity(annotazione);
        AnnotazioneEntity saved = mongoRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Annotazione> findById(UUID id) {
        return mongoRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public List<Annotazione> findAll() {
        return mongoRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Annotazione> findByVersioneNota(String versione) {
        return mongoRepository.findByVersioneNota(versione)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Annotazione> findByValoreNotaContaining(String testo) {
        return mongoRepository.findByValoreNotaContainingIgnoreCase(testo)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        mongoRepository.deleteById(id.toString());
    }

    @Override
    public boolean existsById(UUID id) {
        return mongoRepository.existsById(id.toString());
    }

    @Override
    public long count() {
        return mongoRepository.count();
    }

    private AnnotazioneEntity toEntity(Annotazione domain) {
        AnnotazioneEntity entity = new AnnotazioneEntity();
        entity.setId(domain.getId().toString());
        entity.setVersioneNota(domain.getVersioneNota());
        entity.setValoreNota(domain.getValoreNota());
        return entity;
    }

    private Annotazione toDomain(AnnotazioneEntity entity) {
        Annotazione domain = new Annotazione();
        domain.setId(UUID.fromString(entity.getId()));
        domain.setVersioneNota(entity.getVersioneNota());
        domain.setValoreNota(entity.getValoreNota());
        return domain;
    }
}
