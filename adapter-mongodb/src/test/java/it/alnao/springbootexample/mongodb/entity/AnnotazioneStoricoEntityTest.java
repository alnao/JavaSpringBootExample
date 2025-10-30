package it.alnao.springbootexample.mongodb.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoEntityTest {

    @Test
    void testGettersAndSetters() {
        AnnotazioneStoricoEntity entity = new AnnotazioneStoricoEntity();
        LocalDateTime now = LocalDateTime.now();
        
        entity.setId("1");
        entity.setIdOriginale("orig-1");
        entity.setVersioneNota("v1.0");
        entity.setValoreNota("Test");
        entity.setDescrizione("Descrizione");
        entity.setUtente("user1");
        entity.setCategoria("Cat1");
        entity.setTags("tag1,tag2");
        entity.setPubblica(true);
        entity.setPriorita(5);
        entity.setDataModifica(now);
        
        assertEquals("1", entity.getId());
        assertEquals("orig-1", entity.getIdOriginale());
        assertEquals("v1.0", entity.getVersioneNota());
        assertEquals("Test", entity.getValoreNota());
        assertEquals("Descrizione", entity.getDescrizione());
        assertEquals("user1", entity.getUtente());
        assertEquals("Cat1", entity.getCategoria());
        assertEquals("tag1,tag2", entity.getTags());
        assertTrue(entity.getPubblica());
        assertEquals(5, entity.getPriorita());
        assertEquals(now, entity.getDataModifica());
    }

    @Test
    void testDefaultConstructor() {
        AnnotazioneStoricoEntity entity = new AnnotazioneStoricoEntity();
        
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getIdOriginale());
    }
}
