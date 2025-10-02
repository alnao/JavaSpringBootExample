package it.alnao.springbootexample.azure.repository;

import it.alnao.springbootexample.azure.entity.AnnotazioneMetadataSqlServerEntity;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
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
@Profile("azure")
public class AnnotazioneMetadataRepositoryAzureImpl implements AnnotazioneMetadataRepository {
    @Autowired
    private AnnotazioneMetadataSqlServerRepository sqlRepository;

    @Override
    public AnnotazioneMetadata save(AnnotazioneMetadata metadata) {
        AnnotazioneMetadataSqlServerEntity entity = toEntity(metadata);
        AnnotazioneMetadataSqlServerEntity saved = sqlRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AnnotazioneMetadata> findById(UUID id) {
        return sqlRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public List<AnnotazioneMetadata> findAll() {
        return sqlRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id.toString());
    }

    @Override
    public boolean existsById(UUID id) {
        return sqlRepository.existsById(id.toString());
    }

    @Override
    public List<AnnotazioneMetadata> findByDescrizioneContaining(String descrizione) {
        return sqlRepository.findByDescrizioneContainingIgnoreCase(descrizione)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByCategoria(String categoria) {
        return sqlRepository.findByCategoria(categoria)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPubblica(Boolean pubblica) {
        return sqlRepository.findByPubblica(pubblica)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByStato(StatoAnnotazione stato) {
        return sqlRepository.findByStato(stato.name())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByPriorita(Integer priorita) {
        return sqlRepository.findByPriorita(priorita)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByUtenteCreazione(String utente) {
        return sqlRepository.findByUtenteCreazione(utente)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnotazioneMetadata> findByTagsContaining(String tag) {
        // Da implementare se serve, oppure usare una query custom
        return List.of();
    }

    @Override
    public List<AnnotazioneMetadata> findByDataInserimentoBetween(LocalDateTime dataInizio, LocalDateTime dataFine) {
        return sqlRepository.findByDataInserimentoBetween(dataInizio, dataFine)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return sqlRepository.count();
    }

    @Override
    public long countByUtenteCreazione(String utente) {
        return sqlRepository.countByUtenteCreazione(utente);
    }

    private AnnotazioneMetadataSqlServerEntity toEntity(AnnotazioneMetadata domain) {
        AnnotazioneMetadataSqlServerEntity entity = new AnnotazioneMetadataSqlServerEntity();
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
        entity.setStato(domain.getStato());
        return entity;
    }

    private AnnotazioneMetadata toDomain(AnnotazioneMetadataSqlServerEntity entity) {
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
        domain.setStato(entity.getStato());
        return domain;
    }
}
