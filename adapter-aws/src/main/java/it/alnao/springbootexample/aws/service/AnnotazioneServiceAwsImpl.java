package it.alnao.springbootexample.aws.service;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoDynamoEntity;
import it.alnao.springbootexample.aws.repository.AnnotazioneStoricoDynamoRepository;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.service.AbstractAnnotazioneService;
import it.alnao.springbootexample.core.utils.AnnotazioniUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("aws")
public class AnnotazioneServiceAwsImpl extends AbstractAnnotazioneService {

    @Autowired
    private AnnotazioneRepository annotazioneRepository;

    @Autowired
    private AnnotazioneMetadataRepository metadataRepository;

    @Autowired
    private AnnotazioneStoricoDynamoRepository storicoDynamoRepository;

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
        annotazione.setVersioneNota("1.0");
        annotazione.setValoreNota(valoreNota);
        Annotazione savedAnnotazione = annotazioneRepository.save(annotazione);

        AnnotazioneMetadata metadata = new AnnotazioneMetadata();
        metadata.setId(id);
        metadata.setVersioneNota("1.0");
        metadata.setUtenteCreazione(utente);
        metadata.setDataInserimento(now);
        metadata.setDataUltimaModifica(now);
        metadata.setUtenteUltimaModifica(utente);
        metadata.setDescrizione(descrizione);
        metadata.setStato(StatoAnnotazione.INSERITA.getValue());
        AnnotazioneMetadata savedMetadata = metadataRepository.save(metadata);

        return new AnnotazioneCompleta(savedAnnotazione, savedMetadata);
    }

    @Override
    public AnnotazioneCompleta aggiornaAnnotazione(UUID id, String nuovoValore, String nuovaDescrizione, String utente) {
        Optional<Annotazione> existingAnnotazione = annotazioneRepository.findById(id);
        Optional<AnnotazioneMetadata> existingMetadata = metadataRepository.findById(id);

        if (existingAnnotazione.isPresent() && existingMetadata.isPresent()) {
            Annotazione annotazione = existingAnnotazione.get();
            AnnotazioneMetadata metadata = existingMetadata.get();

            AnnotazioneStoricoDynamoEntity storico = new AnnotazioneStoricoDynamoEntity();
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
            storicoDynamoRepository.save(storico);

            if (nuovoValore != null) {
                annotazione.setValoreNota(nuovoValore);
                annotazione.setVersioneNota(AnnotazioniUtils.incrementaVersione(annotazione.getVersioneNota()));
            }
            Annotazione updatedAnnotazione = annotazioneRepository.save(annotazione);

            metadata.setDataUltimaModifica(LocalDateTime.now());
            metadata.setUtenteUltimaModifica(utente);
            if (nuovaDescrizione != null) {
                metadata.setDescrizione(nuovaDescrizione);
            }
            metadata.setVersioneNota(annotazione.getVersioneNota());
            AnnotazioneMetadata updatedMetadata = metadataRepository.save(metadata);

            return new AnnotazioneCompleta(updatedAnnotazione, updatedMetadata);
        }
        throw new RuntimeException("Annotazione non trovata con ID: " + id);
    }
}
