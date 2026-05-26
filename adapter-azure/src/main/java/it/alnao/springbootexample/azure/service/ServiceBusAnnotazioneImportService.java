package it.alnao.springbootexample.azure.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.alnao.springbootexample.azure.config.AzureProperties;
import it.alnao.springbootexample.core.config.AnnotazioneImportProperties;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.service.AnnotazioneImportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("azure")
public class ServiceBusAnnotazioneImportService implements AnnotazioneImportService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBusAnnotazioneImportService.class);

    private final AnnotazioneMetadataRepository metadataRepository;
    private final AnnotazioneRepository annotazioneRepository;
    private final AnnotazioneImportProperties annotazioneImportProperties;
    private final AzureProperties azureProperties;
    private final ObjectMapper objectMapper;

    public ServiceBusAnnotazioneImportService(
            AnnotazioneMetadataRepository metadataRepository,
            AnnotazioneRepository annotazioneRepository,
            AnnotazioneImportProperties annotazioneImportProperties,
            AzureProperties azureProperties) {
        this.metadataRepository = metadataRepository;
        this.annotazioneRepository = annotazioneRepository;
        this.annotazioneImportProperties = annotazioneImportProperties;
        this.azureProperties = azureProperties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional
    public List<AnnotazioneCompleta> importaAnnotazioni() {
        String connectionString = azureProperties.getServiceBus().getConnectionString();
        String importQueueName = azureProperties.getServiceBus().getImportQueueName();

        if (connectionString == null || connectionString.isBlank()
                || connectionString.startsWith("localServiceBusConnectionString")) {
            logger.warn("ServiceBus connection string non configurata: import non eseguito");
            return List.of();
        }
        if (importQueueName == null || importQueueName.isBlank()) {
            logger.warn("ServiceBus import queue name non configurato: import non eseguito");
            return List.of();
        }

        int maxMessages = Optional.ofNullable(annotazioneImportProperties.getAws())
                .map(a -> a.getSqs().getMaxNumberOfMessages()).orElse(10);

        List<AnnotazioneCompleta> importate = new ArrayList<>();

        try (ServiceBusReceiverClient receiverClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .receiver()
                .queueName(importQueueName)
                .buildClient()) {

            Iterable<ServiceBusReceivedMessage> messages = receiverClient.receiveMessages(maxMessages);
            for (ServiceBusReceivedMessage message : messages) {
                try {
                    AnnotazioneCompleta annotazioneCompleta = processaMessaggio(message.getBody().toString());
                    importate.add(annotazioneCompleta);
                    receiverClient.complete(message);
                    logger.debug("Messaggio ServiceBus {} processato e completato", message.getMessageId());
                } catch (Exception e) {
                    logger.error("Errore import messaggio ServiceBus {}", message.getMessageId(), e);
                    receiverClient.abandon(message);
                }
            }
        } catch (Exception e) {
            logger.error("Errore nella connessione al ServiceBus per import", e);
        }

        return importate;
    }

    private AnnotazioneCompleta processaMessaggio(String payload) throws com.fasterxml.jackson.core.JsonProcessingException {
        AnnotazioneCompleta annotazioneCompleta = objectMapper.readValue(payload, AnnotazioneCompleta.class);
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

        UUID id;
        if (metadata.getId() != null) {
            id = metadata.getId();
        } else if (annotazione.getId() != null) {
            id = annotazione.getId();
        } else {
            id = UUID.randomUUID();
            logger.info("Generato nuovo UUID {} per annotazione importata da ServiceBus", id);
        }

        if (metadata.getId() == null) metadata.setId(id);
        if (annotazione.getId() == null) annotazione.setId(id);
        if (!metadata.getId().equals(annotazione.getId())) {
            logger.warn("ID metadata {} diverso da ID annotazione {}: allineamento a metadata", metadata.getId(), annotazione.getId());
            annotazione.setId(metadata.getId());
        }

        if (annotazioneRepository.findById(metadata.getId()).isPresent()) {
            UUID nuovoId = UUID.randomUUID();
            logger.warn("Annotazione {} già presente: generato nuovo UUID {} per l'import", metadata.getId(), nuovoId);
            annotazione.setId(nuovoId);
            metadata.setId(nuovoId);
        }

        Optional<AnnotazioneMetadata> esistente = metadataRepository.findById(metadata.getId());
        if (esistente.isPresent()) {
            AnnotazioneMetadata existing = esistente.get();
            existing.setStato(StatoAnnotazione.IMPORTATA.getValue());
            existing.setDataUltimaModifica(metadata.getDataUltimaModifica());
            existing.setUtenteUltimaModifica(metadata.getUtenteUltimaModifica());
            existing.setDescrizione(metadata.getDescrizione());
            existing.setCategoria(metadata.getCategoria());
            existing.setTags(metadata.getTags());
            existing.setPubblica(metadata.getPubblica());
            existing.setPriorita(metadata.getPriorita());
            existing.setVersioneNota(metadata.getVersioneNota());
            metadataRepository.save(existing);
            annotazioneCompleta.setMetadata(existing);
        } else {
            metadata.setStato(StatoAnnotazione.IMPORTATA.getValue());
            metadataRepository.save(metadata);
        }

        annotazioneRepository.save(annotazione);
        return annotazioneCompleta;
    }

    @Override
    public boolean isEnabled() {
        String cs = azureProperties.getServiceBus().getConnectionString();
        return annotazioneImportProperties.isEnabled()
                && cs != null && !cs.isBlank() && !cs.startsWith("localServiceBusConnectionString");
    }
}
