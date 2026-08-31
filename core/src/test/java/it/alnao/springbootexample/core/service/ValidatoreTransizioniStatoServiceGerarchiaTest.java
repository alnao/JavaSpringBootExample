package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copre la gerarchia dei ruoli (ADMIN &gt; MODERATOR &gt; USER, con SYSTEM a parte),
 * raggiungibile solo indirettamente dai test sulle transizioni.
 */
class ValidatoreTransizioniStatoServiceGerarchiaTest {

    private ValidatoreTransizioniStatoService service;

    @BeforeEach
    void setup() {
        service = new ValidatoreTransizioniStatoService();
        service.initTransizioni();
    }

    private boolean hasPermission(UserRole utente, UserRole richiesto) throws Exception {
        Method m = ValidatoreTransizioniStatoService.class
                .getDeclaredMethod("hasPermissionForRole", UserRole.class, UserRole.class);
        m.setAccessible(true);
        return (boolean) m.invoke(service, utente, richiesto);
    }

    @Test
    void stessoRuolo_semprePermesso() throws Exception {
        for (UserRole ruolo : UserRole.values()) {
            assertTrue(hasPermission(ruolo, ruolo), "stesso ruolo dovrebbe essere permesso: " + ruolo);
        }
    }

    @Test
    void admin_puoFareTuttoTranneLeOperazioniSystem() throws Exception {
        assertTrue(hasPermission(UserRole.ADMIN, UserRole.USER));
        assertTrue(hasPermission(UserRole.ADMIN, UserRole.MODERATOR));
        assertFalse(hasPermission(UserRole.ADMIN, UserRole.SYSTEM));
    }

    @Test
    void moderator_puoFareSoloLeOperazioniUser() throws Exception {
        assertTrue(hasPermission(UserRole.MODERATOR, UserRole.USER));
        assertFalse(hasPermission(UserRole.MODERATOR, UserRole.ADMIN));
        assertFalse(hasPermission(UserRole.MODERATOR, UserRole.SYSTEM));
    }

    @Test
    void user_nonEreditaNessunAltroRuolo() throws Exception {
        assertFalse(hasPermission(UserRole.USER, UserRole.ADMIN));
        assertFalse(hasPermission(UserRole.USER, UserRole.MODERATOR));
        assertFalse(hasPermission(UserRole.USER, UserRole.SYSTEM));
    }

    @Test
    void system_nonEreditaNessunAltroRuolo() throws Exception {
        assertFalse(hasPermission(UserRole.SYSTEM, UserRole.ADMIN));
        assertFalse(hasPermission(UserRole.SYSTEM, UserRole.USER));
        assertFalse(hasPermission(UserRole.SYSTEM, UserRole.MODERATOR));
    }

    @Test
    void getTransizioniPossibili_filtraSuStatoERuolo() {
        List<TransizioneStato> daInserita =
                service.getTransizioniPossibili(StatoAnnotazione.INSERITA, UserRole.ADMIN);
        assertNotNull(daInserita);
        daInserita.forEach(t -> assertEquals(StatoAnnotazione.INSERITA, t.getStatoPartenza()));
    }

    @Test
    void validaTransizione_seNonPermessaLanciaIllegalState() {
        assertThrows(IllegalStateException.class, () -> service.validaTransizione(
                StatoAnnotazione.INVIATA, StatoAnnotazione.INSERITA, UserRole.USER));
    }

    @Test
    void validaTransizione_sePermessaNonLancia() {
        assertDoesNotThrow(() -> service.validaTransizione(
                StatoAnnotazione.INSERITA, StatoAnnotazione.INSERITA, UserRole.USER));
    }
}
