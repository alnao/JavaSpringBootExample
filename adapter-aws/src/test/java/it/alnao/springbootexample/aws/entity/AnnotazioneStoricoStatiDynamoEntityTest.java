package it.alnao.springbootexample.aws.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoStatiDynamoEntityTest {

    @Test
    void noArgConstructor_works() {
        AnnotazioneStoricoStatiDynamoEntity e = new AnnotazioneStoricoStatiDynamoEntity();
        assertNotNull(e);
    }

    @Test
    void fullArgConstructor_setsAllFields() {
        AnnotazioneStoricoStatiDynamoEntity e = new AnnotazioneStoricoStatiDynamoEntity(
                "op-1", "ann-1", "1.0", "MODIFICATA", "INSERITA", "user",
                "2026-01-01T00:00:00", "nota");
        assertEquals("op-1", e.getIdOperazione());
        assertEquals("ann-1", e.getIdAnnotazione());
        assertEquals("1.0", e.getVersione());
        assertEquals("MODIFICATA", e.getStatoNew());
        assertEquals("INSERITA", e.getStatoOld());
        assertEquals("user", e.getUtente());
        assertEquals("2026-01-01T00:00:00", e.getDataModifica());
        assertEquals("nota", e.getNotaOperazione());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        AnnotazioneStoricoStatiDynamoEntity e = new AnnotazioneStoricoStatiDynamoEntity();
        e.setIdOperazione("op-2");
        e.setIdAnnotazione("ann-2");
        e.setVersione("2.0");
        e.setStatoNew("CONFERMATA");
        e.setStatoOld("MODIFICATA");
        e.setUtente("admin");
        e.setDataModifica("2026-02-01T12:00:00");
        e.setNotaOperazione("nota op");

        assertEquals("op-2", e.getIdOperazione());
        assertEquals("ann-2", e.getIdAnnotazione());
        assertEquals("2.0", e.getVersione());
        assertEquals("CONFERMATA", e.getStatoNew());
        assertEquals("MODIFICATA", e.getStatoOld());
        assertEquals("admin", e.getUtente());
        assertEquals("2026-02-01T12:00:00", e.getDataModifica());
        assertEquals("nota op", e.getNotaOperazione());
    }
}
