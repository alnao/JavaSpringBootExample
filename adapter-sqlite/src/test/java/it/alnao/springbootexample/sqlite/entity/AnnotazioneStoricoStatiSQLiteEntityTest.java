package it.alnao.springbootexample.sqlite.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoStatiSQLiteEntityTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 2, 2, 14, 30);

    private AnnotazioneStoricoStatiSQLiteEntity full() {
        return new AnnotazioneStoricoStatiSQLiteEntity(
                "op-1", "ann-1", "1.0", "INVIATA", "DAINVIARE", "mario", DATA, "invio automatico");
    }

    @Test
    void allArgsConstructor_copiesEveryField() {
        AnnotazioneStoricoStatiSQLiteEntity entity = full();
        assertEquals("op-1", entity.getIdOperazione());
        assertEquals("ann-1", entity.getIdAnnotazione());
        assertEquals("1.0", entity.getVersione());
        assertEquals("INVIATA", entity.getStatoNew());
        assertEquals("DAINVIARE", entity.getStatoOld());
        assertEquals("mario", entity.getUtente());
        assertEquals(DATA, entity.getDataModifica());
        assertEquals("invio automatico", entity.getNotaOperazione());
    }

    @Test
    void defaultConstructor_leavesFieldsNull() {
        AnnotazioneStoricoStatiSQLiteEntity entity = new AnnotazioneStoricoStatiSQLiteEntity();
        assertNull(entity.getIdOperazione());
        assertNull(entity.getIdAnnotazione());
        assertNull(entity.getStatoNew());
        assertNull(entity.getDataModifica());
    }

    @Test
    void settersAndGetters_work() {
        AnnotazioneStoricoStatiSQLiteEntity entity = new AnnotazioneStoricoStatiSQLiteEntity();
        entity.setIdOperazione("op-2");
        entity.setIdAnnotazione("ann-2");
        entity.setVersione("2.0");
        entity.setStatoNew("MODIFICATA");
        entity.setStatoOld("INSERITA");
        entity.setUtente("luigi");
        entity.setDataModifica(DATA);
        entity.setNotaOperazione("modifica manuale");

        assertEquals("op-2", entity.getIdOperazione());
        assertEquals("ann-2", entity.getIdAnnotazione());
        assertEquals("2.0", entity.getVersione());
        assertEquals("MODIFICATA", entity.getStatoNew());
        assertEquals("INSERITA", entity.getStatoOld());
        assertEquals("luigi", entity.getUtente());
        assertEquals(DATA, entity.getDataModifica());
        assertEquals("modifica manuale", entity.getNotaOperazione());
    }

    @Test
    void toString_includesTheIdentifyingFields() {
        String text = full().toString();
        assertTrue(text.contains("op-1"));
        assertTrue(text.contains("ann-1"));
        assertTrue(text.contains("INVIATA"));
        assertTrue(text.contains("mario"));
    }
}
