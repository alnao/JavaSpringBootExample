package it.alnao.springbootexample.kafka.service;

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
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Profile("kube")
public class KafkaAnnotazioneInvioService implements AnnotazioneInvioService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaAnnotazioneInvioService.class);
    
    private final AnnotazioneMetadataRepository metadataRepository;
    private final AnnotazioneRepository annotazioneRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AnnotazioneInvioProperties properties;
    private final ObjectMapper objectMapper;
    
    public KafkaAnnotazioneInvioService(AnnotazioneMetadataRepository metadataRepository,
                                      AnnotazioneRepository annotazioneRepository,
                                      KafkaTemplate<String, String> kafkaTemplate,
                                      AnnotazioneInvioProperties properties,
                                      ObjectMapper objectMapper) {
        this.metadataRepository = metadataRepository;
        this.annotazioneRepository = annotazioneRepository;
        this.kafkaTemplate = kafkaTemplate;
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
                
                // Serializza per Kafka
                String messageJson = objectMapper.writeValueAsString(annotazioneCompleta);
                
                // Invia a Kafka
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    properties.getKafka().getTopicName(),
                    metadata.getId().toString(),
                    messageJson
                );
                
                if (future != null) {
                    future.whenComplete((result, exception) -> {
                        if (exception == null) {
                            logger.debug("Annotazione {} inviata a Kafka topic {} con offset {}",
                                metadata.getId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                        } else {
                            logger.error("Errore invio annotazione {} a Kafka", metadata.getId(), exception);
                        }
                    });
                } else {
                    logger.warn("KafkaTemplate.send() ha restituito null per l'annotazione {}", metadata.getId());
                }
                
                // Aggiorna lo stato a INVIATA
                metadata.setStato(StatoAnnotazione.INVIATA.getValue());
                metadataRepository.save(metadata);
                
                annotazioniInviate.add(annotazioneCompleta);
                
                logger.debug("Annotazione {} inviata con successo", metadata.getId());
                
            } catch (Exception e) {
                logger.error("Errore durante l'invio dell'annotazione {}", metadata.getId(), e);
                // Imposta lo stato di errore
                metadata.setStato(StatoAnnotazione.ERRORE.getValue());
                metadataRepository.save(metadata);
            }
        }
        
        return annotazioniInviate;
    }
    
    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }
}
