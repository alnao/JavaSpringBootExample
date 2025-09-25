package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneSQLiteEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;

@Profile("sqlite")
public class AnnotazioneRepositoryImpl implements AnnotazioneRepository {
    private final AnnotazioneSQLiteJpaRepository jpaRepository;

    public AnnotazioneRepositoryImpl(AnnotazioneSQLiteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Annotazione save(Annotazione annotazione) {
        AnnotazioneSQLiteEntity entity = new AnnotazioneSQLiteEntity(annotazione);
        AnnotazioneSQLiteEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Annotazione> findById(UUID id) {
        return jpaRepository.findById(id.toString())
                .map(AnnotazioneSQLiteEntity::toDomain);
    }

    @Override
    public List<Annotazione> findAll() {
        return jpaRepository.findAll().stream()
                .map(AnnotazioneSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Annotazione> findByVersioneNota(String versione) {
        return jpaRepository.findByVersioneNota(versione).stream()
                .map(AnnotazioneSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Annotazione> findByValoreNotaContaining(String testo) {
        return jpaRepository.findByValoreNotaContaining(testo).stream()
                .map(AnnotazioneSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id.toString());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id.toString());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
