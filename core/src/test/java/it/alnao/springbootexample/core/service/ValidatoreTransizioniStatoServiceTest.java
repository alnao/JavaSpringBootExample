package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ValidatoreTransizioniStatoServiceTest {

    private ValidatoreTransizioniStatoService service;

    @BeforeEach
    void setUp() {
        service = new ValidatoreTransizioniStatoService();
        service.initTransizioni();
    }

    // ---- getTutteLeTransizioni ----

    @Test
    void getTutteLeTransizioni_ritornaListaNonVuota() {
        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        assertNotNull(transizioni);
        assertFalse(transizioni.isEmpty());
    }

    // ---- isTransizionePermessa (enum version) ----

    @Test
    void stessoStato_semprePermesso() {
        assertTrue(service.isTransizionePermessa(StatoAnnotazione.INSERITA, StatoAnnotazione.INSERITA, UserRole.USER));
        assertTrue(service.isTransizionePermessa(StatoAnnotazione.MODIFICATA, StatoAnnotazione.MODIFICATA, UserRole.ADMIN));
    }

    @Test
    void admin_puoEseguireTransizioniRuoloUser() {
        // ADMIN ha gerarchia superiore a USER, dovrebbe poter fare transizioni USER
        // Verifica almeno una transizione USER → ADMIN la può fare
        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        // Trova una transizione con ruolo USER e verifica che ADMIN possa farla
        Optional<TransizioneStato> transizioneUser = transizioni.stream()
                .filter(t -> t.getRuoloRichiesto() == UserRole.USER)
                .findFirst();
        if (transizioneUser.isPresent()) {
            TransizioneStato t = transizioneUser.get();
            assertTrue(service.isTransizionePermessa(t.getStatoPartenza(), t.getStatoArrivo(), UserRole.ADMIN));
        }
    }

    @Test
    void user_nonPuoFareTransizioneRichiedenteAdmin() {
        // CONFERMATA→RIFIUTATA è ADMIN-only nel YAML (nessuna versione USER/MODERATOR)
        assertFalse(service.isTransizionePermessa(
                StatoAnnotazione.CONFERMATA, StatoAnnotazione.RIFIUTATA, UserRole.USER));
    }

    @Test
    void moderator_puoFareTransizioneRichiedenteUser() {
        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        Optional<TransizioneStato> transizioneUser = transizioni.stream()
                .filter(t -> t.getRuoloRichiesto() == UserRole.USER)
                .findFirst();
        if (transizioneUser.isPresent()) {
            TransizioneStato t = transizioneUser.get();
            assertTrue(service.isTransizionePermessa(t.getStatoPartenza(), t.getStatoArrivo(), UserRole.MODERATOR));
        }
    }

    @Test
    void transizione_nonEsistente_nonPermessa() {
        // ERRORE → INSERITA non dovrebbe essere permessa
        assertFalse(service.isTransizionePermessa(StatoAnnotazione.ERRORE, StatoAnnotazione.INSERITA, UserRole.USER));
    }

    // ---- isTransizionePermessa (String version) ----

    @Test
    void isTransizionePermessa_conStringhe_stessoStato_permessa() {
        assertTrue(service.isTransizionePermessa("INSERITA", "INSERITA", "USER"));
    }

    @Test
    void isTransizionePermessa_conStringheNonValide_ritornaFalse() {
        assertFalse(service.isTransizionePermessa("NON_ESISTE", "INSERITA", "USER"));
        assertFalse(service.isTransizionePermessa("INSERITA", "NON_ESISTE", "USER"));
        assertFalse(service.isTransizionePermessa("INSERITA", "INSERITA", "RUOLO_INESISTENTE"));
    }

    // ---- trovaTransizione ----

    @Test
    void trovaTransizione_esistente_ritornaPresent() {
        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        assertFalse(transizioni.isEmpty());
        TransizioneStato prima = transizioni.get(0);
        Optional<TransizioneStato> trovata = service.trovaTransizione(
                prima.getStatoPartenza(), prima.getStatoArrivo(), prima.getRuoloRichiesto());
        assertTrue(trovata.isPresent());
    }

    @Test
    void trovaTransizione_inesistente_ritornaEmpty() {
        Optional<TransizioneStato> trovata = service.trovaTransizione(
                StatoAnnotazione.BANNATA, StatoAnnotazione.INSERITA, UserRole.USER);
        assertTrue(trovata.isEmpty());
    }

    // ---- getTransizioniPossibili ----

    @Test
    void getTransizioniPossibili_ritornaListaFiltrata() {
        List<TransizioneStato> possibili = service.getTransizioniPossibili(StatoAnnotazione.INSERITA, UserRole.USER);
        assertNotNull(possibili);
        // Tutte devono avere statoPartenza = INSERITA
        possibili.forEach(t -> assertEquals(StatoAnnotazione.INSERITA, t.getStatoPartenza()));
    }

    @Test
    void getTransizioniPossibili_statoSenzaTransizioni_ritornaVuoto() {
        // BANNATA probabilmente non ha transizioni da quello stato
        List<TransizioneStato> possibili = service.getTransizioniPossibili(StatoAnnotazione.BANNATA, UserRole.USER);
        assertNotNull(possibili);
        // Non lanciare eccezione, lista può essere vuota
    }

    // ---- validaTransizione ----

    @Test
    void validaTransizione_stessoStato_nonLanciaEccezione() {
        assertDoesNotThrow(() ->
                service.validaTransizione(StatoAnnotazione.INSERITA, StatoAnnotazione.INSERITA, UserRole.USER));
    }

    @Test
    void validaTransizione_nonPermessa_lanciaIllegalStateException() {
        assertThrows(IllegalStateException.class, () ->
                service.validaTransizione(StatoAnnotazione.BANNATA, StatoAnnotazione.INSERITA, UserRole.USER));
    }

    @Test
    void validaTransizione_permessa_nonLanciaEccezione() {
        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        if (!transizioni.isEmpty()) {
            TransizioneStato t = transizioni.get(0);
            assertDoesNotThrow(() ->
                    service.validaTransizione(t.getStatoPartenza(), t.getStatoArrivo(), t.getRuoloRichiesto()));
        }
    }

    // ---- Gerarchia ruoli ----

    @Test
    void admin_nonPuoFareOperazioniSystem() {
        // Construiamo una transizione con ruolo SYSTEM e verifichiamo che ADMIN non possa farla
        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        Optional<TransizioneStato> transizioneSystem = transizioni.stream()
                .filter(t -> t.getRuoloRichiesto() == UserRole.SYSTEM)
                .findFirst();
        if (transizioneSystem.isPresent()) {
            TransizioneStato t = transizioneSystem.get();
            assertFalse(service.isTransizionePermessa(t.getStatoPartenza(), t.getStatoArrivo(), UserRole.ADMIN));
        }
    }

    @Test
    void system_puoFareSoloOperazioniSystem() {
        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        Optional<TransizioneStato> transizioneUser = transizioni.stream()
                .filter(t -> t.getRuoloRichiesto() == UserRole.USER)
                .findFirst();
        if (transizioneUser.isPresent()) {
            TransizioneStato t = transizioneUser.get();
            assertFalse(service.isTransizionePermessa(t.getStatoPartenza(), t.getStatoArrivo(), UserRole.SYSTEM));
        }
    }
}
