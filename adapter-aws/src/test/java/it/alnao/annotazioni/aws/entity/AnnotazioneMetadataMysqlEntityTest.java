package it.alnao.annotazioni.aws.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMetadataMysqlEntityTest {
    @Test
    void gettersAndSetters() {
        AnnotazioneMetadataMysqlEntity e = new AnnotazioneMetadataMysqlEntity();
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
