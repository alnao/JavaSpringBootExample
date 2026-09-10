package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.exception.TransizioniStatoConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copre il caricamento della configurazione delle transizioni: una configurazione
 * non valida deve interrompere l'inizializzazione con un messaggio che permetta di
 * individuare il problema leggendo i soli log, senza insiemi di ripiego.
 */
class ValidatoreTransizioniStatoServiceCaricamentoTest {

    private TransizioniStatoConfigurationException initFallita(String risorsa) {
        ValidatoreTransizioniStatoService service = new ValidatoreTransizioniStatoService(risorsa);
        return assertThrows(TransizioniStatoConfigurationException.class, service::initTransizioni);
    }

    // ---- Configurazione assente ----

    @Test
    void configurazioneAssente_interrompeInizializzazione() {
        TransizioniStatoConfigurationException e = initFallita("transizioni-test/inesistente.yaml");
        assertTrue(e.getMessage().contains("non è stata trovata nel classpath"),
                "il messaggio deve dire che la configurazione non è stata trovata: " + e.getMessage());
        assertTrue(e.getMessage().contains("transizioni-test/inesistente.yaml"),
                "il messaggio deve nominare la risorsa cercata: " + e.getMessage());
    }

    // ---- Configurazione non interpretabile ----

    @Test
    void configurazioneMalformata_interrompeInizializzazione() {
        TransizioniStatoConfigurationException e = initFallita("transizioni-test/malformato.yaml");
        assertTrue(e.getMessage().contains("non è interpretabile come YAML valido"),
                "il messaggio deve indicare il problema di sintassi: " + e.getMessage());
        assertNotNull(e.getCause(), "l'errore di parsing originale va conservato come causa");
    }

    // ---- Stato non riconosciuto ----

    @Test
    void statoInesistente_interrompeInizializzazioneIndicandoLaVoce() {
        TransizioniStatoConfigurationException e = initFallita("transizioni-test/stato-inesistente.yaml");
        assertTrue(e.getMessage().contains("posizione 2"),
                "il messaggio deve indicare la posizione della voce responsabile: " + e.getMessage());
        assertTrue(e.getMessage().contains("APPROVATA"),
                "il messaggio deve riportare il valore non riconosciuto: " + e.getMessage());
        assertTrue(e.getMessage().contains("Voce con stato inesistente"),
                "il messaggio deve riportare la descrizione della voce: " + e.getMessage());
    }

    // ---- Ruolo non riconosciuto ----

    @Test
    void ruoloInesistente_interrompeInizializzazioneIndicandoLaVoce() {
        TransizioniStatoConfigurationException e = initFallita("transizioni-test/ruolo-inesistente.yaml");
        assertTrue(e.getMessage().contains("posizione 1"),
                "il messaggio deve indicare la posizione della voce responsabile: " + e.getMessage());
        assertTrue(e.getMessage().contains("SUPERVISORE"),
                "il messaggio deve riportare il ruolo non riconosciuto: " + e.getMessage());
    }

    // ---- Configurazione vuota ----

    @Test
    void configurazioneVuota_interrompeInizializzazione() {
        TransizioniStatoConfigurationException e = initFallita("transizioni-test/vuoto.yaml");
        assertTrue(e.getMessage().contains("non dichiara alcuna transizione"),
                "una configurazione senza transizioni è un errore: " + e.getMessage());
    }

    @Test
    void chiaveTransizioniSenzaValore_interrompeInizializzazione() {
        TransizioniStatoConfigurationException e = initFallita("transizioni-test/chiave-senza-valore.yaml");
        assertTrue(e.getMessage().contains("non dichiara alcuna transizione"),
                "una chiave transizioni senza voci è equivalente a una configurazione vuota: " + e.getMessage());
    }

    @Test
    void configurazioneConChiaviSconosciute_interrompeInizializzazione() {
        // Una configurazione che non descrive transizioni viene respinta dal parser:
        // resta un errore di avvio, cambia soltanto il motivo riportato.
        TransizioniStatoConfigurationException e = initFallita("transizioni-test/senza-chiave.yaml");
        assertTrue(e.getMessage().contains("transizioni-test/senza-chiave.yaml"),
                "il messaggio deve nominare la risorsa non valida: " + e.getMessage());
        assertTrue(e.getMessage().contains("non può avviarsi"),
                "il messaggio deve dire che l'applicazione non parte: " + e.getMessage());
    }

    // ---- Nessun ripiego ----

    @Test
    void caricamentoFallito_nonLasciaTransizioniUtilizzabili() {
        ValidatoreTransizioniStatoService service =
                new ValidatoreTransizioniStatoService("transizioni-test/inesistente.yaml");
        assertThrows(TransizioniStatoConfigurationException.class, service::initTransizioni);
        assertThrows(NullPointerException.class,
                () -> service.isTransizionePermessa(StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, UserRole.USER),
                "senza caricamento riuscito non deve esistere alcun insieme di ripiego utilizzabile");
    }

    // ---- Configurazione valida ----

    @Test
    void configurazioneValida_vieneCaricata() {
        ValidatoreTransizioniStatoService service =
                new ValidatoreTransizioniStatoService("transizioni-test/valido.yaml");
        assertDoesNotThrow(service::initTransizioni);

        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        assertEquals(2, transizioni.size());
        assertTrue(service.isTransizionePermessa(
                StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, UserRole.USER));
        assertTrue(service.isTransizionePermessa(
                StatoAnnotazione.MODIFICATA, StatoAnnotazione.CONFERMATA, UserRole.MODERATOR));
    }

    // ---- File realmente impacchettato ----

    @Test
    void configurazioneSpedita_siCaricaEOgniVoceRiferisceStatiERuoliEsistenti() {
        ValidatoreTransizioniStatoService service = new ValidatoreTransizioniStatoService();
        assertDoesNotThrow(service::initTransizioni,
                "il file cambiamentoStati.yaml spedito nel jar deve caricarsi senza errori");

        List<TransizioneStato> transizioni = service.getTutteLeTransizioni();
        assertFalse(transizioni.isEmpty(), "la configurazione spedita deve dichiarare transizioni");
        for (TransizioneStato t : transizioni) {
            assertNotNull(t.getStatoPartenza());
            assertNotNull(t.getStatoArrivo());
            assertNotNull(t.getRuoloRichiesto());
        }
    }
}
