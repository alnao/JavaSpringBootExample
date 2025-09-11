package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneMetadataMysqlEntity;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("aws")
public class AnnotazioneMetadataRepositoryAwsImpl implements AnnotazioneMetadataRepository {

    @Autowired
    private AnnotazioneMetadataMysqlRepository mysqlRepository;

    @Override
    public AnnotazioneMetadata save(AnnotazioneMetadata metadata) {
        AnnotazioneMetadataMysqlEntity entity = toEntity(metadata);
        AnnotazioneMetadataMysqlEntity saved = mysqlRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AnnotazioneMetadata> findById(UUID id) {
        return mysqlRepository.findById(id.toString()).map(entity -> this.toDomain(entity));
    }

    @Override
    public List<AnnotazioneMetadata> findAll() {
        return mysqlRepository.findAll().stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        mysqlRepository.deleteById(id.toString());
    }

    @Override
    public boolean existsById(UUID id) {
        return mysqlRepository.existsById(id.toString());
    }

    @Override
    public List<AnnotazioneMetadata> findByDescrizioneContaining(String descrizione) {
        return mysqlRepository.findByDescrizioneContainingIgnoreCase(descrizione)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByCategoria(String categoria) {
        return mysqlRepository.findByCategoria(categoria)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPubblica(Boolean pubblica) {
        return mysqlRepository.findByPubblica(pubblica)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPriorita(Integer priorita) {
        return mysqlRepository.findByPriorita(priorita)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByUtenteCreazione(String utente) {
        return mysqlRepository.findByUtenteCreazione(utente)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByTagsContaining(String tag) {
        return mysqlRepository.findByTagsContaining(tag)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByDataInserimentoBetween(LocalDateTime dataInizio, LocalDateTime dataFine) {
        return mysqlRepository.findByDataInserimentoBetween(dataInizio, dataFine)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    public List<AnnotazioneMetadata> findWithFilters(String descrizione, String categoria, Boolean pubblica, Integer priorita) {
        return mysqlRepository.findWithFilters(descrizione, categoria, pubblica, priorita)
                .stream()
                .map(entity -> this.toDomain(entity))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return mysqlRepository.count();
    }

    @Override
    public long countByUtenteCreazione(String utente) {
        return mysqlRepository.countByUtenteCreazione(utente);
    }

    private AnnotazioneMetadataMysqlEntity toEntity(AnnotazioneMetadata domain) {
        AnnotazioneMetadataMysqlEntity entity = new AnnotazioneMetadataMysqlEntity();
        entity.setId(domain.getId().toString());
        entity.setVersioneNota(domain.getVersioneNota() != null ? domain.getVersioneNota() : "1.0");
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

    private AnnotazioneMetadata toDomain(AnnotazioneMetadataMysqlEntity entity) {
        AnnotazioneMetadata domain = new AnnotazioneMetadata();
        domain.setId(UUID.fromString(entity.getId()));
        domain.setVersioneNota(entity.getVersioneNota() != null ? entity.getVersioneNota() : "1.0");
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
