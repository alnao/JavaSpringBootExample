package it.alnao.springbootexample.onprem.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoEntityTest {
    @Test
    void gettersAndSetters() {
        AnnotazioneStoricoEntity e = new AnnotazioneStoricoEntity();
        e.setId("id");
        e.setIdOriginale("ido");
        e.setValoreNota("nota");
        assertEquals("id", e.getId());
        assertEquals("ido", e.getIdOriginale());
        assertEquals("nota", e.getValoreNota());
    }
}
