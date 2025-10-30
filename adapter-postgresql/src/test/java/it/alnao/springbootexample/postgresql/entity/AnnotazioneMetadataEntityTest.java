package it.alnao.springbootexample.postgresql.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMetadataEntityTest {

    @Test
    void testGettersAndSetters() {
        AnnotazioneMetadataEntity entity = new AnnotazioneMetadataEntity();
        LocalDateTime now = LocalDateTime.now();
        
        entity.setId("123");
        entity.setVersioneNota("v1.0");
        entity.setUtenteCreazione("user1");
        entity.setDataInserimento(now);
        entity.setDataUltimaModifica(now);
        entity.setUtenteUltimaModifica("user2");
        entity.setDescrizione("Descrizione test");
        entity.setCategoria("Categoria1");
        entity.setTags("tag1,tag2");
        entity.setPubblica(true);
        entity.setPriorita(5);
        entity.setStato("INSERITA");
        
        assertEquals("123", entity.getId());
        assertEquals("v1.0", entity.getVersioneNota());
        assertEquals("user1", entity.getUtenteCreazione());
        assertEquals(now, entity.getDataInserimento());
        assertEquals(now, entity.getDataUltimaModifica());
        assertEquals("user2", entity.getUtenteUltimaModifica());
        assertEquals("Descrizione test", entity.getDescrizione());
        assertEquals("Categoria1", entity.getCategoria());
        assertEquals("tag1,tag2", entity.getTags());
        assertTrue(entity.getPubblica());
        assertEquals(5, entity.getPriorita());
        assertEquals("INSERITA", entity.getStato());
    }

    @Test
    void testDefaultConstructor() {
        AnnotazioneMetadataEntity entity = new AnnotazioneMetadataEntity();
        
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getVersioneNota());
        assertNull(entity.getStato());
    }
}
