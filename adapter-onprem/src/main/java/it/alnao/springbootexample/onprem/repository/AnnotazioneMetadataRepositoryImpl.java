package it.alnao.springbootexample.onprem.repository;

import it.alnao.springbootexample.onprem.entity.AnnotazioneMetadataEntity;
import it.alnao.springbootexample.port.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.port.repository.AnnotazioneMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("onprem")
public class AnnotazioneMetadataRepositoryImpl implements AnnotazioneMetadataRepository {

    @Autowired
    private AnnotazioneMetadataJpaRepository jpaRepository;

    @Override
    public AnnotazioneMetadata save(AnnotazioneMetadata metadata) {
        AnnotazioneMetadataEntity entity = toEntity(metadata);
        AnnotazioneMetadataEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AnnotazioneMetadata> findById(UUID id) {
        return jpaRepository.findById(id.toString()).map(entity -> this.toDomain(entity));
    }

    @Override
    public List<AnnotazioneMetadata> findAll() {
        return jpaRepository.findAll().stream()
                .map(entity -> this.toDomain(entity))
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
    public List<AnnotazioneMetadata> findByUtenteCreazione(String utente) {
        return jpaRepository.findByUtenteCreazione(utente)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByDescrizioneContaining(String descrizione) {
        return jpaRepository.findByDescrizioneContainingIgnoreCase(descrizione)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByCategoria(String categoria) {
        return jpaRepository.findByCategoria(categoria)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPubblica(Boolean pubblica) {
        return jpaRepository.findByPubblica(pubblica)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPriorita(Integer priorita) {
        return jpaRepository.findByPriorita(priorita)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByTagsContaining(String tag) {
        return jpaRepository.findByTagsContaining(tag)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByDataInserimentoBetween(LocalDateTime dataInizio, LocalDateTime dataFine) {
        return jpaRepository.findByDataInserimentoBetween(dataInizio, dataFine)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    // Metodo non parte dell'interfaccia port, ma usato internamente
    public List<AnnotazioneMetadata> findWithFilters(String descrizione, String categoria, Boolean pubblica, Integer priorita) {
        return jpaRepository.findWithFilters(descrizione, categoria, pubblica, priorita)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByUtenteCreazione(String utente) {
        return jpaRepository.countByUtenteCreazione(utente);
    }
    


    private AnnotazioneMetadataEntity toEntity(AnnotazioneMetadata domain) {
        AnnotazioneMetadataEntity entity = new AnnotazioneMetadataEntity();
        entity.setId(domain.getId().toString());
        entity.setVersioneNota(domain.getVersioneNota());
        entity.setUtenteCreazione(domain.getUtenteCreazione());
        entity.setDataInserimento(domain.getDataInserimento());
        entity.setDataUltimaModifica(domain.getDataUltimaModifica());
        entity.setUtenteUltimaModifica(domain.getUtenteUltimaModifica());
        entity.setDescrizione(domain.getDescrizione());
        entity.setCategoria(domain.getCategoria());
        entity.setTags(domain.getTags());
        entity.setPubblica(domain.getPubblica());
        entity.setPriorita(domain.getPriorita());
        return entity;
    }

    private AnnotazioneMetadata toDomain(AnnotazioneMetadataEntity entity) {
        AnnotazioneMetadata domain = new AnnotazioneMetadata();
        domain.setId(UUID.fromString(entity.getId()));
        domain.setVersioneNota(entity.getVersioneNota() != null ? entity.getVersioneNota() : "v1.0");
        domain.setUtenteCreazione(entity.getUtenteCreazione());
        domain.setDataInserimento(entity.getDataInserimento());
        domain.setDataUltimaModifica(entity.getDataUltimaModifica());
        domain.setUtenteUltimaModifica(entity.getUtenteUltimaModifica());
        domain.setDescrizione(entity.getDescrizione());
        domain.setCategoria(entity.getCategoria());
        domain.setTags(entity.getTags());
        domain.setPubblica(entity.getPubblica());
        domain.setPriorita(entity.getPriorita());
        return domain;
    }
}

