package it.alnao.springbootexample.azure.service;

import it.alnao.springbootexample.azure.config.AzureProperties;
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceBusAnnotazioneImportServiceTest {

    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneRepository annotazioneRepository;

    private AnnotazioneImportProperties importProperties;
    private AzureProperties azureProperties;
    private ServiceBusAnnotazioneImportService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        importProperties = new AnnotazioneImportProperties();
        importProperties.setEnabled(true);
        azureProperties = new AzureProperties();
        azureProperties.getServiceBus().setConnectionString(
                "Endpoint=sb://test.servicebus.windows.net/;SharedAccessKeyName=k;SharedAccessKey=v");
        azureProperties.getServiceBus().setImportQueueName("annotazioni-import");
        service = new ServiceBusAnnotazioneImportService(
                metadataRepository, annotazioneRepository, importProperties, azureProperties);
    }

    /** processaMessaggio e' privato e non raggiungibile senza un ServiceBus reale. */
    private AnnotazioneCompleta processa(String payload) throws Exception {
        Method m = ServiceBusAnnotazioneImportService.class
                .getDeclaredMethod("processaMessaggio", String.class);
        m.setAccessible(true);
        return (AnnotazioneCompleta) m.invoke(service, payload);
    }

    // ---------- isEnabled ----------

    @Test
    void isEnabled_trueWithRealConnectionString() {
        assertTrue(service.isEnabled());
    }

    @Test
    void isEnabled_falseWhenImportDisabled() {
        importProperties.setEnabled(false);
        assertFalse(service.isEnabled());
    }

    @Test
    void isEnabled_falseWhenConnectionStringIsBlank() {
        azureProperties.getServiceBus().setConnectionString("   ");
        assertFalse(service.isEnabled());
    }

    @Test
    void isEnabled_falseWhenConnectionStringIsThePlaceholder() {
        azureProperties.getServiceBus().setConnectionString("localServiceBusConnectionString-xyz");
        assertFalse(service.isEnabled());
    }

    @Test
    void isEnabled_falseWhenConnectionStringIsNull() {
        azureProperties.getServiceBus().setConnectionString(null);
        assertFalse(service.isEnabled());
    }

    // ---------- importaAnnotazioni: guardie di configurazione ----------

    @Test
    void importaAnnotazioni_whenConnectionStringNull_returnsEmptyWithoutTouchingRepositories() {
        azureProperties.getServiceBus().setConnectionString(null);
        assertTrue(service.importaAnnotazioni().isEmpty());
        verifyNoInteractions(metadataRepository, annotazioneRepository);
    }

    @Test
    void importaAnnotazioni_whenConnectionStringBlank_returnsEmpty() {
        azureProperties.getServiceBus().setConnectionString("  ");
        assertTrue(service.importaAnnotazioni().isEmpty());
    }

    @Test
    void importaAnnotazioni_whenConnectionStringIsThePlaceholder_returnsEmpty() {
        azureProperties.getServiceBus().setConnectionString("localServiceBusConnectionString");
        assertTrue(service.importaAnnotazioni().isEmpty());
    }

    @Test
    void importaAnnotazioni_whenQueueNameNull_returnsEmpty() {
        azureProperties.getServiceBus().setImportQueueName(null);
        assertTrue(service.importaAnnotazioni().isEmpty());
    }

    @Test
    void importaAnnotazioni_whenQueueNameBlank_returnsEmpty() {
        azureProperties.getServiceBus().setImportQueueName("");
        assertTrue(service.importaAnnotazioni().isEmpty());
    }

    // ---------- processaMessaggio ----------

    @Test
    void processaMessaggio_newAnnotazione_savesWithStatoImportata() throws Exception {
        UUID id = UUID.randomUUID();
        String payload = """
                {"annotazione":{"id":"%s","versioneNota":"1.0","valoreNota":"testo"},
                 "metadata":{"id":"%s","versioneNota":"1.0","utenteCreazione":"mario","descrizione":"descr"}}
                """.formatted(id, id);
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        AnnotazioneCompleta result = processa(payload);

        ArgumentCaptor<AnnotazioneMetadata> captor = ArgumentCaptor.forClass(AnnotazioneMetadata.class);
        verify(metadataRepository).save(captor.capture());
        assertEquals(StatoAnnotazione.IMPORTATA.getValue(), captor.getValue().getStato());
        verify(annotazioneRepository).save(any(Annotazione.class));
        assertNotNull(result.getAnnotazione());
    }

    @Test
    void processaMessaggio_whenMetadataAlreadyExists_updatesExistingRow() throws Exception {
        UUID id = UUID.randomUUID();
        String payload = """
                {"annotazione":{"id":"%s","versioneNota":"2.0","valoreNota":"testo"},
                 "metadata":{"id":"%s","versioneNota":"2.0","utenteCreazione":"mario",
                             "descrizione":"nuova descr","categoria":"lavoro","tags":"a,b",
                             "pubblica":true,"priorita":3}}
                """.formatted(id, id);
        AnnotazioneMetadata esistente = new AnnotazioneMetadata(id, "1.0", "mario", "vecchia descr");
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.of(esistente));

        processa(payload);

        ArgumentCaptor<AnnotazioneMetadata> captor = ArgumentCaptor.forClass(AnnotazioneMetadata.class);
        verify(metadataRepository).save(captor.capture());
        AnnotazioneMetadata saved = captor.getValue();
        assertEquals(StatoAnnotazione.IMPORTATA.getValue(), saved.getStato());
        assertEquals("nuova descr", saved.getDescrizione());
        assertEquals("lavoro", saved.getCategoria());
        assertEquals("a,b", saved.getTags());
        assertTrue(saved.getPubblica());
        assertEquals(3, saved.getPriorita());
        assertEquals("2.0", saved.getVersioneNota());
    }

    @Test
    void processaMessaggio_whenAnnotazioneAlreadyExists_reassignsANewId() throws Exception {
        UUID id = UUID.randomUUID();
        String payload = """
                {"annotazione":{"id":"%s","versioneNota":"1.0","valoreNota":"testo"},
                 "metadata":{"id":"%s","versioneNota":"1.0","utenteCreazione":"mario","descrizione":"descr"}}
                """.formatted(id, id);
        when(annotazioneRepository.findById(id))
                .thenReturn(Optional.of(new Annotazione(id, "1.0", "gia presente")));
        when(metadataRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        AnnotazioneCompleta result = processa(payload);

        assertNotEquals(id, result.getAnnotazione().getId());
        assertNotEquals(id, result.getMetadata().getId());
        assertEquals(result.getAnnotazione().getId(), result.getMetadata().getId());
    }

    @Test
    void processaMessaggio_withoutAnyId_generatesOne() throws Exception {
        String payload = """
                {"annotazione":{"versioneNota":"1.0","valoreNota":"testo"},
                 "metadata":{"versioneNota":"1.0","utenteCreazione":"mario","descrizione":"descr"}}
                """;
        when(annotazioneRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(metadataRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        AnnotazioneCompleta result = processa(payload);

        assertNotNull(result.getMetadata().getId());
        assertEquals(result.getMetadata().getId(), result.getAnnotazione().getId());
    }

    @Test
    void processaMessaggio_withOnlyAnnotazioneId_propagatesItToMetadata() throws Exception {
        UUID id = UUID.randomUUID();
        String payload = """
                {"annotazione":{"id":"%s","versioneNota":"1.0","valoreNota":"testo"},
                 "metadata":{"versioneNota":"1.0","utenteCreazione":"mario","descrizione":"descr"}}
                """.formatted(id);
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        AnnotazioneCompleta result = processa(payload);

        assertEquals(id, result.getMetadata().getId());
    }

    @Test
    void processaMessaggio_whenIdsDiffer_alignsAnnotazioneToMetadata() throws Exception {
        UUID idMeta = UUID.randomUUID();
        UUID idAnn = UUID.randomUUID();
        String payload = """
                {"annotazione":{"id":"%s","versioneNota":"1.0","valoreNota":"testo"},
                 "metadata":{"id":"%s","versioneNota":"1.0","utenteCreazione":"mario","descrizione":"descr"}}
                """.formatted(idAnn, idMeta);
        when(annotazioneRepository.findById(idMeta)).thenReturn(Optional.empty());
        when(metadataRepository.findById(idMeta)).thenReturn(Optional.empty());

        AnnotazioneCompleta result = processa(payload);

        assertEquals(idMeta, result.getAnnotazione().getId());
    }

    @Test
    void processaMessaggio_withInvalidJson_throws() {
        assertThrows(Exception.class, () -> processa("{non e' json valido"));
    }
}
