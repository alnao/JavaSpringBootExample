package it.alnao.springbootexample.sqlite.service;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneStoricoStatiSQLiteEntity;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneStoricoStatiSQLiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneStoricoStatiServiceTest {

    @Mock AnnotazioneStoricoStatiSQLiteRepository storicoStatiRepository;
    @InjectMocks AnnotazioneStoricoStatiService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void inserisciCambioStato_savesAndReturnsDomain() {
        AnnotazioneStoricoStatiSQLiteEntity entity = new AnnotazioneStoricoStatiSQLiteEntity(
                "op-1", "ann-1", "1.0", "MODIFICATA", "INSERITA", "user",
                LocalDateTime.now(), "nota");
        when(storicoStatiRepository.save(any())).thenReturn(entity);

        AnnotazioneStoricoStati result = service.inserisciCambioStato(
                "ann-1", "1.0", "MODIFICATA", "INSERITA", "user", "nota");

        assertNotNull(result);
        assertEquals("ann-1", result.getIdAnnotazione());
        assertEquals("MODIFICATA", result.getStatoNew());
        assertEquals("INSERITA", result.getStatoOld());
        assertEquals("user", result.getUtente());
        verify(storicoStatiRepository).save(any());
    }

    @Test
    void trovaStoricoPerAnnotazione_returnsListMappedToDomain() {
        AnnotazioneStoricoStatiSQLiteEntity e1 = new AnnotazioneStoricoStatiSQLiteEntity(
                "op-1", "ann-1", "1.0", "MODIFICATA", "INSERITA", "user",
                LocalDateTime.now(), null);
        AnnotazioneStoricoStatiSQLiteEntity e2 = new AnnotazioneStoricoStatiSQLiteEntity(
                "op-2", "ann-1", "1.1", "CONFERMATA", "MODIFICATA", "admin",
                LocalDateTime.now(), null);
        when(storicoStatiRepository.findByIdAnnotazioneOrderByDataModificaDesc("ann-1"))
                .thenReturn(List.of(e1, e2));

        List<AnnotazioneStoricoStati> result = service.trovaStoricoPerAnnotazione("ann-1");

        assertEquals(2, result.size());
        assertEquals("MODIFICATA", result.get(0).getStatoNew());
        assertEquals("CONFERMATA", result.get(1).getStatoNew());
    }

    @Test
    void trovaStoricoPerAnnotazione_whenEmpty_returnsEmptyList() {
        when(storicoStatiRepository.findByIdAnnotazioneOrderByDataModificaDesc("ann-x"))
                .thenReturn(List.of());

        List<AnnotazioneStoricoStati> result = service.trovaStoricoPerAnnotazione("ann-x");

        assertTrue(result.isEmpty());
    }
}
