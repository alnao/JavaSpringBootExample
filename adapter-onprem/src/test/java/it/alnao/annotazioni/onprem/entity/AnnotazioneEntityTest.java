package it.alnao.annotazioni.onprem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneEntityTest {
    @Test
    void gettersAndSetters() {
        AnnotazioneEntity e = new AnnotazioneEntity();
        e.setId("id");
        e.setVersioneNota("v1");
        e.setValoreNota("nota");
        assertEquals("id", e.getId());
        assertEquals("v1", e.getVersioneNota());
        assertEquals("nota", e.getValoreNota());
    }
}
