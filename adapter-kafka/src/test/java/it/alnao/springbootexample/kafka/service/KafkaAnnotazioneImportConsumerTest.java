package it.alnao.springbootexample.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaAnnotazioneImportConsumerTest {

    @Mock
    private AnnotazioneMetadataRepository metadataRepository;

    @Mock
    private ObjectMapper objectMapper;

    private KafkaAnnotazioneImportConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaAnnotazioneImportConsumer(metadataRepository, objectMapper);
    }

    @Test
    void importaAnnotazione_aggiornaEsistenteAImportata() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata incoming = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        AnnotazioneMetadata existing = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        existing.setStato(StatoAnnotazione.INVIATA.getValue());

        AnnotazioneCompleta payload = new AnnotazioneCompleta();
        payload.setMetadata(incoming);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(metadataRepository.findById(id)).thenReturn(Optional.of(existing));

        consumer.importaAnnotazione("json");

        verify(metadataRepository).save(existing);
        org.junit.jupiter.api.Assertions.assertEquals(StatoAnnotazione.IMPORTATA.getValue(), existing.getStato());
    }

    @Test
    void importaAnnotazione_salvaNuovoMetadataImportato() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata incoming = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        incoming.setStato(StatoAnnotazione.INVIATA.getValue());

        AnnotazioneCompleta payload = new AnnotazioneCompleta();
        payload.setMetadata(incoming);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        consumer.importaAnnotazione("json");

        verify(metadataRepository).save(incoming);
        org.junit.jupiter.api.Assertions.assertEquals(StatoAnnotazione.IMPORTATA.getValue(), incoming.getStato());
    }

    @Test
    void importaAnnotazione_nonSalvaSeMetadataMancanti() throws Exception {
        AnnotazioneCompleta payload = new AnnotazioneCompleta();
        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);

        consumer.importaAnnotazione("json");

        verify(metadataRepository, never()).save(any());
    }

    @Test
    void importaAnnotazione_propagaErroreSuJsonNonValido() throws Exception {
        when(objectMapper.readValue("json", AnnotazioneCompleta.class))
            .thenThrow(new RuntimeException("bad json"));

        assertThrows(RuntimeException.class, () -> consumer.importaAnnotazione("json"));

        verify(metadataRepository, never()).save(any());
    }
}
