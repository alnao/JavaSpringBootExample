package it.alnao.springbootexample.sqlite.service;

import it.alnao.springbootexample.core.service.AnnotazioneService;
import it.alnao.springbootexample.core.utils.AnnotazioniUtils;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneSQLiteRepository;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneMetadataSQLiteRepository;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneSQLiteEntity;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("sqlite")
@Service
public class AnnotazioneServiceSQLiteImpl implements AnnotazioneService {
    @Override
    public it.alnao.springbootexample.core.domain.AnnotazioneCompleta cambiaStato(java.util.UUID id, String nuovoStato, String utenteModifica) {
        var metadataOpt = metadataRepository.findById(id.toString());
        var annotazioneOpt = annotazioneRepository.findById(id.toString());
        if (metadataOpt.isPresent() && annotazioneOpt.isPresent()) {
            var metadataEntity = metadataOpt.get();
            var metadata = metadataEntity.toDomain();
            boolean statoValido = false;
            for (it.alnao.springbootexample.core.domain.StatoAnnotazione statoEnum : it.alnao.springbootexample.core.domain.StatoAnnotazione.values()) {
                if (statoEnum.getValue().equals(nuovoStato)) {
                    statoValido = true;
                    break;
                }
            }
            if (!statoValido) {
                throw new IllegalArgumentException("Stato annotazione non valido: " + nuovoStato);
            }
            metadata.setStato(nuovoStato);
            metadata.setUtenteUltimaModifica(utenteModifica);
            metadata.setDataUltimaModifica(java.time.LocalDateTime.now());
            metadataRepository.save(new it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity(metadata));
            var annotazioneEntity = annotazioneOpt.get();
            return new it.alnao.springbootexample.core.domain.AnnotazioneCompleta(
                new it.alnao.springbootexample.core.domain.Annotazione(
                    java.util.UUID.fromString(annotazioneEntity.getId()),
                    annotazioneEntity.getVersioneNota(),
                    annotazioneEntity.getValoreNota()
                ),
                metadata
            );
        }
        throw new RuntimeException("Annotazione non trovata con ID: " + id);
    }
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AnnotazioneServiceSQLiteImpl.class);

    @Autowired
    private AnnotazioneSQLiteRepository annotazioneRepository;

    @Autowired
    private AnnotazioneMetadataSQLiteRepository metadataRepository;

    // Aggiornamento completo di annotazione e metadata
    public it.alnao.springbootexample.core.domain.AnnotazioneCompleta aggiornaAnnotazioneCompleta(
            java.util.UUID id, String nuovoValore, String nuovaDescrizione, String utente,
            String categoria, String tags, Boolean pubblica, Integer priorita) {
        logger.info("Richiesta aggiornamento completo annotazione con ID: {}", id);
        var opt = annotazioneRepository.findById(id.toString());
        if (opt.isEmpty()) {
            logger.warn("Annotazione con ID {} non trovata, nessun aggiornamento eseguito.", id);
            return null;
        }
        var annotazioneEntity = opt.get();
        annotazioneEntity.setValoreNota(nuovoValore);
        annotazioneEntity.setDataUltimaModifica(java.time.LocalDateTime.now());
        annotazioneEntity = annotazioneRepository.save(annotazioneEntity);
        var metadataOpt = metadataRepository.findById(id.toString());
        it.alnao.springbootexample.core.domain.AnnotazioneMetadata metadata;
        if (metadataOpt.isPresent()) {
            var metadataEntity = metadataOpt.get();
            metadata = metadataEntity.toDomain();
            metadata.setDescrizione(nuovaDescrizione);
            metadata.setCategoria(categoria);
            metadata.setTags(tags);
            metadata.setPubblica(pubblica);
            metadata.setPriorita(priorita);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(java.time.LocalDateTime.now());
            metadataRepository.save(new AnnotazioneMetadataSQLiteEntity(metadata));
            logger.info("Annotazione {} aggiornata con successo.", id);
        } else {
            logger.warn("Metadati per annotazione {} non trovati, nessun aggiornamento eseguito.", id);
            return null;
        }
        return new it.alnao.springbootexample.core.domain.AnnotazioneCompleta(
            new it.alnao.springbootexample.core.domain.Annotazione(
                java.util.UUID.fromString(annotazioneEntity.getId()),
                annotazioneEntity.getVersioneNota(),
                annotazioneEntity.getValoreNota()
            ),
            metadata
        );
    }

    // Storage in memoria per test locale
    @Override
    public it.alnao.springbootexample.core.domain.AnnotazioneCompleta creaAnnotazione(String valoreNota, String descrizione, String utente) {
        java.util.UUID id=java.util.UUID.randomUUID();
        var annotazioneEntity = new AnnotazioneSQLiteEntity(id, valoreNota, "1");
        logger.warn("creaAnnotazione annotazioneEntity con ID {} ", annotazioneEntity.getId());
        annotazioneEntity = annotazioneRepository.save(annotazioneEntity);
        var metadata = new it.alnao.springbootexample.core.domain.AnnotazioneMetadata(
            id, annotazioneEntity.getVersioneNota(), utente, descrizione
        );
        metadata.setStato(it.alnao.springbootexample.core.domain.StatoAnnotazione.INSERITA.getValue());
        logger.warn("creaAnnotazione metadata con ID {} ", metadata.getId());
        var metadataEntity = new AnnotazioneMetadataSQLiteEntity(metadata);
        logger.warn("creaAnnotazione metadataEntity con ID {} ", metadata.getId());
        metadataRepository.save(metadataEntity);
        return new it.alnao.springbootexample.core.domain.AnnotazioneCompleta(
            new it.alnao.springbootexample.core.domain.Annotazione(
                id,
                annotazioneEntity.getVersioneNota(),
                annotazioneEntity.getValoreNota()
            ),
            metadata
        );
    }

    @Override
    public it.alnao.springbootexample.core.domain.AnnotazioneCompleta aggiornaAnnotazione(java.util.UUID id, String nuovoValore, String nuovaDescrizione, String utente) {
        logger.info("Richiesta aggiornamento annotazione con ID: {}", id);
        var opt = annotazioneRepository.findById(id.toString());
        if (opt.isEmpty()) {
            logger.warn("Annotazione con ID {} non trovata, nessun aggiornamento eseguito.", id);
            return null;
        }
        var annotazioneEntity = opt.get();
        annotazioneEntity.setValoreNota(nuovoValore);
        annotazioneEntity.setDataUltimaModifica(java.time.LocalDateTime.now());
        annotazioneEntity.setVersioneNota( AnnotazioniUtils.incrementaVersione(annotazioneEntity.getVersioneNota()) );
        annotazioneEntity = annotazioneRepository.save(annotazioneEntity); 
        
        var metadataOpt = metadataRepository.findById(id.toString());
        it.alnao.springbootexample.core.domain.AnnotazioneMetadata metadata;
        if (metadataOpt.isPresent()) {
            var metadataEntity = metadataOpt.get();
            metadata = metadataEntity.toDomain();
            metadata.setDescrizione(nuovaDescrizione);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(java.time.LocalDateTime.now());
            // esempio: aggiorna stato in base a logica
            if (metadata.getStato() == null || metadata.getStato().isEmpty() || metadata.getStato().equals(it.alnao.springbootexample.core.domain.StatoAnnotazione.INSERITA.getValue())) {
                metadata.setStato(it.alnao.springbootexample.core.domain.StatoAnnotazione.MODIFICATA.getValue());
            }
            metadataRepository.save(new AnnotazioneMetadataSQLiteEntity(metadata));
            logger.info("Annotazione {} aggiornata con successo.", id);
        } else {
            logger.warn("Metadati per annotazione {} non trovati, nessun aggiornamento eseguito.", id);
            return null;
        }
        return new it.alnao.springbootexample.core.domain.AnnotazioneCompleta(
            new it.alnao.springbootexample.core.domain.Annotazione(
                java.util.UUID.fromString(annotazioneEntity.getId()),
                annotazioneEntity.getVersioneNota(),
                annotazioneEntity.getValoreNota()
            ),
            metadata
        );
    }

    @Override
    public java.util.Optional<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> trovaPerID(java.util.UUID id) {
        var opt = annotazioneRepository.findById(id.toString());
        if (opt.isEmpty()) return java.util.Optional.empty();
        var annotazioneEntity = opt.get();
        var metadataOpt = metadataRepository.findById(id.toString());
        it.alnao.springbootexample.core.domain.AnnotazioneMetadata metadata = metadataOpt.map(AnnotazioneMetadataSQLiteEntity::toDomain).orElse(null);
        return java.util.Optional.of(new it.alnao.springbootexample.core.domain.AnnotazioneCompleta(
            new it.alnao.springbootexample.core.domain.Annotazione(
                java.util.UUID.fromString(annotazioneEntity.getId()),
                annotazioneEntity.getVersioneNota(),
                annotazioneEntity.getValoreNota()
            ),
            metadata
        ));
    }

    @Override
    public java.util.List<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> trovaTutte() {
        var list = new java.util.ArrayList<it.alnao.springbootexample.core.domain.AnnotazioneCompleta>();
        for (var annotazioneEntity : annotazioneRepository.findAll()) {
            var metadataOpt = metadataRepository.findById(annotazioneEntity.getId());
            it.alnao.springbootexample.core.domain.AnnotazioneMetadata metadata = metadataOpt.map(AnnotazioneMetadataSQLiteEntity::toDomain).orElse(null);
            list.add(new it.alnao.springbootexample.core.domain.AnnotazioneCompleta(
                new it.alnao.springbootexample.core.domain.Annotazione(
                    java.util.UUID.fromString(annotazioneEntity.getId()),
                    annotazioneEntity.getVersioneNota(),
                    annotazioneEntity.getValoreNota()
                ),
                metadata
            ));
        }
        return list;
    }

    @Override
    public java.util.List<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> trovaPerUtente(String utente) {
        return trovaTutte().stream()
            .filter(a -> utente != null && utente.equals(a.getMetadata().getUtenteCreazione()))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> trovaPerCategoria(String categoria) {
        return trovaTutte().stream()
            .filter(a -> categoria != null && categoria.equals(a.getMetadata().getCategoria()))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> trovaPerPeriodo(java.time.LocalDateTime inizio, java.time.LocalDateTime fine) {
        return trovaTutte().stream()
            .filter(a -> {
                var data = a.getMetadata().getDataInserimento();
                return data != null && !data.isBefore(inizio) && !data.isAfter(fine);
            })
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> cercaPerTesto(String testo) {
        return trovaTutte().stream()
            .filter(a -> (a.getAnnotazione().getValoreNota() != null && a.getAnnotazione().getValoreNota().contains(testo))
                || (a.getMetadata().getDescrizione() != null && a.getMetadata().getDescrizione().contains(testo)))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> trovaPubbliche() {
        return trovaTutte().stream()
            .filter(a -> Boolean.TRUE.equals(a.getMetadata().getPubblica()))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<it.alnao.springbootexample.core.domain.AnnotazioneCompleta> trovaPerStato(it.alnao.springbootexample.core.domain.StatoAnnotazione stato) {
        return trovaTutte().stream()
            .filter(a -> stato.equals(a.getMetadata().getStato()))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void eliminaAnnotazione(java.util.UUID id) {
        annotazioneRepository.deleteById(id.toString());
        metadataRepository.deleteById(id.toString());
    }

    @Override
    public boolean esisteAnnotazione(java.util.UUID id) {
    return annotazioneRepository.existsById(id.toString());
    }

    @Override
    public long contaAnnotazioni() {
        return annotazioneRepository.count();
    }

    @Override
    public long contaAnnotazioniPerUtente(String utente) {
        return trovaPerUtente(utente).size();
    }

    @Override
    public void impostaVisibilitaPubblica(java.util.UUID id, boolean pubblica, String utente) {
        var metadataOpt = metadataRepository.findById(id.toString());
        if (metadataOpt.isPresent()) {
            var metadataEntity = metadataOpt.get();
            var metadata = metadataEntity.toDomain();
            metadata.setPubblica(pubblica);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(java.time.LocalDateTime.now());
            metadataRepository.save(new AnnotazioneMetadataSQLiteEntity(metadata));
        }else {
            logger.warn("impostaVisibilitaPubblica per annotazione {} non trovati, nessun aggiornamento eseguito.", id);
        }
    }

    @Override
    public void impostaCategoria(java.util.UUID id, String categoria, String utente) {
        var metadataOpt = metadataRepository.findById(id.toString());
        if (metadataOpt.isPresent()) {
            var metadataEntity = metadataOpt.get();
            var metadata = metadataEntity.toDomain();
            metadata.setCategoria(categoria);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(java.time.LocalDateTime.now());
            metadataRepository.save(new AnnotazioneMetadataSQLiteEntity(metadata));
        }else {
            logger.warn("impostaCategoria per annotazione {} non trovati, nessun aggiornamento eseguito.", id);
        }
    }

    @Override
    public void impostaTags(java.util.UUID id, String tags, String utente) {
        var metadataOpt = metadataRepository.findById(id.toString());
        if (metadataOpt.isPresent()) {
            var metadataEntity = metadataOpt.get();
            var metadata = metadataEntity.toDomain();
            metadata.setTags(tags);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(java.time.LocalDateTime.now());
            metadataRepository.save(new AnnotazioneMetadataSQLiteEntity(metadata));
        }else {
            logger.warn("impostaTags per annotazione {} non trovati, nessun aggiornamento eseguito.", id);
        }
    }

    @Override
    public void impostaPriorita(java.util.UUID id, Integer priorita, String utente) {
        var metadataOpt = metadataRepository.findById(id.toString());
        if (metadataOpt.isPresent()) {
            var metadataEntity = metadataOpt.get();
            var metadata = metadataEntity.toDomain();
            metadata.setPriorita(priorita);
            metadata.setUtenteUltimaModifica(utente);
            metadata.setDataUltimaModifica(java.time.LocalDateTime.now());
            metadataRepository.save(new AnnotazioneMetadataSQLiteEntity(metadata));
        }else {
            logger.warn("impostaPriorita per annotazione {} non trovati, nessun aggiornamento eseguito.", id);
        }
    }
}
