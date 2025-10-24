package it.alnao.springbootexample.azure.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.alnao.springbootexample.azure.config.AzureProperties;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.scheduler.AnnotazioneInvioScheduler;
import it.alnao.springbootexample.core.service.AnnotazioneInvioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("azure")
@ConditionalOnProperty(value = "gestione-annotazioni.export-annotazioni.enabled", havingValue = "true", matchIfMissing = false)
//@ConditionalOnProperty(value = "annotazione.invio.enabled", havingValue = "true", matchIfMissing = false)
//nota questo è obbligatorio per evitare l'errore
//Parameter 0 of constructor in it.alnao.springbootexample.core.scheduler.AnnotazioneInvioScheduler required a bean of type 'it.alnao.springbootexample.core.service.AnnotazioneInvioService' that could not be found.
public class AnnotazioneInvioServiceAzureImpl implements AnnotazioneInvioService {
    private static final Logger logger = LoggerFactory.getLogger(AnnotazioneInvioScheduler.class);
    
    private final AnnotazioneMetadataRepository metadataRepository;
    private final ServiceBusSenderClient senderClient;
    private final AnnotazioneInvioProperties annotazioneInvioProperties;
    private final AzureProperties azureProperties;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public AnnotazioneInvioServiceAzureImpl(
        AnnotazioneMetadataRepository metadataRepository,
        AnnotazioneInvioProperties annotazioneInvioProperties,
        AzureProperties azureProperties) {
        this.metadataRepository = metadataRepository;
        this.annotazioneInvioProperties = annotazioneInvioProperties;
        this.azureProperties = azureProperties;
        this.enabled = this.annotazioneInvioProperties.isEnabled();

        logger.info("Starting Service Bus client with connection string: {}", this.azureProperties.getServiceBus().getConnectionString());
        logger.info("Starting Service Bus client with queue name: {}", this.azureProperties.getServiceBus().getQueueName());

        this.senderClient = new ServiceBusClientBuilder()
            .connectionString(this.azureProperties.getServiceBus().getConnectionString())
            .sender()
            .queueName(this.azureProperties.getServiceBus().getQueueName())
            .buildClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Errore durante l'invio dell'annotazione con ID 5a32515f-c89f-48df-b0db-aa5c78bfc5b8: Java 8 date/time type `java.time.LocalDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: it.alnao.springbootexample.core.domain.AnnotazioneCompleta["metadata"]->it.alnao.springbootexample.core.domain.AnnotazioneMetadata["dataInserimento"])
    }

    @Override
    public List<AnnotazioneCompleta> inviaAnnotazioni() {
        if (!this.enabled) return List.of();
        List<AnnotazioneCompleta> daInviare = metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)
                .stream()
                .map(meta -> new AnnotazioneCompleta(null, meta))
                .collect(Collectors.toList());
        for (AnnotazioneCompleta ann : daInviare) {
            try {
                String json = objectMapper.writeValueAsString(ann);
                ServiceBusMessage message = new ServiceBusMessage(json);
                senderClient.sendMessage(message);
                // Aggiorna stato a INVIATA
                var meta = ann.getMetadata();
                meta.setStato(StatoAnnotazione.INVIATA.name());
                metadataRepository.save(meta);
                logger.info("Annotazione con ID {} inviata con successo", ann.getMetadata().getId());
            } catch (Exception e) {
                logger.error("Errore durante l'invio dell'annotazione con ID {}: {}", ann.getMetadata().getId(), e.getMessage());
                e.printStackTrace();
                // Gestione errore: puoi loggare o impostare stato errore
                var meta = ann.getMetadata();
                meta.setStato(StatoAnnotazione.ERRORE.name());
                metadataRepository.save(meta);
            }
        }
        return daInviare;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
