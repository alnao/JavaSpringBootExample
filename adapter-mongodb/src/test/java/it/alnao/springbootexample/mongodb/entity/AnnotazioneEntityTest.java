package it.alnao.springbootexample.mongodb.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneEntityTest {

    @Test
    void testConstructorAndGetters() {
        String id = "123";
        String versione = "v1.0";
        String valore = "Test valore";
        
        AnnotazioneEntity entity = new AnnotazioneEntity(id, versione, valore);
        
        assertEquals(id, entity.getId());
        assertEquals(versione, entity.getVersioneNota());
        assertEquals(valore, entity.getValoreNota());
    }

    @Test
    void testSetters() {
        AnnotazioneEntity entity = new AnnotazioneEntity();
        
        entity.setId("456");
        entity.setVersioneNota("v2.0");
        entity.setValoreNota("Nuovo valore");
        
        assertEquals("456", entity.getId());
        assertEquals("v2.0", entity.getVersioneNota());
        assertEquals("Nuovo valore", entity.getValoreNota());
    }

    @Test
    void testDefaultConstructor() {
        AnnotazioneEntity entity = new AnnotazioneEntity();
        
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getVersioneNota());
        assertNull(entity.getValoreNota());
    }
}
