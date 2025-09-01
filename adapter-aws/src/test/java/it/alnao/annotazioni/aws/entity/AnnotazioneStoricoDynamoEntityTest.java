package it.alnao.annotazioni.aws.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoDynamoEntityTest {
    @Test
    void gettersAndSetters() {
        AnnotazioneStoricoDynamoEntity e = new AnnotazioneStoricoDynamoEntity();
        e.setId("id");
        e.setIdOriginale("ido");
        e.setValoreNota("nota");
        assertEquals("id", e.getId());
        assertEquals("ido", e.getIdOriginale());
        assertEquals("nota", e.getValoreNota());
    }
}
