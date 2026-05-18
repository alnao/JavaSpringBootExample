package it.alnao.springbootexample.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneImportProperties;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Profile("kube")
public class KafkaAnnotazioneImportConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaAnnotazioneImportConsumer.class);

    private final AnnotazioneMetadataRepository metadataRepository;
    private final AnnotazioneRepository annotazioneRepository;
    private final ObjectMapper objectMapper;
    private final AnnotazioneImportProperties properties;

    public KafkaAnnotazioneImportConsumer(AnnotazioneMetadataRepository metadataRepository,
                                          AnnotazioneRepository annotazioneRepository,
                                          ObjectMapper objectMapper,
                                          AnnotazioneImportProperties properties) {
        this.metadataRepository = metadataRepository;
        this.annotazioneRepository = annotazioneRepository;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public String getTopicName() {
        return properties.getKafka().getTopicName();
    }

    @KafkaListener(
        topics = "${gestione-annotazioni.import-annotazioni.kafka.topic-name:annotazioni-import}",
        groupId = "annotazioni-import-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void importaAnnotazione(String message) throws com.fasterxml.jackson.core.JsonProcessingException {
        AnnotazioneCompleta annotazioneCompleta = objectMapper.readValue(message, AnnotazioneCompleta.class);
        AnnotazioneMetadata metadata = annotazioneCompleta.getMetadata();
        Annotazione annotazione = annotazioneCompleta.getAnnotazione();

        if (metadata == null) {
            metadata = new AnnotazioneMetadata();
            annotazioneCompleta.setMetadata(metadata);
        }
        if (annotazione == null) {
            annotazione = new Annotazione();
            annotazioneCompleta.setAnnotazione(annotazione);
        }

        // Generazione / allineamento ID
        UUID id;
        if (metadata.getId() != null) {
            id = metadata.getId();
        } else if (annotazione.getId() != null) {
            id = annotazione.getId();
        } else {
            id = UUID.randomUUID();
            logger.info("Generato nuovo UUID {} per annotazione importata", id);
        }
        if (metadata.getId() == null) {
            metadata.setId(id);
        }
        if (annotazione.getId() == null) {
            annotazione.setId(id);
        }
        if (!metadata.getId().equals(annotazione.getId())) {
            logger.warn("ID metadata {} diverso da ID annotazione {}: allineamento a quello di metadata", metadata.getId(), annotazione.getId());
            annotazione.setId(metadata.getId());
            id = metadata.getId();
        }

        final UUID finalId = id;
        final AnnotazioneMetadata finalMetadata = metadata;
        final Annotazione finalAnnotazione = annotazione;

        // Se MongoDB ha già un documento con questo ID, genera un nuovo UUID per entrambi
        if (annotazioneRepository.findById(finalId).isPresent()) {
            UUID nuovoId = UUID.randomUUID();
            logger.warn("Annotazione {} già presente su MongoDB: generato nuovo UUID {} per l'import", finalId, nuovoId);
            finalAnnotazione.setId(nuovoId);
            finalMetadata.setId(nuovoId);
        }

        // Salva/aggiorna metadata su PostgreSQL (con l'ID definitivo, nuovo o originale)
        metadataRepository.findById(finalMetadata.getId()).ifPresentOrElse(existing -> {
            existing.setStato(StatoAnnotazione.IMPORTATA.getValue());
            metadataRepository.save(existing);
            logger.info("Annotazione {} importata con stato {}", existing.getId(), StatoAnnotazione.IMPORTATA);
        }, () -> {
            finalMetadata.setStato(StatoAnnotazione.IMPORTATA.getValue());
            metadataRepository.save(finalMetadata);
            logger.info("Annotazione {} importata da Kafka (nuova metadata)", finalMetadata.getId());
        });

        // Salva su MongoDB (l'ID è già allineato con metadata)
        annotazioneRepository.save(finalAnnotazione);
        logger.info("Annotazione {} salvata su MongoDB", finalAnnotazione.getId());
    }
}
