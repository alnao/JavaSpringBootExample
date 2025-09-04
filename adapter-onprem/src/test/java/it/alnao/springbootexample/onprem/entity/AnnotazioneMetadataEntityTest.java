package it.alnao.springbootexample.onprem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMetadataEntityTest {
    @Test
    void gettersAndSetters() {
        AnnotazioneMetadataEntity e = new AnnotazioneMetadataEntity();
        e.setId("id");
        e.setDescrizione("desc");
        e.setCategoria("cat");
        e.setTags("tag");
        e.setPubblica(true);
        e.setPriorita(2);
        assertEquals("id", e.getId());
        assertEquals("desc", e.getDescrizione());
        assertEquals("cat", e.getCategoria());
        assertEquals("tag", e.getTags());
        assertTrue(e.getPubblica());
        assertEquals(2, e.getPriorita());
    }
}
