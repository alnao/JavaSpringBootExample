package it.alnao.springbootexample.azure.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import it.alnao.springbootexample.azure.config.AzureProperties;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Il sender ServiceBus viene creato nel costruttore: qui viene sostituito con un
 * mock cosi' da poter verificare il ciclo di invio senza una coda reale.
 */
class AnnotazioneInvioServiceAzureImplSendTest {

    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock ServiceBusSenderClient senderClient;

    private AnnotazioneInvioProperties invioProperties;
    private AnnotazioneInvioServiceAzureImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        invioProperties = new AnnotazioneInvioProperties();
        invioProperties.setEnabled(true);

        AzureProperties azureProperties = new AzureProperties();
        azureProperties.getServiceBus().setConnectionString(
                "Endpoint=sb://test.servicebus.windows.net/;SharedAccessKeyName=k;SharedAccessKey=v");
        azureProperties.getServiceBus().setQueueName("annotazioni-queue");

        service = new AnnotazioneInvioServiceAzureImpl(metadataRepository, invioProperties, azureProperties);
        ReflectionTestUtils.setField(service, "senderClient", senderClient);
    }

    private AnnotazioneMetadata metadata() {
        AnnotazioneMetadata meta = new AnnotazioneMetadata(
                UUID.randomUUID(), "1.0", "mario", "descrizione");
        meta.setDataInserimento(LocalDateTime.now());
        meta.setStato(StatoAnnotazione.DAINVIARE.getValue());
        return meta;
    }

    @Test
    void inviaAnnotazioni_sendsEachPendingMetadataAndMarksItSent() {
        AnnotazioneMetadata meta = metadata();
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of(meta));

        List<AnnotazioneCompleta> inviate = service.inviaAnnotazioni();

        assertEquals(1, inviate.size());
        assertEquals(StatoAnnotazione.INVIATA.name(), meta.getStato());
        verify(senderClient).sendMessage(any(ServiceBusMessage.class));
        verify(metadataRepository).save(meta);
    }

    @Test
    void inviaAnnotazioni_sendsEveryPendingItem() {
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE))
                .thenReturn(List.of(metadata(), metadata(), metadata()));

        assertEquals(3, service.inviaAnnotazioni().size());
        verify(senderClient, times(3)).sendMessage(any(ServiceBusMessage.class));
    }

    @Test
    void inviaAnnotazioni_whenSendFails_marksTheMetadataAsError() {
        AnnotazioneMetadata meta = metadata();
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of(meta));
        doThrow(new RuntimeException("coda irraggiungibile"))
                .when(senderClient).sendMessage(any(ServiceBusMessage.class));

        service.inviaAnnotazioni();

        assertEquals(StatoAnnotazione.ERRORE.name(), meta.getStato());
        verify(metadataRepository).save(meta);
    }

    @Test
    void inviaAnnotazioni_whenDisabled_returnsEmptyWithoutTouchingTheRepository() {
        invioProperties.setEnabled(false);
        AzureProperties azureProperties = new AzureProperties();
        azureProperties.getServiceBus().setConnectionString(
                "Endpoint=sb://test.servicebus.windows.net/;SharedAccessKeyName=k;SharedAccessKey=v");
        AnnotazioneInvioServiceAzureImpl disabilitato =
                new AnnotazioneInvioServiceAzureImpl(metadataRepository, invioProperties, azureProperties);

        assertTrue(disabilitato.inviaAnnotazioni().isEmpty());
        assertFalse(disabilitato.isEnabled());
        verifyNoInteractions(metadataRepository);
    }

    @Test
    void inviaAnnotazioni_whenNothingPending_sendsNothing() {
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of());
        assertTrue(service.inviaAnnotazioni().isEmpty());
        verifyNoInteractions(senderClient);
    }
}
