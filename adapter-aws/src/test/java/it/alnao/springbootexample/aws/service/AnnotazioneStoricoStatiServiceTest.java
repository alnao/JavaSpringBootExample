package it.alnao.springbootexample.aws.service;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoStatiDynamoEntity;
import it.alnao.springbootexample.aws.repository.AnnotazioneStoricoStatiDynamoRepository;
import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
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

    @Mock AnnotazioneStoricoStatiDynamoRepository storicoStatiRepository;
    @InjectMocks AnnotazioneStoricoStatiService service;

    private AnnotazioneStoricoStatiDynamoEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        entity = new AnnotazioneStoricoStatiDynamoEntity(
                "op-1", "ann-1", "1.0", "INVIATA", "DAINVIARE",
                "mario", "2026-02-02T10:00:00", "invio automatico");
    }

    @Test
    void inserisciCambioStato_savesTheEntityAndMapsItBack() {
        when(storicoStatiRepository.save(any(AnnotazioneStoricoStatiDynamoEntity.class))).thenReturn(entity);

        AnnotazioneStoricoStati result = service.inserisciCambioStato(
                "ann-1", "1.0", "INVIATA", "DAINVIARE", "mario", "invio automatico");

        assertEquals("op-1", result.getIdOperazione());
        assertEquals("ann-1", result.getIdAnnotazione());
        assertEquals("INVIATA", result.getStatoNew());
        assertEquals("DAINVIARE", result.getStatoOld());
        assertEquals(LocalDateTime.of(2026, 2, 2, 10, 0), result.getDataModifica());
    }

    @Test
    void inserisciCambioStato_generatesAnOperationIdAndAnIsoTimestamp() {
        when(storicoStatiRepository.save(any(AnnotazioneStoricoStatiDynamoEntity.class))).thenReturn(entity);

        service.inserisciCambioStato("ann-1", "1.0", "INVIATA", "DAINVIARE", "mario", "nota");

        ArgumentCaptor<AnnotazioneStoricoStatiDynamoEntity> captor =
                ArgumentCaptor.forClass(AnnotazioneStoricoStatiDynamoEntity.class);
        verify(storicoStatiRepository).save(captor.capture());
        assertNotNull(captor.getValue().getIdOperazione());
        assertDoesNotThrow(() -> LocalDateTime.parse(captor.getValue().getDataModifica()));
    }

    @Test
    void entityToDomain_whenTheDateIsUnparsable_fallsBackToNow() {
        AnnotazioneStoricoStatiDynamoEntity rotta = new AnnotazioneStoricoStatiDynamoEntity(
                "op-2", "ann-1", "1.0", "INVIATA", "DAINVIARE", "mario", "data-non-valida", "nota");
        when(storicoStatiRepository.save(any(AnnotazioneStoricoStatiDynamoEntity.class))).thenReturn(rotta);

        AnnotazioneStoricoStati result = service.inserisciCambioStato(
                "ann-1", "1.0", "INVIATA", "DAINVIARE", "mario", "nota");

        assertNotNull(result.getDataModifica());
    }

    @Test
    void trovaStoricoPerAnnotazione_mapsEveryRow() {
        when(storicoStatiRepository.findByIdAnnotazione("ann-1")).thenReturn(List.of(entity, entity));
        assertEquals(2, service.trovaStoricoPerAnnotazione("ann-1").size());
    }

    @Test
    void trovaStoricoPerAnnotazione_whenNoRows_returnsEmptyList() {
        when(storicoStatiRepository.findByIdAnnotazione("ann-999")).thenReturn(List.of());
        assertTrue(service.trovaStoricoPerAnnotazione("ann-999").isEmpty());
    }
}
