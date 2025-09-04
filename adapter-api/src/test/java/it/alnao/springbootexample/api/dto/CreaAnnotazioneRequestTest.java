package it.alnao.springbootexample.api.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CreaAnnotazioneRequestTest {
    @Test
    void gettersAndSetters() {
        CreaAnnotazioneRequest req = new CreaAnnotazioneRequest();
        req.setValoreNota("nota");
        req.setDescrizione("desc");
        req.setUtente("utente");
        req.setCategoria("cat");
        req.setTags("tag");
        req.setPubblica(true);
        req.setPriorita(2);
        assertEquals("nota", req.getValoreNota());
        assertEquals("desc", req.getDescrizione());
        assertEquals("utente", req.getUtente());
        assertEquals("cat", req.getCategoria());
        assertEquals("tag", req.getTags());
        assertTrue(req.getPubblica());
        assertEquals(2, req.getPriorita());
    }
}
