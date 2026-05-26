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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqsAnnotazioneImportServiceTest {

    private AnnotazioneMetadataRepository metadataRepository;
    private AnnotazioneRepository annotazioneRepository;
    private SqsClient sqsClient;
    private ObjectMapper objectMapper;
    private SqsAnnotazioneImportService service;

    @BeforeEach
    void setUp() {
        metadataRepository = mock(AnnotazioneMetadataRepository.class);
        annotazioneRepository = mock(AnnotazioneRepository.class);
        sqsClient = mock(SqsClient.class);
        objectMapper = mock(ObjectMapper.class);

        AnnotazioneImportProperties importProperties = new AnnotazioneImportProperties();
        importProperties.setEnabled(true);

        AwsProperties awsProperties = new AwsProperties();
        awsProperties.getSqs().setExportQueueUrl("http://sqs.test.local/queue/annotazioni");

        service = new SqsAnnotazioneImportService(
            metadataRepository,
            annotazioneRepository,
            sqsClient,
            importProperties,
            awsProperties,
            objectMapper
        );
    }

    @Test
    void importaAnnotazioni_importaMessaggioEImpostaStatoImportata() throws Exception {
        UUID id = UUID.randomUUID();
        Annotazione annotazione = new Annotazione(id, "1.0", "nota");
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        metadata.setStato(StatoAnnotazione.INVIATA.getValue());
        AnnotazioneCompleta payload = new AnnotazioneCompleta(annotazione, metadata);

        Message msg = Message.builder()
            .messageId("m1")
            .receiptHandle("rh1")
            .body("json")
            .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().messages(msg).build());
        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        service.importaAnnotazioni();

        ArgumentCaptor<AnnotazioneMetadata> metadataCaptor = ArgumentCaptor.forClass(AnnotazioneMetadata.class);
        verify(metadataRepository).save(metadataCaptor.capture());
        assertEquals(StatoAnnotazione.IMPORTATA.getValue(), metadataCaptor.getValue().getStato());

        verify(annotazioneRepository).save(annotazione);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }
}
