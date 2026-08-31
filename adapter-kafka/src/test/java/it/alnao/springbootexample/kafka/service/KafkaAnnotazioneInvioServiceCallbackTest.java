package it.alnao.springbootexample.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copre i rami di callback del future Kafka e i percorsi di errore,
 * non verificati in {@link KafkaAnnotazioneInvioServiceTest}.
 */
class KafkaAnnotazioneInvioServiceCallbackTest {

    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock KafkaTemplate<String, String> kafkaTemplate;
    @Mock ObjectMapper objectMapper;

    private AnnotazioneInvioProperties properties;
    private KafkaAnnotazioneInvioService service;
    private UUID id;
    private AnnotazioneMetadata metadata;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        properties = new AnnotazioneInvioProperties();
        properties.setEnabled(true);
        properties.getKafka().setTopicName("test-topic");
        service = new KafkaAnnotazioneInvioService(
                metadataRepository, annotazioneRepository, kafkaTemplate, properties, objectMapper);

        id = UUID.randomUUID();
        metadata = new AnnotazioneMetadata(id, "1.0", "mario", "descrizione");
        metadata.setDataInserimento(LocalDateTime.now());
        metadata.setStato(StatoAnnotazione.DAINVIARE.getValue());

        lenient().when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE))
                .thenReturn(List.of(metadata));
        lenient().when(annotazioneRepository.findById(id))
                .thenReturn(Optional.of(new Annotazione(id, "1.0", "nota")));
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":\"test\"}");
    }

    private SendResult<String, String> sendResult() {
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("test-topic", 0), 0L, 0, 0L, 0, 0);
        return new SendResult<>(new ProducerRecord<>("test-topic", "k", "v"), recordMetadata);
    }

    @Test
    void inviaAnnotazioni_whenTheSendCompletesSuccessfully_marksItSent() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult()));

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        assertEquals(1, inviate.size());
        assertEquals(StatoAnnotazione.INVIATA.getValue(), metadata.getStato());
        verify(metadataRepository).save(metadata);
    }

    @Test
    void inviaAnnotazioni_whenTheSendCompletesExceptionally_stillMarksItSent() {
        CompletableFuture<SendResult<String, String>> fallito = new CompletableFuture<>();
        fallito.completeExceptionally(new RuntimeException("broker irraggiungibile"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(fallito);

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        // il fallimento arriva in modo asincrono: l'invio sincrono e' comunque andato a buon fine
        assertEquals(1, inviate.size());
        assertEquals(StatoAnnotazione.INVIATA.getValue(), metadata.getStato());
    }

    @Test
    void inviaAnnotazioni_whenSerializationFails_marksItAsError() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("serializzazione fallita"));

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        assertTrue(inviate.isEmpty());
        assertEquals(StatoAnnotazione.ERRORE.getValue(), metadata.getStato());
        verify(metadataRepository).save(metadata);
    }

    @Test
    void inviaAnnotazioni_whenSendThrows_marksItAsError() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("kafka giu"));

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        assertTrue(inviate.isEmpty());
        assertEquals(StatoAnnotazione.ERRORE.getValue(), metadata.getStato());
    }

    @Test
    void isEnabled_reflectsTheProperties() {
        assertTrue(service.isEnabled());
        properties.setEnabled(false);
        assertFalse(service.isEnabled());
    }
}
