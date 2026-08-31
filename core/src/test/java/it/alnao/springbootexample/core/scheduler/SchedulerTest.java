package it.alnao.springbootexample.core.scheduler;

import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.service.AnnotazioneImportService;
import it.alnao.springbootexample.core.service.AnnotazioneInvioService;
import it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SchedulerTest {

    @Mock AnnotazioneInvioService invioService;
    @Mock AnnotazioneImportService importService;
    @Mock AnnotazioneStoricoStatiService storicoStatiService;

    private AnnotazioneInvioScheduler invioScheduler;
    private AnnotazioneImportScheduler importScheduler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        invioScheduler = new AnnotazioneInvioScheduler(
                invioService, storicoStatiService, new AnnotazioneInvioProperties());
        importScheduler = new AnnotazioneImportScheduler(importService);
    }

    private AnnotazioneCompleta completa() {
        UUID id = UUID.randomUUID();
        Annotazione ann = new Annotazione(id, "1.0", "nota");
        AnnotazioneMetadata meta = new AnnotazioneMetadata(id, "1.0", "mario", "descr");
        meta.setDataInserimento(LocalDateTime.now());
        meta.setStato(StatoAnnotazione.DAINVIARE.getValue());
        return new AnnotazioneCompleta(ann, meta);
    }

    // ---------- AnnotazioneInvioScheduler ----------

    @Test
    void inviaAnnotazioni_whenServiceIsDisabled_doesNothing() {
        when(invioService.isEnabled()).thenReturn(false);
        invioScheduler.inviaAnnotazioni();
        verify(invioService, never()).inviaAnnotazioni();
        verifyNoInteractions(storicoStatiService);
    }

    @Test
    void inviaAnnotazioni_whenNothingIsSent_writesNoHistory() {
        when(invioService.isEnabled()).thenReturn(true);
        when(invioService.inviaAnnotazioni()).thenReturn(List.of());
        invioScheduler.inviaAnnotazioni();
        verifyNoInteractions(storicoStatiService);
    }

    @Test
    void inviaAnnotazioni_writesOneHistoryRowPerSentAnnotazione() {
        AnnotazioneCompleta a = completa();
        AnnotazioneCompleta b = completa();
        when(invioService.isEnabled()).thenReturn(true);
        when(invioService.inviaAnnotazioni()).thenReturn(List.of(a, b));

        invioScheduler.inviaAnnotazioni();

        verify(storicoStatiService, times(2)).inserisciCambioStato(
                anyString(), anyString(), eq(StatoAnnotazione.INVIATA.getValue()),
                anyString(), eq("CRON Scheduler"), anyString());
    }

    @Test
    void inviaAnnotazioni_whenTheServiceThrows_swallowsTheError() {
        when(invioService.isEnabled()).thenReturn(true);
        when(invioService.inviaAnnotazioni()).thenThrow(new RuntimeException("coda irraggiungibile"));
        assertDoesNotThrow(() -> invioScheduler.inviaAnnotazioni());
    }

    @Test
    void inviaAnnotazioni_whenHistoryWriteFails_swallowsTheError() {
        when(invioService.isEnabled()).thenReturn(true);
        when(invioService.inviaAnnotazioni()).thenReturn(List.of(completa()));
        doThrow(new RuntimeException("db ko")).when(storicoStatiService).inserisciCambioStato(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        assertDoesNotThrow(() -> invioScheduler.inviaAnnotazioni());
    }

    // ---------- AnnotazioneImportScheduler ----------

    @Test
    void importaAnnotazioni_whenServiceIsDisabled_doesNothing() {
        when(importService.isEnabled()).thenReturn(false);
        importScheduler.importaAnnotazioni();
        verify(importService, never()).importaAnnotazioni();
    }

    @Test
    void importaAnnotazioni_whenEnabled_runsTheImport() {
        when(importService.isEnabled()).thenReturn(true);
        when(importService.importaAnnotazioni()).thenReturn(List.of(completa()));
        importScheduler.importaAnnotazioni();
        verify(importService).importaAnnotazioni();
    }

    @Test
    void importaAnnotazioni_whenTheServiceThrows_swallowsTheError() {
        when(importService.isEnabled()).thenReturn(true);
        when(importService.importaAnnotazioni()).thenThrow(new RuntimeException("coda ko"));
        assertDoesNotThrow(() -> importScheduler.importaAnnotazioni());
    }
}
