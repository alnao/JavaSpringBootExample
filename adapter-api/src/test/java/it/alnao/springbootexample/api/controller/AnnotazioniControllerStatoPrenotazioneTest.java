package it.alnao.springbootexample.api.controller;

import it.alnao.springbootexample.api.dto.AggiornaAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.AnnotazioneResponse;
import it.alnao.springbootexample.api.dto.CambiaStatoAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.PrenotaAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.PrenotaAnnotazioneResponse;
import it.alnao.springbootexample.api.dto.TransizioneStatoResponse;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.exception.AnnotationLockedException;
import it.alnao.springbootexample.core.portService.AnnotazioniPortService;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copre aggiornamento, cambio stato, statistiche e l'intero flusso di prenotazione,
 * non verificati in {@link AnnotazioniControllerTest}.
 */
class AnnotazioniControllerStatoPrenotazioneTest {

    @Mock AnnotazioniPortService portService;
    @Mock AnnotazioneLockService lockService;

    private AnnotazioniController controller;
    private UUID id;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new AnnotazioniController(portService, lockService);
        ReflectionTestUtils.setField(controller, "lockNumeroSecondiDefault", 42);
        id = UUID.randomUUID();
    }

    private AnnotazioneCompleta completa() {
        Annotazione ann = new Annotazione(id, "1.0", "nota");
        AnnotazioneMetadata meta = new AnnotazioneMetadata();
        meta.setId(id);
        meta.setVersioneNota("1.0");
        meta.setUtenteCreazione("mario");
        meta.setDataInserimento(LocalDateTime.now());
        meta.setDataUltimaModifica(LocalDateTime.now());
        meta.setStato("INSERITA");
        return new AnnotazioneCompleta(ann, meta);
    }

    private AggiornaAnnotazioneRequest aggiornaRequest() {
        AggiornaAnnotazioneRequest req = new AggiornaAnnotazioneRequest();
        req.setValoreNota("nuovo valore");
        req.setDescrizione("nuova descrizione");
        req.setUtente("mario");
        return req;
    }

    // ---------- aggiornaAnnotazione ----------

    @Test
    void aggiornaAnnotazione_whenOk_returnsOk() {
        when(portService.aggiornaAnnotazione(any(AnnotazioneCompleta.class), anyString()))
                .thenReturn(completa());
        ResponseEntity<AnnotazioneResponse> response = controller.aggiornaAnnotazione(id, aggiornaRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void aggiornaAnnotazione_setsTheIdFromThePath() {
        AggiornaAnnotazioneRequest req = aggiornaRequest();
        when(portService.aggiornaAnnotazione(any(AnnotazioneCompleta.class), anyString()))
                .thenReturn(completa());
        controller.aggiornaAnnotazione(id, req);
        assertEquals(id, req.getId());
    }

    @Test
    void aggiornaAnnotazione_whenLocked_returnsConflict() {
        when(portService.aggiornaAnnotazione(any(AnnotazioneCompleta.class), anyString()))
                .thenThrow(new AnnotationLockedException(id, "luigi"));
        ResponseEntity<AnnotazioneResponse> response = controller.aggiornaAnnotazione(id, aggiornaRequest());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ---------- ottieniAnnotazioniPerStato ----------

    @Test
    void ottieniAnnotazioniPerStato_whenStatoIsValid_returnsOk() {
        when(portService.trovaPerStato(StatoAnnotazione.INSERITA)).thenReturn(List.of(completa()));
        ResponseEntity<List<AnnotazioneResponse>> response = controller.ottieniAnnotazioniPerStato("inserita");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void ottieniAnnotazioniPerStato_whenStatoIsUnknown_returnsBadRequest() {
        ResponseEntity<List<AnnotazioneResponse>> response = controller.ottieniAnnotazioniPerStato("STATO_INESISTENTE");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(portService, never()).trovaPerStato(any());
    }

    // ---------- ottieniStatistiche ----------

    @Test
    void ottieniStatistiche_returnsTheCountAndAGenerationDate() {
        when(portService.contaAnnotazioni()).thenReturn(7L);
        ResponseEntity<AnnotazioniController.StatisticheResponse> response = controller.ottieniStatistiche();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7L, response.getBody().getTotaleAnnotazioni());
        assertNotNull(response.getBody().getDataGenerazione());
    }

    // ---------- ottieniTransizioniStato ----------

    @Test
    void ottieniTransizioniStato_whenServiceFails_returnsInternalServerError() {
        when(portService.listaCambiamentiStati()).thenThrow(new RuntimeException("errore"));
        ResponseEntity<List<TransizioneStatoResponse>> response = controller.ottieniTransizioniStato();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void ottieniTransizioniStato_mapsTheDomainTransitions() {
        when(portService.listaCambiamentiStati()).thenReturn(List.of(new TransizioneStato(
                StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, UserRole.USER, "modifica")));
        ResponseEntity<List<TransizioneStatoResponse>> response = controller.ottieniTransizioniStato();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("INSERITA", response.getBody().get(0).getStatoPartenza());
        assertEquals("MODIFICATA", response.getBody().get(0).getStatoArrivo());
    }

    // ---------- cambiaStato ----------

    @Test
    void cambiaStato_whenOk_returnsOk() {
        when(portService.cambiaStato(eq(id), eq(StatoAnnotazione.INSERITA),
                eq(StatoAnnotazione.CONFERMATA), anyString())).thenReturn(completa());
        ResponseEntity<AnnotazioneResponse> response = controller.cambiaStato(
                id, new CambiaStatoAnnotazioneRequest("INSERITA", "CONFERMATA", "admin"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void cambiaStato_withAnUnknownStato_returnsBadRequest() {
        ResponseEntity<AnnotazioneResponse> response = controller.cambiaStato(
                id, new CambiaStatoAnnotazioneRequest("NON_ESISTE", "CONFERMATA", "admin"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void cambiaStato_whenTransitionIsNotAllowed_returnsForbidden() {
        when(portService.cambiaStato(any(), any(), any(), anyString()))
                .thenThrow(new IllegalStateException("transizione non permessa"));
        ResponseEntity<AnnotazioneResponse> response = controller.cambiaStato(
                id, new CambiaStatoAnnotazioneRequest("INSERITA", "CONFERMATA", "mario"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void cambiaStato_whenServiceFails_returnsInternalServerError() {
        when(portService.cambiaStato(any(), any(), any(), anyString()))
                .thenThrow(new RuntimeException("errore generico"));
        ResponseEntity<AnnotazioneResponse> response = controller.cambiaStato(
                id, new CambiaStatoAnnotazioneRequest("INSERITA", "CONFERMATA", "mario"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ---------- prenotaAnnotazione ----------

    @Test
    void prenotaAnnotazione_whenAnnotazioneIsMissing_returnsNotFound() {
        when(portService.trovaPerID(id)).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.prenotaAnnotazione(id, new PrenotaAnnotazioneRequest("mario", null));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(lockService, never()).acquireLock(any(), anyString(), anyInt());
    }

    @Test
    void prenotaAnnotazione_whenFree_acquiresTheLockForTheRequestedSeconds() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(false);
        when(lockService.acquireLock(id, "mario", 100)).thenReturn(true);

        ResponseEntity<?> response = controller.prenotaAnnotazione(id, new PrenotaAnnotazioneRequest("mario", 100));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PrenotaAnnotazioneResponse body = (PrenotaAnnotazioneResponse) response.getBody();
        assertTrue(body.isPrenotataConSuccesso());
        assertEquals("mario", body.getUtente());
        verify(lockService).acquireLock(id, "mario", 100);
    }

    @Test
    void prenotaAnnotazione_withoutSeconds_usesTheConfiguredDefault() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(false);
        when(lockService.acquireLock(id, "mario", 42)).thenReturn(true);

        ResponseEntity<?> response = controller.prenotaAnnotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(lockService).acquireLock(id, "mario", 42);
    }

    @Test
    void prenotaAnnotazione_whenAlreadyLockedBySameUser_returnsOk() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("mario"));

        ResponseEntity<?> response = controller.prenotaAnnotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PrenotaAnnotazioneResponse body = (PrenotaAnnotazioneResponse) response.getBody();
        assertTrue(body.isPrenotataConSuccesso());
        assertEquals("Annotazione già prenotata da te", body.getMessaggio());
        verify(lockService, never()).acquireLock(any(), anyString(), anyInt());
    }

    @Test
    void prenotaAnnotazione_whenLockedByAnotherUser_returnsConflict() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("luigi"));

        ResponseEntity<?> response = controller.prenotaAnnotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(lockService, never()).acquireLock(any(), anyString(), anyInt());
    }

    @Test
    void prenotaAnnotazione_whenOwnerIsUnknown_returnsConflict() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.prenotaAnnotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void prenotaAnnotazione_whenLockCannotBeAcquired_returnsConflict() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(false);
        when(lockService.acquireLock(id, "mario", 42)).thenReturn(false);

        ResponseEntity<?> response = controller.prenotaAnnotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ---------- rilasciaPrenotazione ----------

    @Test
    void rilasciaPrenotazione_whenAnnotazioneIsMissing_returnsNotFound() {
        when(portService.trovaPerID(id)).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.rilasciaPrenotazione(id, new PrenotaAnnotazioneRequest("mario", null));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(lockService, never()).releaseLock(any(), anyString());
    }

    @Test
    void rilasciaPrenotazione_whenOwner_releasesTheLock() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("mario"));

        ResponseEntity<?> response = controller.rilasciaPrenotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(lockService).releaseLock(id, "mario");
    }

    @Test
    void rilasciaPrenotazione_whenNotOwner_returnsConflict() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("luigi"));

        ResponseEntity<?> response = controller.rilasciaPrenotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(lockService, never()).releaseLock(any(), anyString());
    }

    @Test
    void rilasciaPrenotazione_whenNotLocked_releasesAnyway() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(false);

        ResponseEntity<?> response = controller.rilasciaPrenotazione(id, new PrenotaAnnotazioneRequest("mario", null));

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(lockService).releaseLock(id, "mario");
    }

    // ---------- verificaStatoPrenotazione ----------

    @Test
    void verificaStatoPrenotazione_whenAnnotazioneIsMissing_returnsNotFound() {
        when(portService.trovaPerID(id)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.verificaStatoPrenotazione(id).getStatusCode());
    }

    @Test
    void verificaStatoPrenotazione_whenLocked_returnsOwner() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("mario"));

        ResponseEntity<?> response = controller.verificaStatoPrenotazione(id);
        AnnotazioniController.StatoPrenotazioneResponse body =
                (AnnotazioniController.StatoPrenotazioneResponse) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(body.isPrenotata());
        assertEquals("mario", body.getUtenteProprietario());
        assertEquals(id, body.getAnnotazioneId());
    }

    @Test
    void verificaStatoPrenotazione_whenFree_returnsNoOwner() {
        when(portService.trovaPerID(id)).thenReturn(Optional.of(completa()));
        when(lockService.isLocked(id)).thenReturn(false);
        when(lockService.getOwner(id)).thenReturn(Optional.empty());

        AnnotazioniController.StatoPrenotazioneResponse body =
                (AnnotazioniController.StatoPrenotazioneResponse) controller.verificaStatoPrenotazione(id).getBody();

        assertFalse(body.isPrenotata());
        assertNull(body.getUtenteProprietario());
    }

    // ---------- classi di risposta annidate ----------

    @Test
    void statoPrenotazioneResponse_settersAndGettersWork() {
        AnnotazioniController.StatoPrenotazioneResponse r =
                new AnnotazioniController.StatoPrenotazioneResponse(id, true, "mario");
        UUID altro = UUID.randomUUID();
        r.setAnnotazioneId(altro);
        r.setPrenotata(false);
        r.setUtenteProprietario("luigi");
        assertEquals(altro, r.getAnnotazioneId());
        assertFalse(r.isPrenotata());
        assertEquals("luigi", r.getUtenteProprietario());
    }

    @Test
    void statisticheResponse_settersAndGettersWork() {
        AnnotazioniController.StatisticheResponse r = new AnnotazioniController.StatisticheResponse();
        LocalDateTime now = LocalDateTime.now();
        r.setTotaleAnnotazioni(15L);
        r.setDataGenerazione(now);
        assertEquals(15L, r.getTotaleAnnotazioni());
        assertEquals(now, r.getDataGenerazione());
    }
}
