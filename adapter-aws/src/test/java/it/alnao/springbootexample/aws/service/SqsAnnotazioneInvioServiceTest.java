package it.alnao.springbootexample.aws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.aws.config.AwsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqsAnnotazioneInvioServiceTest {

    private static final String EXPORT_QUEUE_URL = "https://sqs.test.amazonaws.com/123/test-queue";

    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock SqsClient sqsClient;
    @Mock AnnotazioneInvioProperties annotazioneInvioProperties;
    @Mock AwsProperties awsProperties;
    @Mock ObjectMapper objectMapper;
    @InjectMocks SqsAnnotazioneInvioService service;

    private AwsProperties.SqsConfig sqsConfig;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sqsConfig = new AwsProperties.SqsConfig();
        sqsConfig.setExportQueueUrl(EXPORT_QUEUE_URL);
    }

    @Test
    void isEnabled_delegatesToProperties() {
        when(awsProperties.getSqs()).thenReturn(sqsConfig);
        when(annotazioneInvioProperties.isEnabled()).thenReturn(true);
        assertTrue(service.isEnabled());
        when(annotazioneInvioProperties.isEnabled()).thenReturn(false);
        assertFalse(service.isEnabled());
    }

    @Test
    void isEnabled_falseWhenQueueUrlMissing() {
        AwsProperties.SqsConfig senzaCoda = new AwsProperties.SqsConfig();
        senzaCoda.setExportQueueUrl(null);
        when(awsProperties.getSqs()).thenReturn(senzaCoda);
        when(annotazioneInvioProperties.isEnabled()).thenReturn(true);
        assertFalse(service.isEnabled());
    }

    @Test
    void inviaAnnotazioni_whenNoneToSend_returnsEmptyList() {
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of());
        List<AnnotazioneCompleta> result = service.inviaAnnotazioni();
        assertTrue(result.isEmpty());
    }

    @Test
    void inviaAnnotazioni_whenOneToSend_callsSqsAndUpdatesState() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata meta = buildMetadata(id, StatoAnnotazione.DAINVIARE.getValue());
        Annotazione ann = new Annotazione(id, "1.0", "nota");
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of(meta));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":\"test\"}");
        when(awsProperties.getSqs()).thenReturn(sqsConfig);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-1").build());
        when(metadataRepository.save(any())).thenReturn(meta);

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        assertEquals(1, inviate.size());
        assertEquals(StatoAnnotazione.INVIATA.getValue(), meta.getStato());
        verify(metadataRepository).findByStato(StatoAnnotazione.DAINVIARE);
        verify(sqsClient).sendMessage(any(SendMessageRequest.class));
        verify(metadataRepository).save(meta);
    }

    @Test
    void inviaAnnotazioni_whenAnnotazioneMissing_skipsMessage() {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata meta = buildMetadata(id, StatoAnnotazione.DAINVIARE.getValue());
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of(meta));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        assertTrue(inviate.isEmpty());
        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void inviaAnnotazioni_whenSqsFails_setsErrorState() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata meta = buildMetadata(id, StatoAnnotazione.DAINVIARE.getValue());
        Annotazione ann = new Annotazione(id, "1.0", "nota");
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of(meta));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":\"test\"}");
        when(awsProperties.getSqs()).thenReturn(sqsConfig);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("sqs down"));

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        assertTrue(inviate.isEmpty());
        assertEquals(StatoAnnotazione.ERRORE.getValue(), meta.getStato());
    }

    private AnnotazioneMetadata buildMetadata(UUID id, String stato) {
        AnnotazioneMetadata m = new AnnotazioneMetadata();
        m.setId(id);
        m.setVersioneNota("1.0");
        m.setUtenteCreazione("user");
        m.setDataInserimento(LocalDateTime.now());
        m.setDataUltimaModifica(LocalDateTime.now());
        m.setStato(stato);
        return m;
    }
}
