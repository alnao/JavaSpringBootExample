package it.alnao.springbootexample.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneImportProperties;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class KafkaAnnotazioneImportConsumerTest {

    @Mock
    private AnnotazioneMetadataRepository metadataRepository;

    @Mock
    private AnnotazioneRepository annotazioneRepository;

    @Mock
    private ObjectMapper objectMapper;

    private KafkaAnnotazioneImportConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaAnnotazioneImportConsumer(metadataRepository, annotazioneRepository, objectMapper, new AnnotazioneImportProperties());
    }

    @Test
    void importaAnnotazione_aggiornaEsistenteAImportata() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata incoming = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        AnnotazioneMetadata existing = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        existing.setStato(StatoAnnotazione.INVIATA.getValue());
        Annotazione annotazione = new Annotazione(id, "1.0", "nota");

        AnnotazioneCompleta payload = new AnnotazioneCompleta(annotazione, incoming);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(metadataRepository.findById(id)).thenReturn(Optional.of(existing));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());

        consumer.importaAnnotazione("json");

        verify(metadataRepository).save(existing);
        verify(annotazioneRepository).save(annotazione);
        assertEquals(StatoAnnotazione.IMPORTATA.getValue(), existing.getStato());
    }

    @Test
    void importaAnnotazione_generaNuovoIdSeAnnotazioneMongoEsiste() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata incoming = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        Annotazione annotazione = new Annotazione(id, "1.0", "nota");
        Annotazione existingMongo = new Annotazione(id, "1.0", "nota esistente");

        AnnotazioneCompleta payload = new AnnotazioneCompleta(annotazione, incoming);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(existingMongo));

        consumer.importaAnnotazione("json");

        // ID deve essere stato cambiato
        assertNotNull(annotazione.getId());
        assertNotNull(incoming.getId());
        assertEquals(annotazione.getId(), incoming.getId());
        // deve salvare con il nuovo ID (diverso dall'originale)
        verify(annotazioneRepository).save(annotazione);
    }

    @Test
    void importaAnnotazione_salvaNuovoMetadataImportato() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata incoming = new AnnotazioneMetadata(id, "1.0", "utente", "desc");
        incoming.setStato(StatoAnnotazione.INVIATA.getValue());
        Annotazione annotazione = new Annotazione(id, "1.0", "nota");

        AnnotazioneCompleta payload = new AnnotazioneCompleta(annotazione, incoming);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());

        consumer.importaAnnotazione("json");

        verify(metadataRepository).save(incoming);
        verify(annotazioneRepository).save(annotazione);
        assertEquals(StatoAnnotazione.IMPORTATA.getValue(), incoming.getStato());
    }

    @Test
    void importaAnnotazione_generaIdSeEntrambiNull() throws Exception {
        AnnotazioneMetadata metadata = new AnnotazioneMetadata();
        Annotazione annotazione = new Annotazione();
        annotazione.setId(null);
        metadata.setId(null);

        AnnotazioneCompleta payload = new AnnotazioneCompleta(annotazione, metadata);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(metadataRepository.findById(any())).thenReturn(Optional.empty());
        when(annotazioneRepository.findById(any())).thenReturn(Optional.empty());

        consumer.importaAnnotazione("json");

        assertNotNull(metadata.getId());
        assertNotNull(annotazione.getId());
        assertEquals(metadata.getId(), annotazione.getId());
        verify(metadataRepository).save(metadata);
        verify(annotazioneRepository).save(annotazione);
    }

    @Test
    void importaAnnotazione_usaIdMetadataSeAnnotazioneNull() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "utente", "desc");

        AnnotazioneCompleta payload = new AnnotazioneCompleta(null, metadata);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());

        consumer.importaAnnotazione("json");

        verify(metadataRepository).save(metadata);
        verify(annotazioneRepository).save(any());
    }

    @Test
    void importaAnnotazione_allineaIdAnnotazioneDiversoDaMetadata() throws Exception {
        UUID idMetadata = UUID.randomUUID();
        UUID idAnnotazione = UUID.randomUUID();
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(idMetadata, "1.0", "utente", "desc");
        Annotazione annotazione = new Annotazione(idAnnotazione, "1.0", "nota");

        AnnotazioneCompleta payload = new AnnotazioneCompleta(annotazione, metadata);

        when(objectMapper.readValue("json", AnnotazioneCompleta.class)).thenReturn(payload);
        when(metadataRepository.findById(idMetadata)).thenReturn(Optional.empty());
        when(annotazioneRepository.findById(idMetadata)).thenReturn(Optional.empty());

        consumer.importaAnnotazione("json");

        assertEquals(idMetadata, annotazione.getId());
        verify(metadataRepository).save(metadata);
        verify(annotazioneRepository).save(annotazione);
    }

    @Test
    void importaAnnotazione_propagaErroreSuJsonNonValido() throws Exception {
        when(objectMapper.readValue("json", AnnotazioneCompleta.class))
            .thenThrow(new RuntimeException("bad json"));

        assertThrows(RuntimeException.class, () -> consumer.importaAnnotazione("json"));

        verify(metadataRepository, never()).save(any());
    }
}
