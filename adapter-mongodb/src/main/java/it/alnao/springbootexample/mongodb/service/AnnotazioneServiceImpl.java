package it.alnao.springbootexample.mongodb.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.exception.AnnotationLockedException;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.service.AbstractAnnotazioneService;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import it.alnao.springbootexample.core.utils.AnnotazioniUtils;
import it.alnao.springbootexample.mongodb.entity.AnnotazioneStoricoEntity;
import it.alnao.springbootexample.mongodb.repository.AnnotazioneStoricoMongoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("kube")
@Transactional
public class AnnotazioneServiceImpl extends AbstractAnnotazioneService {

    private static final Logger logger = LoggerFactory.getLogger(AnnotazioneServiceImpl.class);

    private final AnnotazioneRepository annotazioneRepository;
    private final AnnotazioneMetadataRepository metadataRepository;
    private final AnnotazioneStoricoMongoRepository storicoMongoRepository;
    private final AnnotazioneLockService lockService;

    public AnnotazioneServiceImpl(AnnotazioneRepository annotazioneRepository,
                                  AnnotazioneMetadataRepository metadataRepository,
                                  AnnotazioneStoricoMongoRepository storicoMongoRepository,
                                  AnnotazioneLockService lockService) {
        this.annotazioneRepository = annotazioneRepository;
        this.metadataRepository = metadataRepository;
        this.storicoMongoRepository = storicoMongoRepository;
        this.lockService = lockService;
    }

    @Override
    protected AnnotazioneRepository getAnnotazioneRepository() {
        return annotazioneRepository;
    }

    @Override
    protected AnnotazioneMetadataRepository getMetadataRepository() {
        return metadataRepository;
    }

    @Override
    public AnnotazioneCompleta creaAnnotazione(String valoreNota, String descrizione, String utente) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Annotazione annotazione = new Annotazione();
        annotazione.setId(id);
        annotazione.setVersioneNota("v1.0");
        annotazione.setValoreNota(valoreNota);
        Annotazione savedAnnotazione = annotazioneRepository.save(annotazione);
        logger.info("AnnotazioneServiceImpl creaAnnotazione Creata annotazione con ID: {}", savedAnnotazione.getId());

        AnnotazioneMetadata metadata = new AnnotazioneMetadata();
        metadata.setId(id);
        metadata.setVersioneNota("v1.0");
        metadata.setUtenteCreazione(utente);
        metadata.setDataInserimento(now);
        metadata.setDataUltimaModifica(now);
        metadata.setUtenteUltimaModifica(utente);
        metadata.setDescrizione(descrizione);
        metadata.setCategoria("Default");
        metadata.setTags("");
        metadata.setStato(StatoAnnotazione.INSERITA.getValue());
        metadata.setPubblica(false);
        metadata.setPriorita(1);
        AnnotazioneMetadata savedMetadata = metadataRepository.save(metadata);
        logger.info("AnnotazioneServiceImpl creaAnnotazione Creata metadata per annotazione con ID: {}", savedMetadata.getId());

        return new AnnotazioneCompleta(savedAnnotazione, savedMetadata);
    }

    @Override
    public AnnotazioneCompleta aggiornaAnnotazione(UUID id, String nuovoValore, String nuovaDescrizione, String utente) {
        if (utente == null) {
            throw new IllegalArgumentException("Utente non può essere null");
        }
        if (!lockService.acquireLock(id, utente, 30)) {
            Optional<String> owner = lockService.getOwner(id);
            String ownerName = owner.orElse("altro utente");
            if (!utente.equals(ownerName)) {
                logger.warn("Impossibile acquisire lock su annotazione {} per utente {}, già posseduto da {}", id, utente, ownerName);
                throw new AnnotationLockedException(id, ownerName);
            }
            logger.info("Utente {} ha già il lock sull'annotazione {}", utente, id);
        }
        try {
            Optional<Annotazione> existingAnnotazione = annotazioneRepository.findById(id);
            Optional<AnnotazioneMetadata> existingMetadata = metadataRepository.findById(id);

            if (existingAnnotazione.isPresent() && existingMetadata.isPresent()) {
                Annotazione annotazione = existingAnnotazione.get();
                AnnotazioneMetadata metadata = existingMetadata.get();

                AnnotazioneStoricoEntity storico = new AnnotazioneStoricoEntity();
                storico.setIdOriginale(annotazione.getId().toString());
                storico.setVersioneNota(annotazione.getVersioneNota());
                storico.setValoreNota(annotazione.getValoreNota());
                storico.setDescrizione(metadata.getDescrizione());
                storico.setUtente(metadata.getUtenteUltimaModifica());
                storico.setCategoria(metadata.getCategoria());
                storico.setTags(metadata.getTags());
                storico.setPubblica(metadata.getPubblica());
                storico.setPriorita(metadata.getPriorita());
                storico.setDataModifica(metadata.getDataUltimaModifica());
                storicoMongoRepository.save(storico);

                annotazione.setValoreNota(nuovoValore);
                annotazione.setVersioneNota(AnnotazioniUtils.incrementaVersione(annotazione.getVersioneNota()));
                Annotazione updatedAnnotazione = annotazioneRepository.save(annotazione);

                metadata.setDataUltimaModifica(LocalDateTime.now());
                metadata.setUtenteUltimaModifica(utente);
                metadata.setDescrizione(nuovaDescrizione);
                metadata.setVersioneNota(annotazione.getVersioneNota());
                AnnotazioneMetadata updatedMetadata = metadataRepository.save(metadata);

                logger.info("Annotazione {} aggiornata con successo da utente {}", id, utente);
                return new AnnotazioneCompleta(updatedAnnotazione, updatedMetadata);
            }
            throw new RuntimeException("Annotazione non trovata con ID: " + id);
        } finally {
            lockService.releaseLock(id, utente);
        }
    }
}
