package it.alnao.annotazioni.api.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AggiornaAnnotazioneRequestTest {
    @Test
    void gettersAndSetters() {
        AggiornaAnnotazioneRequest req = new AggiornaAnnotazioneRequest();
        UUID id = UUID.randomUUID();
        req.setId(id);
        req.setValoreNota("nota");
        req.setDescrizione("desc");
        req.setUtente("utente");
        req.setCategoria("cat");
        req.setTags("tag");
        req.setPubblica(true);
        req.setPriorita(2);
        assertEquals(id, req.getId());
        assertEquals("nota", req.getValoreNota());
        assertEquals("desc", req.getDescrizione());
        assertEquals("utente", req.getUtente());
        assertEquals("cat", req.getCategoria());
        assertEquals("tag", req.getTags());
        assertTrue(req.getPubblica());
        assertEquals(2, req.getPriorita());
    }
}
