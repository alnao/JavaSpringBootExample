package it.alnao.springbootexample.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaAnnotazioneInvioServiceTest {

    @Mock
    private AnnotazioneMetadataRepository metadataRepository;

    @Mock
    private AnnotazioneRepository annotazioneRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private AnnotazioneInvioProperties properties;

    @Mock
    private ObjectMapper objectMapper;

    private KafkaAnnotazioneInvioService service;

    @BeforeEach
    void setUp() {
        service = new KafkaAnnotazioneInvioService(
            metadataRepository, annotazioneRepository, kafkaTemplate, properties, objectMapper);

        // Setup default properties (only when needed)
        AnnotazioneInvioProperties.Kafka kafkaProps = new AnnotazioneInvioProperties.Kafka();
        kafkaProps.setTopicName("test-topic");
        lenient().when(properties.getKafka()).thenReturn(kafkaProps);
        lenient().when(properties.isEnabled()).thenReturn(true);
    }

    @Test
    void testInviaAnnotazioni_Success() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "testuser", "test desc");
        metadata.setStato(StatoAnnotazione.DAINVIARE.getValue());
        
        Annotazione annotazione = new Annotazione(id, "1.0", "test value");
        
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE))
            .thenReturn(List.of(metadata));
        when(annotazioneRepository.findById(id))
            .thenReturn(Optional.of(annotazione));
        when(objectMapper.writeValueAsString(any(AnnotazioneCompleta.class)))
            .thenReturn("{\"test\":\"json\"}");
            
        // Mock CompletableFuture per kafkaTemplate.send
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(future);

        // Act
        List<AnnotazioneCompleta> result = service.inviaAnnotazioni();

        // Assert
        assertEquals(1, result.size());
        assertEquals(StatoAnnotazione.INVIATA.getValue(), metadata.getStato());
        
        verify(kafkaTemplate).send(eq("test-topic"), eq(id.toString()), eq("{\"test\":\"json\"}"));
        verify(metadataRepository).save(metadata);
    }

    @Test
    void testInviaAnnotazioni_NoAnnotationsToSend() {
        // Arrange
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE))
            .thenReturn(Collections.emptyList());

        // Act
        List<AnnotazioneCompleta> result = service.inviaAnnotazioni();

        // Assert
        assertEquals(0, result.size());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void testInviaAnnotazioni_AnnotationNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "testuser", "test desc");
        metadata.setStato(StatoAnnotazione.DAINVIARE.getValue());
        
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE))
            .thenReturn(List.of(metadata));
        when(annotazioneRepository.findById(id))
            .thenReturn(Optional.empty());

        // Act
        List<AnnotazioneCompleta> result = service.inviaAnnotazioni();

        // Assert
        assertEquals(0, result.size());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void testIsEnabled() {
        // Arrange
        when(properties.isEnabled()).thenReturn(false);

        // Act
        boolean result = service.isEnabled();

        // Assert
        assertFalse(result);
    }
}
