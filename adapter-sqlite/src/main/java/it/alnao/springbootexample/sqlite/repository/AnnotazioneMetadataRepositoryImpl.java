package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("sqlite")
public class AnnotazioneMetadataRepositoryImpl implements AnnotazioneMetadataRepository {

    @Autowired
    private AnnotazioneMetadataSQLiteJpaRepository jpaRepository;

    @Override
    public List<AnnotazioneMetadata> findByStato(StatoAnnotazione stato) {
        return jpaRepository.findByStato(stato.name()).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByDataInserimentoBetween(LocalDateTime inizio, LocalDateTime fine) {
        return jpaRepository.findByDataInserimentoBetween(inizio, fine).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByUtenteCreazione(String utente) {
        return jpaRepository.findByUtenteCreazione(utente).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByCategoria(String categoria) {
        return jpaRepository.findByCategoria(categoria).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPubblica(Boolean pubblica) {
        return jpaRepository.findByPubblica(pubblica).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPriorita(Integer priorita) {
        return jpaRepository.findByPriorita(priorita).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByDescrizioneContaining(String testo) {
        return jpaRepository.findByDescrizioneContaining(testo).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByTagsContaining(String tag) {
        return jpaRepository.findByTagsContaining(tag).stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUtenteCreazione(String utente) {
        return jpaRepository.countByUtenteCreazione(utente);
    }

    @Override
    public AnnotazioneMetadata save(AnnotazioneMetadata entity) {
        AnnotazioneMetadataSQLiteEntity sqliteEntity = new AnnotazioneMetadataSQLiteEntity(entity);
        AnnotazioneMetadataSQLiteEntity savedEntity = jpaRepository.save(sqliteEntity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<AnnotazioneMetadata> findById(UUID id) {
        return jpaRepository.findById(id.toString())
                .map(AnnotazioneMetadataSQLiteEntity::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id.toString());
    }

    @Override
    public List<AnnotazioneMetadata> findAll() {
        return jpaRepository.findAll().stream()
                .map(AnnotazioneMetadataSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id.toString());
    }
}
