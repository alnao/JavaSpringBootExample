package it.alnao.springbootexample.mongodb.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoStatiEntityTest {

    @Test
    void testConstructorWithParameters() {
        String idOp = "op-1";
        String idAnn = "ann-1";
        String vers = "v1.0";
        String statoNew = "MODIFICATA";
        String statoOld = "INSERITA";
        String utente = "user1";
        LocalDateTime data = LocalDateTime.now();
        String nota = "Cambio stato";
        
        AnnotazioneStoricoStatiEntity entity = new AnnotazioneStoricoStatiEntity(
            idOp, idAnn, vers, statoNew, statoOld, utente, data, nota
        );
        
        assertEquals(idOp, entity.getIdOperazione());
        assertEquals(idAnn, entity.getIdAnnotazione());
        assertEquals(vers, entity.getVersione());
        assertEquals(statoNew, entity.getStatoNew());
        assertEquals(statoOld, entity.getStatoOld());
        assertEquals(utente, entity.getUtente());
        assertEquals(data, entity.getDataModifica());
        assertEquals(nota, entity.getNotaOperazione());
    }

    @Test
    void testGettersAndSetters() {
        AnnotazioneStoricoStatiEntity entity = new AnnotazioneStoricoStatiEntity();
        LocalDateTime now = LocalDateTime.now();
        
        entity.setIdOperazione("op-2");
        entity.setIdAnnotazione("ann-2");
        entity.setVersione("v2.0");
        entity.setStatoNew("APPROVATA");
        entity.setStatoOld("MODIFICATA");
        entity.setUtente("user2");
        entity.setDataModifica(now);
        entity.setNotaOperazione("Test nota");
        
        assertEquals("op-2", entity.getIdOperazione());
        assertEquals("ann-2", entity.getIdAnnotazione());
        assertEquals("v2.0", entity.getVersione());
        assertEquals("APPROVATA", entity.getStatoNew());
        assertEquals("MODIFICATA", entity.getStatoOld());
        assertEquals("user2", entity.getUtente());
        assertEquals(now, entity.getDataModifica());
        assertEquals("Test nota", entity.getNotaOperazione());
    }

    @Test
    void testToString() {
        AnnotazioneStoricoStatiEntity entity = new AnnotazioneStoricoStatiEntity();
        entity.setIdOperazione("op-1");
        entity.setIdAnnotazione("ann-1");
        
        String result = entity.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("op-1"));
        assertTrue(result.contains("ann-1"));
    }
}
