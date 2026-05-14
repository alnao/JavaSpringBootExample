package it.alnao.springbootexample.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("kube")
public class KafkaAnnotazioneImportConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaAnnotazioneImportConsumer.class);

    private final AnnotazioneMetadataRepository metadataRepository;
    private final ObjectMapper objectMapper;

    public KafkaAnnotazioneImportConsumer(AnnotazioneMetadataRepository metadataRepository,
                                          ObjectMapper objectMapper) {
        this.metadataRepository = metadataRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${gestione-annotazioni.export-annotazioni.kafka.topic-name:annotazioni-export}",
        groupId = "annotazioni-import-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void importaAnnotazione(String message) throws com.fasterxml.jackson.core.JsonProcessingException {
        AnnotazioneCompleta annotazioneCompleta = objectMapper.readValue(message, AnnotazioneCompleta.class);
        AnnotazioneMetadata metadata = annotazioneCompleta.getMetadata();
        if (metadata == null || metadata.getId() == null) {
            logger.warn("Messaggio Kafka non valido: metadata o id mancanti");
            return;
        }

        metadataRepository.findById(metadata.getId()).ifPresentOrElse(existing -> {
            existing.setStato(StatoAnnotazione.IMPORTATA.getValue());
            metadataRepository.save(existing);
            logger.info("Annotazione {} importata con stato {}", existing.getId(), StatoAnnotazione.IMPORTATA);
        }, () -> {
            metadata.setStato(StatoAnnotazione.IMPORTATA.getValue());
            metadataRepository.save(metadata);
            logger.info("Annotazione {} importata da Kafka (nuova metadata)", metadata.getId());
        });
    }
}
