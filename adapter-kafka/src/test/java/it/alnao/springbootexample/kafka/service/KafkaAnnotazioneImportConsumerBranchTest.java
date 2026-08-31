package it.alnao.springbootexample.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneImportProperties;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Copre getTopicName e i rami con metadata/annotazione assenti nel payload.
 */
class KafkaAnnotazioneImportConsumerBranchTest {

    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneRepository annotazioneRepository;

    private AnnotazioneImportProperties properties;
    private KafkaAnnotazioneImportConsumer consumer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        properties = new AnnotazioneImportProperties();
        properties.getKafka().setTopicName("annotazioni-import-test");
        consumer = new KafkaAnnotazioneImportConsumer(
                metadataRepository, annotazioneRepository, new ObjectMapper(), properties);
        lenient().when(annotazioneRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        lenient().when(metadataRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
    }

    @Test
    void getTopicName_returnsTheConfiguredTopic() {
        assertEquals("annotazioni-import-test", consumer.getTopicName());
    }

    @Test
    void importaAnnotazione_withoutMetadata_createsAnEmptyOne() throws Exception {
        String payload = "{\"annotazione\":{\"versioneNota\":\"1.0\",\"valoreNota\":\"testo\"}}";

        consumer.importaAnnotazione(payload);

        verify(metadataRepository).save(any(AnnotazioneMetadata.class));
        verify(annotazioneRepository).save(any(Annotazione.class));
    }

    @Test
    void importaAnnotazione_withoutAnnotazione_createsAnEmptyOne() throws Exception {
        String payload = "{\"metadata\":{\"versioneNota\":\"1.0\",\"utenteCreazione\":\"mario\"}}";

        consumer.importaAnnotazione(payload);

        verify(annotazioneRepository).save(any(Annotazione.class));
    }

    @Test
    void importaAnnotazione_withAnEmptyPayload_stillSavesBothParts() throws Exception {
        consumer.importaAnnotazione("{}");

        verify(metadataRepository).save(any(AnnotazioneMetadata.class));
        verify(annotazioneRepository).save(any(Annotazione.class));
    }
}
