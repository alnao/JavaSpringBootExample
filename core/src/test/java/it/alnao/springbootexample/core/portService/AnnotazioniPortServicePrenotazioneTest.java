package it.alnao.springbootexample.core.portService;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.exception.AnnotationLockedException;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import it.alnao.springbootexample.core.service.AnnotazioneService;
import it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService;
import it.alnao.springbootexample.core.service.ValidatoreTransizioniStatoService;
import it.alnao.springbootexample.core.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Fissa cosa succede alla prenotazione durante e dopo un aggiornamento:
 * la verifica è di sola lettura, non acquisisce né rilascia nulla, così una
 * prenotazione esplicita sopravvive al salvataggio su tutti i profili.
 */
@ExtendWith(MockitoExtension.class)
class AnnotazioniPortServicePrenotazioneTest {

    @Mock private AnnotazioneService annotazioneService;
    @Mock private UserService userService;
    @Mock private ValidatoreTransizioniStatoService validatoreTransizioniStatoService;
    @Mock private AnnotazioneStoricoStatiService annotazioneStoricoStatiService;
    @Mock private AnnotazioneLockService lockService;

    @InjectMocks private AnnotazioniPortServiceImpl portService;

    private UUID id;
    private AnnotazioneCompleta annotazioneCompleta;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        Annotazione annotazione = new Annotazione(id, "1.0", "testo");
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "luigi", "descrizione");
        annotazioneCompleta = new AnnotazioneCompleta(annotazione, metadata);
    }

    private void utenteConRuolo(String username, UserRole ruolo) {
        User user = new User();
        user.setRole(ruolo);
        lenient().when(userService.findByUsername(username)).thenReturn(Optional.of(user));
    }

    private void aggiornamentoRiuscito() {
        lenient().when(annotazioneService.aggiornaAnnotazione(eq(id), anyString(), anyString(), anyString()))
                .thenReturn(annotazioneCompleta);
        lenient().when(annotazioneService.trovaPerID(id)).thenReturn(Optional.of(annotazioneCompleta));
    }

    // ---- La prenotazione sopravvive alla modifica ----

    @Test
    void aggiornamentoDalProprietario_nonRilasciaLaPrenotazione() {
        utenteConRuolo("luigi", UserRole.USER);
        aggiornamentoRiuscito();
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("luigi"));

        portService.aggiornaAnnotazione(annotazioneCompleta, "luigi");

        verify(lockService, never()).releaseLock(any(), anyString());
    }

    @Test
    void aggiornamento_nonAcquisisceMaiUnaPrenotazione() {
        utenteConRuolo("luigi", UserRole.USER);
        aggiornamentoRiuscito();
        when(lockService.isLocked(id)).thenReturn(false);

        portService.aggiornaAnnotazione(annotazioneCompleta, "luigi");

        verify(lockService, never()).acquireLock(any(), anyString(), anyLong());
        verify(lockService, never()).releaseLock(any(), anyString());
    }

    @Test
    void dueAggiornamentiConsecutivi_conUnaSolaPrenotazione_vannoEntrambiABuonFine() {
        utenteConRuolo("luigi", UserRole.USER);
        aggiornamentoRiuscito();
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("luigi"));

        assertDoesNotThrow(() -> portService.aggiornaAnnotazione(annotazioneCompleta, "luigi"));
        assertDoesNotThrow(() -> portService.aggiornaAnnotazione(annotazioneCompleta, "luigi"));

        verify(annotazioneService, times(2)).aggiornaAnnotazione(eq(id), anyString(), anyString(), eq("luigi"));
        verify(lockService, never()).releaseLock(any(), anyString());
    }

    // ---- Contesa ----

    @Test
    void aggiornamentoDaUtenteDiverso_vieneRifiutatoSenzaToccareLaPrenotazione() {
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("luigi"));

        AnnotationLockedException e = assertThrows(AnnotationLockedException.class,
                () -> portService.aggiornaAnnotazione(annotazioneCompleta, "mario"));

        assertEquals("luigi", e.getCurrentOwner());
        verify(annotazioneService, never()).aggiornaAnnotazione(any(), anyString(), anyString(), anyString());
        verify(lockService, never()).releaseLock(any(), anyString());
    }
}
