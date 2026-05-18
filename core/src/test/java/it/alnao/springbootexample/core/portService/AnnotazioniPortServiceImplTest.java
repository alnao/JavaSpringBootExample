package it.alnao.springbootexample.core.portService;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.exception.AnnotationLockedException;
import it.alnao.springbootexample.core.service.AnnotazioneService;
import it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import it.alnao.springbootexample.core.service.ValidatoreTransizioniStatoService;
import it.alnao.springbootexample.core.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnotazioniPortServiceImplTest {

    @Mock
    private AnnotazioneService annotazioneService;
    @Mock
    private UserService userService;
    @Mock
    private ValidatoreTransizioniStatoService validatoreTransizioniStatoService;
    @Mock
    private AnnotazioneStoricoStatiService annotazioneStoricoStatiService;
    @Mock
    private AnnotazioneLockService lockService;

    @InjectMocks
    private AnnotazioniPortServiceImpl portService;

    private UUID id;
    private User adminUser;
    private AnnotazioneCompleta annotazioneCompleta;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        adminUser = new User();
        adminUser.setRole(UserRole.ADMIN);

        Annotazione annotazione = new Annotazione(id, "1.0", "testo");
        AnnotazioneMetadata metadata = new AnnotazioneMetadata(id, "1.0", "utente", "descrizione");
        metadata.setCategoria("cat");
        metadata.setTags("tag1");
        metadata.setPubblica(true);
        metadata.setPriorita(2);
        annotazioneCompleta = new AnnotazioneCompleta(annotazione, metadata);
    }

    // ---- trovaTutte ----

    @Test
    void trovaTutte_delegaAService() {
        when(annotazioneService.trovaTutte()).thenReturn(Collections.emptyList());
        List<AnnotazioneCompleta> result = portService.trovaTutte();
        assertNotNull(result);
        verify(annotazioneService).trovaTutte();
    }

    // ---- trovaPerID ----

    @Test
    void trovaPerID_delegaAService() {
        when(annotazioneService.trovaPerID(id)).thenReturn(Optional.of(annotazioneCompleta));
        Optional<AnnotazioneCompleta> result = portService.trovaPerID(id);
        assertTrue(result.isPresent());
        verify(annotazioneService).trovaPerID(id);
    }

    @Test
    void trovaPerID_nonTrovato_ritornaEmpty() {
        when(annotazioneService.trovaPerID(id)).thenReturn(Optional.empty());
        Optional<AnnotazioneCompleta> result = portService.trovaPerID(id);
        assertTrue(result.isEmpty());
    }

    // ---- eliminaAnnotazione ----

    @Test
    void eliminaAnnotazione_delegaAService() {
        portService.eliminaAnnotazione(id);
        verify(annotazioneService).eliminaAnnotazione(id);
    }

    // ---- cercaPerTesto ----

    @Test
    void cercaPerTesto_delegaAService() {
        when(annotazioneService.cercaPerTesto("testo")).thenReturn(Collections.emptyList());
        portService.cercaPerTesto("testo");
        verify(annotazioneService).cercaPerTesto("testo");
    }

    // ---- trovaPerUtente ----

    @Test
    void trovaPerUtente_delegaAService() {
        when(annotazioneService.trovaPerUtente("utente")).thenReturn(Collections.emptyList());
        portService.trovaPerUtente("utente");
        verify(annotazioneService).trovaPerUtente("utente");
    }

    // ---- trovaPerCategoria ----

    @Test
    void trovaPerCategoria_delegaAService() {
        when(annotazioneService.trovaPerCategoria("cat")).thenReturn(Collections.emptyList());
        portService.trovaPerCategoria("cat");
        verify(annotazioneService).trovaPerCategoria("cat");
    }

    // ---- trovaPubbliche ----

    @Test
    void trovaPubbliche_delegaAService() {
        when(annotazioneService.trovaPubbliche()).thenReturn(Collections.emptyList());
        portService.trovaPubbliche();
        verify(annotazioneService).trovaPubbliche();
    }

    // ---- trovaPerStato ----

    @Test
    void trovaPerStato_delegaAService() {
        when(annotazioneService.trovaPerStato(StatoAnnotazione.INSERITA)).thenReturn(Collections.emptyList());
        portService.trovaPerStato(StatoAnnotazione.INSERITA);
        verify(annotazioneService).trovaPerStato(StatoAnnotazione.INSERITA);
    }

    // ---- contaAnnotazioni ----

    @Test
    void contaAnnotazioni_delegaAService() {
        when(annotazioneService.contaAnnotazioni()).thenReturn(5L);
        assertEquals(5L, portService.contaAnnotazioni());
    }

    // ---- listaCambiamentiStati ----

    @Test
    void listaCambiamentiStati_delegaAValidatore() {
        when(validatoreTransizioniStatoService.getTutteLeTransizioni()).thenReturn(Collections.emptyList());
        portService.listaCambiamentiStati();
        verify(validatoreTransizioniStatoService).getTutteLeTransizioni();
    }

    // ---- creaAnnotazione ----

    @Test
    void creaAnnotazione_senzaOpzionali_delegaAService() {
        Annotazione ann = new Annotazione(id, "1.0", "valore");
        AnnotazioneMetadata meta = new AnnotazioneMetadata();
        meta.setDescrizione("desc");
        // categoria, tags, pubblica, priorita null
        AnnotazioneCompleta input = new AnnotazioneCompleta(ann, meta);

        when(annotazioneService.creaAnnotazione("valore", "desc", "utente"))
                .thenReturn(annotazioneCompleta);
        when(annotazioneService.trovaPerID(any())).thenReturn(Optional.of(annotazioneCompleta));

        AnnotazioneCompleta result = portService.creaAnnotazione(input, "utente");
        assertNotNull(result);
        verify(annotazioneService).creaAnnotazione("valore", "desc", "utente");
        verify(annotazioneService, never()).impostaCategoria(any(), any(), any());
        verify(annotazioneService, never()).impostaTags(any(), any(), any());
    }

    @Test
    void creaAnnotazione_conTuttiOPzionali_chiamaTuttiIMetodi() {
        when(annotazioneService.creaAnnotazione(anyString(), anyString(), anyString()))
                .thenReturn(annotazioneCompleta);
        when(annotazioneService.trovaPerID(any())).thenReturn(Optional.of(annotazioneCompleta));

        portService.creaAnnotazione(annotazioneCompleta, "utente");

        verify(annotazioneService).impostaCategoria(any(), eq("cat"), eq("utente"));
        verify(annotazioneService).impostaTags(any(), eq("tag1"), eq("utente"));
        verify(annotazioneService).impostaVisibilitaPubblica(any(), eq(true), eq("utente"));
        verify(annotazioneService).impostaPriorita(any(), eq(2), eq("utente"));
    }

    // ---- aggiornaAnnotazione ----

    @Test
    void aggiornaAnnotazione_lockNonPresente_aggiornaCorrettamente() {
        when(lockService.isLocked(id)).thenReturn(false);
        when(userService.findByUsername("utente")).thenReturn(Optional.of(adminUser));
        when(annotazioneService.trovaPerID(id)).thenReturn(Optional.of(annotazioneCompleta));
        when(annotazioneStoricoStatiService.inserisciCambioStato(any(), any(), any(), any(), any(), any())).thenReturn(null);
        when(annotazioneService.cambiaStato(any(), any(), any())).thenReturn(annotazioneCompleta);

        AnnotazioneCompleta result = portService.aggiornaAnnotazione(annotazioneCompleta, "utente");
        assertNotNull(result);
        verify(annotazioneService).aggiornaAnnotazione(eq(id), anyString(), anyString(), eq("utente"));
    }

    @Test
    void aggiornaAnnotazione_annotazioneLoccataDaAltro_lanciaEccezione() {
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("altroUtente"));

        assertThrows(AnnotationLockedException.class, () ->
                portService.aggiornaAnnotazione(annotazioneCompleta, "utente"));
    }

    @Test
    void aggiornaAnnotazione_lockOwnerUgualeUtente_nonLanciaEccezione() {
        when(lockService.isLocked(id)).thenReturn(true);
        when(lockService.getOwner(id)).thenReturn(Optional.of("utente"));
        when(userService.findByUsername("utente")).thenReturn(Optional.of(adminUser));
        when(annotazioneService.trovaPerID(id)).thenReturn(Optional.of(annotazioneCompleta));
        when(annotazioneStoricoStatiService.inserisciCambioStato(any(), any(), any(), any(), any(), any())).thenReturn(null);
        when(annotazioneService.cambiaStato(any(), any(), any())).thenReturn(annotazioneCompleta);

        assertDoesNotThrow(() -> portService.aggiornaAnnotazione(annotazioneCompleta, "utente"));
    }

    @Test
    void aggiornaAnnotazione_utenteNonTrovato_lanciaException() {
        when(lockService.isLocked(id)).thenReturn(false);
        when(userService.findByUsername("utente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                portService.aggiornaAnnotazione(annotazioneCompleta, "utente"));
    }

    // ---- cambiaStato ----

    @Test
    void cambiaStato_utenteNonTrovato_lanciaException() {
        when(userService.findByUsername("utente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                portService.cambiaStato(id, StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, "utente"));
    }

    @Test
    void cambiaStato_annotazioneNonTrovata_lanciaException() {
        when(userService.findByUsername("utente")).thenReturn(Optional.of(adminUser));
        doNothing().when(validatoreTransizioniStatoService).validaTransizione(any(), any(), any());
        when(annotazioneService.trovaPerID(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                portService.cambiaStato(id, StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, "utente"));
    }

    @Test
    void cambiaStato_corretto_inserisceStorico() {
        when(userService.findByUsername("utente")).thenReturn(Optional.of(adminUser));
        when(annotazioneService.trovaPerID(id)).thenReturn(Optional.of(annotazioneCompleta));
        when(annotazioneStoricoStatiService.inserisciCambioStato(any(), any(), any(), any(), any(), any())).thenReturn(null);
        when(annotazioneService.cambiaStato(eq(id), anyString(), anyString())).thenReturn(annotazioneCompleta);

        AnnotazioneCompleta result = portService.cambiaStato(id, StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, "utente");
        assertNotNull(result);
        verify(annotazioneStoricoStatiService).inserisciCambioStato(any(), any(), any(), any(), any(), any());
    }
}
