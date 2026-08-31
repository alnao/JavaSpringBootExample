package it.alnao.springbootexample.core.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneCompletaTest {

    private AnnotazioneCompleta completa(UUID id) {
        Annotazione ann = new Annotazione(id, "1.0", "il testo");
        AnnotazioneMetadata meta = new AnnotazioneMetadata(id, "1.0", "mario", "la descrizione");
        return new AnnotazioneCompleta(ann, meta);
    }

    @Test
    void accessorsDelegateToTheWrappedObjects() {
        UUID id = UUID.randomUUID();
        AnnotazioneCompleta c = completa(id);
        assertEquals(id, c.getId());
        assertEquals("il testo", c.getValoreNota());
        assertEquals("la descrizione", c.getDescrizione());
        assertEquals("mario", c.getUtenteCreazione());
        assertEquals("1.0", c.getVersioneNota());
    }

    @Test
    void accessorsAreNullSafeWhenTheAnnotazioneIsMissing() {
        AnnotazioneCompleta c = new AnnotazioneCompleta();
        assertNull(c.getId());
        assertNull(c.getValoreNota());
        assertNull(c.getVersioneNota());
        assertNull(c.getDescrizione());
        assertNull(c.getUtenteCreazione());
    }

    @Test
    void settersReplaceTheWrappedObjects() {
        UUID id = UUID.randomUUID();
        AnnotazioneCompleta c = new AnnotazioneCompleta();
        Annotazione ann = new Annotazione(id, "2.0", "nuovo testo");
        AnnotazioneMetadata meta = new AnnotazioneMetadata(id, "2.0", "luigi", "nuova descr");
        c.setAnnotazione(ann);
        c.setMetadata(meta);

        assertSame(ann, c.getAnnotazione());
        assertSame(meta, c.getMetadata());
        assertEquals("nuovo testo", c.getValoreNota());
        assertEquals("luigi", c.getUtenteCreazione());
    }

    @Test
    void toStringIncludesBothParts() {
        String text = completa(UUID.randomUUID()).toString();
        assertTrue(text.contains("annotazione="));
        assertTrue(text.contains("metadata="));
    }

    @Test
    void metadata_aggiornaRefreshesTheModificationFields() {
        AnnotazioneMetadata meta = new AnnotazioneMetadata(UUID.randomUUID(), "1.0", "mario", "descr");
        LocalDateTime prima = LocalDateTime.now().minusDays(1);
        meta.setDataUltimaModifica(prima);

        meta.aggiorna("luigi");

        assertEquals("luigi", meta.getUtenteUltimaModifica());
        assertTrue(meta.getDataUltimaModifica().isAfter(prima));
    }

    @Test
    void metadata_toStringIncludesTheMainFields() {
        AnnotazioneMetadata meta = new AnnotazioneMetadata(UUID.randomUUID(), "1.0", "mario", "descr");
        meta.setCategoria("lavoro");
        meta.setStato(StatoAnnotazione.INSERITA.getValue());
        String text = meta.toString();
        assertTrue(text.contains("mario"));
        assertTrue(text.contains("lavoro"));
        assertTrue(text.contains("INSERITA"));
    }
}
