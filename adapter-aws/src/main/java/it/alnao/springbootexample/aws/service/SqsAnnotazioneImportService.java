package it.alnao.springbootexample.aws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.aws.config.AwsProperties;
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
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("aws")
public class SqsAnnotazioneImportService implements AnnotazioneImportService {

    private static final Logger logger = LoggerFactory.getLogger(SqsAnnotazioneImportService.class);

    private final AnnotazioneMetadataRepository metadataRepository;
    private final AnnotazioneRepository annotazioneRepository;
    private final SqsClient sqsClient;
    private final AnnotazioneImportProperties annotazioneImportProperties;
    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;

    public SqsAnnotazioneImportService(AnnotazioneMetadataRepository metadataRepository,
                                       AnnotazioneRepository annotazioneRepository,
                                       SqsClient sqsClient,
                                       AnnotazioneImportProperties annotazioneImportProperties,
                                       AwsProperties awsProperties,
                                       ObjectMapper objectMapper) {
        this.metadataRepository = metadataRepository;
        this.annotazioneRepository = annotazioneRepository;
        this.sqsClient = sqsClient;
        this.annotazioneImportProperties = annotazioneImportProperties;
        this.awsProperties = awsProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public List<AnnotazioneCompleta> importaAnnotazioni() {
        String queueUrl = awsProperties.getSqs().getImportQueueUrl();
        if (queueUrl == null || queueUrl.isBlank()) {
            logger.warn("Import Queue URL SQS non configurato: import non eseguito");
            return List.of();
        }

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(normalizzaIntervallo(annotazioneImportProperties.getAws().getSqs().getMaxNumberOfMessages(), 1, 10, 10))
            .waitTimeSeconds(normalizzaIntervallo(annotazioneImportProperties.getAws().getSqs().getWaitTimeSeconds(), 0, 20, 5))
            .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        List<AnnotazioneCompleta> importate = new ArrayList<>();

        for (Message message : messages) {
            try {
                AnnotazioneCompleta annotazioneCompleta = processaMessaggio(message.body());
                importate.add(annotazioneCompleta);

                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());

                logger.debug("Messaggio SQS {} processato e cancellato", message.messageId());
            } catch (Exception e) {
                logger.error("Errore import messaggio SQS {}", message.messageId(), e);
            }
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
            logger.info("Generato nuovo UUID {} per annotazione importata da SQS", id);
        }

        if (metadata.getId() == null) {
            metadata.setId(id);
        }
        if (annotazione.getId() == null) {
            annotazione.setId(id);
        }
        if (!metadata.getId().equals(annotazione.getId())) {
            logger.warn("ID metadata {} diverso da ID annotazione {}: allineamento a metadata", metadata.getId(), annotazione.getId());
            annotazione.setId(metadata.getId());
        }

        if (annotazioneRepository.findById(metadata.getId()).isPresent()) {
            UUID nuovoId = UUID.randomUUID();
            logger.warn("Annotazione {} già presente su DynamoDB: generato nuovo UUID {} per l'import", metadata.getId(), nuovoId);
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

    private int normalizzaIntervallo(Integer value, int min, int max, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    @Override
    public boolean isEnabled() {
        return annotazioneImportProperties.isEnabled() && awsProperties.getSqs().getImportQueueUrl() != null;
    }
}
