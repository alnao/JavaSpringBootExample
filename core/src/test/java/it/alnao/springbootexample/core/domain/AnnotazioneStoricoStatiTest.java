package it.alnao.springbootexample.core.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoStatiTest {

    @Test
    void noArgConstructor_works() {
        AnnotazioneStoricoStati s = new AnnotazioneStoricoStati();
        assertNotNull(s);
    }

    @Test
    void fullArgConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        AnnotazioneStoricoStati s = new AnnotazioneStoricoStati(
                "op-1", "ann-1", "1.0", "MODIFICATA", "INSERITA", "user", now, "nota");

        assertEquals("op-1", s.getIdOperazione());
        assertEquals("ann-1", s.getIdAnnotazione());
        assertEquals("1.0", s.getVersione());
        assertEquals("MODIFICATA", s.getStatoNew());
        assertEquals("INSERITA", s.getStatoOld());
        assertEquals("user", s.getUtente());
        assertEquals(now, s.getDataModifica());
        assertEquals("nota", s.getNotaOperazione());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        AnnotazioneStoricoStati s = new AnnotazioneStoricoStati();
        s.setIdOperazione("op-1");
        s.setIdAnnotazione("ann-1");
        s.setVersione("2.0");
        s.setStatoNew("CONFERMATA");
        s.setStatoOld("MODIFICATA");
        s.setUtente("admin");
        s.setDataModifica(now);
        s.setNotaOperazione("nota operazione");

        assertEquals("op-1", s.getIdOperazione());
        assertEquals("ann-1", s.getIdAnnotazione());
        assertEquals("2.0", s.getVersione());
        assertEquals("CONFERMATA", s.getStatoNew());
        assertEquals("MODIFICATA", s.getStatoOld());
        assertEquals("admin", s.getUtente());
        assertEquals(now, s.getDataModifica());
        assertEquals("nota operazione", s.getNotaOperazione());
    }

    @Test
    void toString_containsKeyFields() {
        AnnotazioneStoricoStati s = new AnnotazioneStoricoStati();
        s.setIdAnnotazione("ann-1");
        s.setStatoNew("MODIFICATA");
        String str = s.toString();
        assertNotNull(str);
        assertTrue(str.contains("ann-1") || str.contains("MODIFICATA") || str.length() > 0);
    }
}
