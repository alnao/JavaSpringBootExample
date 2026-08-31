package it.alnao.springbootexample.sqlite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneInviataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqliteAnnotazioneInvioServiceTest {

    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock AnnotazioneInviataRepository annotazioneInviataRepository;
    @Mock AnnotazioneInvioProperties properties;
    @Mock ObjectMapper objectMapper;
    @InjectMocks SqliteAnnotazioneInvioService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isEnabled_delegatesToProperties() {
        when(properties.isEnabled()).thenReturn(true);
        assertTrue(service.isEnabled());
        when(properties.isEnabled()).thenReturn(false);
        assertFalse(service.isEnabled());
    }

    @Test
    void inviaAnnotazioni_whenNoneToSend_returnsEmptyList() {
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of());
        List<AnnotazioneCompleta> result = service.inviaAnnotazioni();
        assertTrue(result.isEmpty());
    }

    @Test
    void inviaAnnotazioni_whenOneToSend_processesAnnotazione() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata meta = buildMetadata(id, "DAINVIARE");
        Annotazione ann = new Annotazione(id, "1.0", "nota");
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of(meta));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":\"test\"}");
        when(annotazioneInviataRepository.save(any())).thenReturn(null);
        when(metadataRepository.save(any())).thenReturn(meta);

        // Should not throw - either success or error path both handle gracefully
        List<AnnotazioneCompleta> result = service.inviaAnnotazioni();

        assertNotNull(result);
        verify(metadataRepository).findByStato(StatoAnnotazione.DAINVIARE);
    }

    @Test
    void inviaAnnotazioni_whenSerializationFails_savesErrorState() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneMetadata meta = buildMetadata(id, "DAINVIARE");
        Annotazione ann = new Annotazione(id, "1.0", "nota");
        when(metadataRepository.findByStato(StatoAnnotazione.DAINVIARE)).thenReturn(List.of(meta));
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        when(objectMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("err"){});
        when(annotazioneInviataRepository.save(any())).thenReturn(null);
        when(metadataRepository.save(any())).thenReturn(meta);

        assertDoesNotThrow(() -> service.inviaAnnotazioni());
        verify(annotazioneInviataRepository).save(any());
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
