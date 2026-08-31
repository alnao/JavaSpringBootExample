package it.alnao.springbootexample.azure.service;

import it.alnao.springbootexample.azure.entity.AnnotazioneStoricoStatiCosmosEntity;
import it.alnao.springbootexample.azure.repository.AnnotazioneStoricoStatiCosmosRepository;
import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
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

class AnnotazioneStoricoStatiServiceAzureImplTest {

    @Mock AnnotazioneStoricoStatiCosmosRepository storicoRepository;
    @InjectMocks AnnotazioneStoricoStatiServiceAzureImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void inserisciCambioStato_savesAndReturnsDomain() {
        AnnotazioneStoricoStatiCosmosEntity entity = buildEntity("ann-1", "MODIFICATA", "INSERITA");
        when(storicoRepository.save(any())).thenReturn(entity);

        AnnotazioneStoricoStati result = service.inserisciCambioStato(
                "ann-1", "1.0", "MODIFICATA", "INSERITA", "user", "nota");

        assertNotNull(result);
        assertEquals("ann-1", result.getIdAnnotazione());
        assertEquals("MODIFICATA", result.getStatoNew());
        assertEquals("INSERITA", result.getStatoOld());
        verify(storicoRepository).save(any());
    }

    @Test
    void trovaStoricoPerAnnotazione_returnsListMappedToDomain() {
        AnnotazioneStoricoStatiCosmosEntity e1 = buildEntity("ann-1", "MODIFICATA", "INSERITA");
        AnnotazioneStoricoStatiCosmosEntity e2 = buildEntity("ann-1", "CONFERMATA", "MODIFICATA");
        when(storicoRepository.findByIdAnnotazioneOrderByDataCambioDesc("ann-1"))
                .thenReturn(List.of(e1, e2));

        List<AnnotazioneStoricoStati> result = service.trovaStoricoPerAnnotazione("ann-1");

        assertEquals(2, result.size());
        assertEquals("MODIFICATA", result.get(0).getStatoNew());
        assertEquals("CONFERMATA", result.get(1).getStatoNew());
    }

    @Test
    void trovaStoricoPerAnnotazione_whenEmpty_returnsEmptyList() {
        when(storicoRepository.findByIdAnnotazioneOrderByDataCambioDesc("ann-x"))
                .thenReturn(List.of());

        List<AnnotazioneStoricoStati> result = service.trovaStoricoPerAnnotazione("ann-x");

        assertTrue(result.isEmpty());
    }

    private AnnotazioneStoricoStatiCosmosEntity buildEntity(String idAnn, String statoNew, String statoOld) {
        AnnotazioneStoricoStatiCosmosEntity e = new AnnotazioneStoricoStatiCosmosEntity();
        e.setId(java.util.UUID.randomUUID().toString());
        e.setIdAnnotazione(idAnn);
        e.setVersione("1.0");
        e.setStatoNew(statoNew);
        e.setStatoOld(statoOld);
        e.setUtente("user");
        e.setDataCambio(LocalDateTime.now());
        return e;
    }
}
