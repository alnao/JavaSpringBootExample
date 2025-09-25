package it.alnao.springbootexample.aws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.service.AnnotazioneInvioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(value = "spring.profiles.active", havingValue = "aws")
public class SqsAnnotazioneInvioService implements AnnotazioneInvioService {
    
    private static final Logger logger = LoggerFactory.getLogger(SqsAnnotazioneInvioService.class);
    
    private final AnnotazioneMetadataRepository metadataRepository;
    private final AnnotazioneRepository annotazioneRepository;
    private final SqsClient sqsClient;
    private final AnnotazioneInvioProperties properties;
    private final ObjectMapper objectMapper;
    
    public SqsAnnotazioneInvioService(AnnotazioneMetadataRepository metadataRepository,
                                    AnnotazioneRepository annotazioneRepository,
                                    SqsClient sqsClient,
                                    AnnotazioneInvioProperties properties,
                                    ObjectMapper objectMapper) {
        this.metadataRepository = metadataRepository;
        this.annotazioneRepository = annotazioneRepository;
        this.sqsClient = sqsClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional
    public List<AnnotazioneCompleta> inviaAnnotazioni() {
        List<AnnotazioneMetadata> metadatiDaInviare = metadataRepository.findByStato(StatoAnnotazione.DAINVIARE);
        List<AnnotazioneCompleta> annotazioniInviate = new ArrayList<>();
        
        for (AnnotazioneMetadata metadata : metadatiDaInviare) {
            try {
                // Recupera l'annotazione completa
                var annotazioneOpt = annotazioneRepository.findById(metadata.getId());
                if (annotazioneOpt.isEmpty()) {
                    logger.warn("Annotazione con ID {} non trovata", metadata.getId());
                    continue;
                }
                
                AnnotazioneCompleta annotazioneCompleta = new AnnotazioneCompleta(
                    annotazioneOpt.get(), metadata);
                
                // Serializza per SQS
                String messageJson = objectMapper.writeValueAsString(annotazioneCompleta);
                
                // Crea il messaggio SQS
                SendMessageRequest request = SendMessageRequest.builder()
                        .queueUrl(properties.getSqs().getQueueUrl())
                        .messageBody(messageJson)
                        .messageGroupId("annotazioni-invio")
                        .messageDeduplicationId(metadata.getId().toString())
                        .build();
                
                // Invia a SQS
                SendMessageResponse response = sqsClient.sendMessage(request);
                
                // Aggiorna lo stato a INVIATA
                metadata.setStato(StatoAnnotazione.INVIATA.getValue());
                metadataRepository.save(metadata);
                
                annotazioniInviate.add(annotazioneCompleta);
                
                logger.debug("Annotazione {} inviata a SQS con MessageId {}", 
                    metadata.getId(), response.messageId());
                
            } catch (Exception e) {
                logger.error("Errore durante l'invio dell'annotazione {} a SQS", metadata.getId(), e);
                // Imposta lo stato di errore
                metadata.setStato(StatoAnnotazione.ERRORE.getValue());
                metadataRepository.save(metadata);
            }
        }
        
        return annotazioniInviate;
    }
    
    @Override
    public boolean isEnabled() {
        return properties.isEnabled() && properties.getSqs().getQueueUrl() != null;
    }
}
