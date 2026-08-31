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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Copre guardie di configurazione, normalizzazione dei parametri SQS e i rami
 * di processaMessaggio non verificati in {@link SqsAnnotazioneImportServiceTest}.
 */
class SqsAnnotazioneImportServiceBranchTest {

    private static final String IMPORT_QUEUE = "http://sqs.test.local/queue/annotazioni-import";

    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock SqsClient sqsClient;

    private ObjectMapper objectMapper;
    private AnnotazioneImportProperties importProperties;
    private AwsProperties awsProperties;
    private SqsAnnotazioneImportService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        importProperties = new AnnotazioneImportProperties();
        importProperties.setEnabled(true);
        awsProperties = new AwsProperties();
        awsProperties.getSqs().setImportQueueUrl(IMPORT_QUEUE);
        service = new SqsAnnotazioneImportService(metadataRepository, annotazioneRepository,
                sqsClient, importProperties, awsProperties, objectMapper);
    }

    private Message message(String body) {
        return Message.builder().messageId("m-1").receiptHandle("rh-1").body(body).build();
    }

    private void queueReturns(Message... messages) {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(messages).build());
    }

    private String payload(String annotazioneId, String metadataId) {
        String ann = annotazioneId == null ? "" : "\"id\":\"" + annotazioneId + "\",";
        String meta = metadataId == null ? "" : "\"id\":\"" + metadataId + "\",";
        return "{\"annotazione\":{" + ann + "\"versioneNota\":\"1.0\",\"valoreNota\":\"testo\"},"
                + "\"metadata\":{" + meta + "\"versioneNota\":\"1.0\",\"utenteCreazione\":\"mario\","
                + "\"descrizione\":\"descr\"}}";
    }

    // ---------- isEnabled ----------

    @Test
    void isEnabled_trueWhenImportEnabledAndQueueConfigured() {
        assertTrue(service.isEnabled());
    }

    @Test
    void isEnabled_falseWhenImportDisabled() {
        importProperties.setEnabled(false);
        assertFalse(service.isEnabled());
    }

    @Test
    void isEnabled_falseWhenQueueUrlIsNull() {
        awsProperties.getSqs().setImportQueueUrl(null);
        assertFalse(service.isEnabled());
    }

    // ---------- guardie e parametri di ricezione ----------

    @Test
    void importaAnnotazioni_whenQueueUrlIsNull_returnsEmptyWithoutCallingSqs() {
        awsProperties.getSqs().setImportQueueUrl(null);
        assertTrue(service.importaAnnotazioni().isEmpty());
        verifyNoInteractions(sqsClient);
    }

    @Test
    void importaAnnotazioni_whenQueueUrlIsBlank_returnsEmptyWithoutCallingSqs() {
        awsProperties.getSqs().setImportQueueUrl("   ");
        assertTrue(service.importaAnnotazioni().isEmpty());
        verifyNoInteractions(sqsClient);
    }

    @Test
    void importaAnnotazioni_usesTheConfiguredReceiveParameters() {
        importProperties.getAws().getSqs().setMaxNumberOfMessages(7);
        importProperties.getAws().getSqs().setWaitTimeSeconds(15);
        queueReturns();

        service.importaAnnotazioni();

        ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsClient).receiveMessage(captor.capture());
        assertEquals(7, captor.getValue().maxNumberOfMessages());
        assertEquals(15, captor.getValue().waitTimeSeconds());
        assertEquals(IMPORT_QUEUE, captor.getValue().queueUrl());
    }

    @Test
    void importaAnnotazioni_clampsOutOfRangeReceiveParameters() {
        importProperties.getAws().getSqs().setMaxNumberOfMessages(99);
        importProperties.getAws().getSqs().setWaitTimeSeconds(999);
        queueReturns();

        service.importaAnnotazioni();

        ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsClient).receiveMessage(captor.capture());
        assertEquals(10, captor.getValue().maxNumberOfMessages());
        assertEquals(20, captor.getValue().waitTimeSeconds());
    }

    @Test
    void importaAnnotazioni_raisesBelowRangeReceiveParametersToTheMinimum() {
        importProperties.getAws().getSqs().setMaxNumberOfMessages(0);
        importProperties.getAws().getSqs().setWaitTimeSeconds(-5);
        queueReturns();

        service.importaAnnotazioni();

        ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsClient).receiveMessage(captor.capture());
        assertEquals(1, captor.getValue().maxNumberOfMessages());
        assertEquals(0, captor.getValue().waitTimeSeconds());
    }

    @Test
    void importaAnnotazioni_withNullReceiveParameters_usesTheFallbacks() {
        importProperties.getAws().getSqs().setMaxNumberOfMessages(null);
        importProperties.getAws().getSqs().setWaitTimeSeconds(null);
        queueReturns();

        service.importaAnnotazioni();

        ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsClient).receiveMessage(captor.capture());
        assertEquals(10, captor.getValue().maxNumberOfMessages());
        assertEquals(5, captor.getValue().waitTimeSeconds());
    }

    @Test
    void importaAnnotazioni_whenTheQueueIsEmpty_returnsEmpty() {
        queueReturns();
        assertTrue(service.importaAnnotazioni().isEmpty());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    // ---------- elaborazione dei messaggi ----------

    @Test
    void importaAnnotazioni_deletesTheMessageAfterASuccessfulImport() {
        UUID id = UUID.randomUUID();
        queueReturns(message(payload(id.toString(), id.toString())));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        List<AnnotazioneCompleta> result = service.importaAnnotazioni();

        assertEquals(1, result.size());
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void importaAnnotazioni_whenThePayloadIsInvalid_keepsTheMessageOnTheQueue() {
        queueReturns(message("{non e' json valido"));

        List<AnnotazioneCompleta> result = service.importaAnnotazioni();

        assertTrue(result.isEmpty());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void importaAnnotazioni_newAnnotazione_isSavedAsImportata() {
        UUID id = UUID.randomUUID();
        queueReturns(message(payload(id.toString(), id.toString())));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        service.importaAnnotazioni();

        ArgumentCaptor<AnnotazioneMetadata> captor = ArgumentCaptor.forClass(AnnotazioneMetadata.class);
        verify(metadataRepository).save(captor.capture());
        assertEquals(StatoAnnotazione.IMPORTATA.getValue(), captor.getValue().getStato());
        verify(annotazioneRepository).save(any(Annotazione.class));
    }

    @Test
    void importaAnnotazioni_whenMetadataAlreadyExists_updatesTheExistingRow() {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata esistente = new AnnotazioneMetadata(id, "0.9", "mario", "vecchia");
        queueReturns(message(payload(id.toString(), id.toString())));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.of(esistente));

        service.importaAnnotazioni();

        ArgumentCaptor<AnnotazioneMetadata> captor = ArgumentCaptor.forClass(AnnotazioneMetadata.class);
        verify(metadataRepository).save(captor.capture());
        assertEquals(StatoAnnotazione.IMPORTATA.getValue(), captor.getValue().getStato());
        assertEquals("descr", captor.getValue().getDescrizione());
        assertEquals("1.0", captor.getValue().getVersioneNota());
    }

    @Test
    void importaAnnotazioni_whenTheAnnotazioneAlreadyExists_reassignsANewId() {
        UUID id = UUID.randomUUID();
        queueReturns(message(payload(id.toString(), id.toString())));
        when(annotazioneRepository.findById(id))
                .thenReturn(Optional.of(new Annotazione(id, "1.0", "gia presente")));
        when(metadataRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        List<AnnotazioneCompleta> result = service.importaAnnotazioni();

        assertNotEquals(id, result.get(0).getAnnotazione().getId());
        assertEquals(result.get(0).getAnnotazione().getId(), result.get(0).getMetadata().getId());
    }

    @Test
    void importaAnnotazioni_withoutAnyId_generatesOne() {
        queueReturns(message(payload(null, null)));
        when(annotazioneRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(metadataRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        List<AnnotazioneCompleta> result = service.importaAnnotazioni();

        assertNotNull(result.get(0).getMetadata().getId());
        assertEquals(result.get(0).getMetadata().getId(), result.get(0).getAnnotazione().getId());
    }

    @Test
    void importaAnnotazioni_withOnlyTheAnnotazioneId_propagatesItToMetadata() {
        UUID id = UUID.randomUUID();
        queueReturns(message(payload(id.toString(), null)));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        List<AnnotazioneCompleta> result = service.importaAnnotazioni();

        assertEquals(id, result.get(0).getMetadata().getId());
    }

    @Test
    void importaAnnotazioni_whenIdsDiffer_alignsTheAnnotazioneToMetadata() {
        UUID idAnn = UUID.randomUUID();
        UUID idMeta = UUID.randomUUID();
        queueReturns(message(payload(idAnn.toString(), idMeta.toString())));
        when(annotazioneRepository.findById(idMeta)).thenReturn(Optional.empty());
        when(metadataRepository.findById(idMeta)).thenReturn(Optional.empty());

        List<AnnotazioneCompleta> result = service.importaAnnotazioni();

        assertEquals(idMeta, result.get(0).getAnnotazione().getId());
    }

    @Test
    void importaAnnotazioni_processesEveryMessageInTheBatch() {
        UUID primo = UUID.randomUUID();
        UUID secondo = UUID.randomUUID();
        queueReturns(message(payload(primo.toString(), primo.toString())),
                     message(payload(secondo.toString(), secondo.toString())));
        when(annotazioneRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(metadataRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertEquals(2, service.importaAnnotazioni().size());
        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
