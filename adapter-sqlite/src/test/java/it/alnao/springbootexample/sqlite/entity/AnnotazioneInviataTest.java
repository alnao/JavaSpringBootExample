package it.alnao.springbootexample.sqlite.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneInviataTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 4, 1, 16, 0);

    @Test
    void constructor_copiesEveryFieldAndLeavesErrorNull() {
        UUID annotazioneId = UUID.randomUUID();
        AnnotazioneInviata inviata = new AnnotazioneInviata(annotazioneId, "{\"id\":1}", DATA, "OK");
        assertEquals(annotazioneId, inviata.getAnnotazioneId());
        assertEquals("{\"id\":1}", inviata.getContenuto());
        assertEquals(DATA, inviata.getDataInvio());
        assertEquals("OK", inviata.getStatoInvio());
        assertNull(inviata.getMessaggioErrore());
        assertNull(inviata.getId());
    }

    @Test
    void defaultConstructor_leavesFieldsNull() {
        AnnotazioneInviata inviata = new AnnotazioneInviata();
        assertNull(inviata.getAnnotazioneId());
        assertNull(inviata.getContenuto());
        assertNull(inviata.getDataInvio());
        assertNull(inviata.getStatoInvio());
    }

    @Test
    void settersAndGetters_work() {
        UUID annotazioneId = UUID.randomUUID();
        AnnotazioneInviata inviata = new AnnotazioneInviata();
        inviata.setId(7L);
        inviata.setAnnotazioneId(annotazioneId);
        inviata.setContenuto("payload");
        inviata.setDataInvio(DATA);
        inviata.setStatoInvio("ERRORE");
        inviata.setMessaggioErrore("coda irraggiungibile");

        assertEquals(7L, inviata.getId());
        assertEquals(annotazioneId, inviata.getAnnotazioneId());
        assertEquals("payload", inviata.getContenuto());
        assertEquals(DATA, inviata.getDataInvio());
        assertEquals("ERRORE", inviata.getStatoInvio());
        assertEquals("coda irraggiungibile", inviata.getMessaggioErrore());
    }

    @Test
    void toString_includesStatoAndContenuto() {
        AnnotazioneInviata inviata = new AnnotazioneInviata(UUID.randomUUID(), "payload", DATA, "OK");
        String text = inviata.toString();
        assertTrue(text.contains("OK"));
        assertTrue(text.contains("payload"));
    }
}
