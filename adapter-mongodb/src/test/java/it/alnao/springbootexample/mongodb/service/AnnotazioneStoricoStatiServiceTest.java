package it.alnao.springbootexample.mongodb.service;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
import it.alnao.springbootexample.mongodb.entity.AnnotazioneStoricoStatiEntity;
import it.alnao.springbootexample.mongodb.repository.AnnotazioneStoricoStatiMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneStoricoStatiServiceTest {

    @Mock AnnotazioneStoricoStatiMongoRepository storicoStatiRepository;
    @InjectMocks AnnotazioneStoricoStatiService service;

    private AnnotazioneStoricoStatiEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        entity = new AnnotazioneStoricoStatiEntity(
                "op-1", "ann-1", "1.0", "INVIATA", "DAINVIARE",
                "mario", LocalDateTime.of(2026, 2, 2, 10, 0), "invio automatico");
    }

    @Test
    void inserisciCambioStato_savesTheEntityAndMapsItBack() {
        when(storicoStatiRepository.save(any(AnnotazioneStoricoStatiEntity.class))).thenReturn(entity);

        AnnotazioneStoricoStati result = service.inserisciCambioStato(
                "ann-1", "1.0", "INVIATA", "DAINVIARE", "mario", "invio automatico");

        assertEquals("op-1", result.getIdOperazione());
        assertEquals("ann-1", result.getIdAnnotazione());
        assertEquals("1.0", result.getVersione());
        assertEquals("INVIATA", result.getStatoNew());
        assertEquals("DAINVIARE", result.getStatoOld());
        assertEquals("mario", result.getUtente());
        assertEquals("invio automatico", result.getNotaOperazione());
    }

    @Test
    void inserisciCambioStato_generatesAnOperationIdAndATimestamp() {
        when(storicoStatiRepository.save(any(AnnotazioneStoricoStatiEntity.class))).thenReturn(entity);

        service.inserisciCambioStato("ann-1", "1.0", "INVIATA", "DAINVIARE", "mario", "nota");

        ArgumentCaptor<AnnotazioneStoricoStatiEntity> captor =
                ArgumentCaptor.forClass(AnnotazioneStoricoStatiEntity.class);
        verify(storicoStatiRepository).save(captor.capture());
        AnnotazioneStoricoStatiEntity saved = captor.getValue();
        assertNotNull(saved.getIdOperazione());
        assertNotNull(saved.getDataModifica());
        assertEquals("ann-1", saved.getIdAnnotazione());
        assertEquals("INVIATA", saved.getStatoNew());
    }

    @Test
    void trovaStoricoPerAnnotazione_mapsEveryRowInOrder() {
        AnnotazioneStoricoStatiEntity secondo = new AnnotazioneStoricoStatiEntity(
                "op-2", "ann-1", "1.1", "CONFERMATA", "INVIATA",
                "luigi", LocalDateTime.of(2026, 3, 3, 10, 0), "conferma");
        when(storicoStatiRepository.findByIdAnnotazioneOrderByDataModificaDesc("ann-1"))
                .thenReturn(List.of(entity, secondo));

        List<AnnotazioneStoricoStati> result = service.trovaStoricoPerAnnotazione("ann-1");

        assertEquals(2, result.size());
        assertEquals("op-1", result.get(0).getIdOperazione());
        assertEquals("op-2", result.get(1).getIdOperazione());
    }

    @Test
    void trovaStoricoPerAnnotazione_whenNoRows_returnsEmptyList() {
        when(storicoStatiRepository.findByIdAnnotazioneOrderByDataModificaDesc("ann-999"))
                .thenReturn(List.of());
        assertTrue(service.trovaStoricoPerAnnotazione("ann-999").isEmpty());
    }
}
