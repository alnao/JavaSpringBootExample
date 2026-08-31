package it.alnao.springbootexample.api.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica costruttori, accessor e toString dei DTO di richiesta/risposta.
 */
class ApiDtoTest {

    // ---------- PrenotaAnnotazioneResponse ----------

    @Test
    void prenotaAnnotazioneResponse_allArgsConstructor_copiesEveryField() {
        UUID id = UUID.randomUUID();
        LocalDateTime prenotazione = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime scadenza = prenotazione.plusMinutes(5);
        PrenotaAnnotazioneResponse r = new PrenotaAnnotazioneResponse(
                id, "mario", prenotazione, scadenza, true, "prenotata");
        assertEquals(id, r.getAnnotazioneId());
        assertEquals("mario", r.getUtente());
        assertEquals(prenotazione, r.getDataPrenotazione());
        assertEquals(scadenza, r.getScadenzaLock());
        assertTrue(r.isPrenotataConSuccesso());
        assertEquals("prenotata", r.getMessaggio());
    }

    @Test
    void prenotaAnnotazioneResponse_settersAndGettersWork() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PrenotaAnnotazioneResponse r = new PrenotaAnnotazioneResponse();
        r.setAnnotazioneId(id);
        r.setUtente("luigi");
        r.setDataPrenotazione(now);
        r.setScadenzaLock(now.plusMinutes(1));
        r.setPrenotataConSuccesso(false);
        r.setMessaggio("gia prenotata");

        assertEquals(id, r.getAnnotazioneId());
        assertEquals("luigi", r.getUtente());
        assertEquals(now, r.getDataPrenotazione());
        assertEquals(now.plusMinutes(1), r.getScadenzaLock());
        assertFalse(r.isPrenotataConSuccesso());
        assertEquals("gia prenotata", r.getMessaggio());
    }

    @Test
    void prenotaAnnotazioneResponse_defaultConstructorLeavesFieldsEmpty() {
        PrenotaAnnotazioneResponse r = new PrenotaAnnotazioneResponse();
        assertNull(r.getAnnotazioneId());
        assertNull(r.getUtente());
        assertFalse(r.isPrenotataConSuccesso());
    }

    // ---------- PrenotaAnnotazioneRequest ----------

    @Test
    void prenotaAnnotazioneRequest_constructorAndAccessors() {
        PrenotaAnnotazioneRequest r = new PrenotaAnnotazioneRequest("mario", 42);
        assertEquals("mario", r.getUtente());
        assertEquals(42, r.getSecondi());

        r.setUtente("luigi");
        r.setSecondi(10);
        assertEquals("luigi", r.getUtente());
        assertEquals(10, r.getSecondi());
    }

    @Test
    void prenotaAnnotazioneRequest_defaultConstructorLeavesFieldsNull() {
        PrenotaAnnotazioneRequest r = new PrenotaAnnotazioneRequest();
        assertNull(r.getUtente());
        assertNull(r.getSecondi());
    }

    // ---------- CambiaStatoAnnotazioneRequest ----------

    @Test
    void cambiaStatoAnnotazioneRequest_constructorAndAccessors() {
        CambiaStatoAnnotazioneRequest r =
                new CambiaStatoAnnotazioneRequest("INSERITA", "CONFERMATA", "admin");
        assertEquals("INSERITA", r.getVecchioStato());
        assertEquals("CONFERMATA", r.getNuovoStato());
        assertEquals("admin", r.getUtente());

        r.setVecchioStato("MODIFICATA");
        r.setNuovoStato("INVIATA");
        r.setUtente("mario");
        assertEquals("MODIFICATA", r.getVecchioStato());
        assertEquals("INVIATA", r.getNuovoStato());
        assertEquals("mario", r.getUtente());
    }

    @Test
    void cambiaStatoAnnotazioneRequest_defaultConstructorLeavesFieldsNull() {
        CambiaStatoAnnotazioneRequest r = new CambiaStatoAnnotazioneRequest();
        assertNull(r.getVecchioStato());
        assertNull(r.getNuovoStato());
        assertNull(r.getUtente());
    }

    @Test
    void cambiaStatoAnnotazioneRequest_toStringIncludesTheThreeFields() {
        String text = new CambiaStatoAnnotazioneRequest("INSERITA", "CONFERMATA", "admin").toString();
        assertTrue(text.contains("INSERITA"));
        assertTrue(text.contains("CONFERMATA"));
        assertTrue(text.contains("admin"));
    }

    // ---------- TransizioneStatoResponse ----------

    @Test
    void transizioneStatoResponse_constructorAndAccessors() {
        TransizioneStatoResponse r = new TransizioneStatoResponse(
                "INSERITA", "MODIFICATA", "USER", "utente puo modificare");
        assertEquals("INSERITA", r.getStatoPartenza());
        assertEquals("MODIFICATA", r.getStatoArrivo());
        assertEquals("USER", r.getRuoloRichiesto());
        assertEquals("utente puo modificare", r.getDescrizione());

        r.setStatoPartenza("MODIFICATA");
        r.setStatoArrivo("INVIATA");
        r.setRuoloRichiesto("ADMIN");
        r.setDescrizione("solo admin");
        assertEquals("MODIFICATA", r.getStatoPartenza());
        assertEquals("INVIATA", r.getStatoArrivo());
        assertEquals("ADMIN", r.getRuoloRichiesto());
        assertEquals("solo admin", r.getDescrizione());
    }

    @Test
    void transizioneStatoResponse_defaultConstructorLeavesFieldsNull() {
        TransizioneStatoResponse r = new TransizioneStatoResponse();
        assertNull(r.getStatoPartenza());
        assertNull(r.getStatoArrivo());
        assertNull(r.getRuoloRichiesto());
        assertNull(r.getDescrizione());
    }
}
