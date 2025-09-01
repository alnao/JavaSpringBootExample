package it.alnao.annotazioni.api.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneResponseTest {
    @Test
    void gettersAndSetters() {
        AnnotazioneResponse resp = new AnnotazioneResponse();
        UUID id = UUID.randomUUID();
        resp.setId(id);
        resp.setVersioneNota("v1");
        resp.setValoreNota("nota");
        resp.setDescrizione("desc");
        resp.setUtenteCreazione("utente");
        LocalDateTime now = LocalDateTime.now();
        resp.setDataInserimento(now);
        resp.setDataUltimaModifica(now);
        resp.setUtenteUltimaModifica("mod");
        resp.setCategoria("cat");
        resp.setTags("tag");
        resp.setPubblica(true);
        resp.setPriorita(2);
        assertEquals(id, resp.getId());
        assertEquals("v1", resp.getVersioneNota());
        assertEquals("nota", resp.getValoreNota());
        assertEquals("desc", resp.getDescrizione());
        assertEquals("utente", resp.getUtenteCreazione());
        assertEquals(now, resp.getDataInserimento());
        assertEquals(now, resp.getDataUltimaModifica());
        assertEquals("mod", resp.getUtenteUltimaModifica());
        assertEquals("cat", resp.getCategoria());
        assertEquals("tag", resp.getTags());
        assertTrue(resp.getPubblica());
        assertEquals(2, resp.getPriorita());
    }
}
