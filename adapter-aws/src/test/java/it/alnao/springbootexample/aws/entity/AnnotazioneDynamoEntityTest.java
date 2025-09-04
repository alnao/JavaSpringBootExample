package it.alnao.springbootexample.aws.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneDynamoEntityTest {
    @Test
    void gettersAndSetters() {
        AnnotazioneDynamoEntity e = new AnnotazioneDynamoEntity();
        e.setId("id");
        e.setValoreNota("nota");
        e.setVersioneNota("v1");
        assertEquals("id", e.getId());
        assertEquals("nota", e.getValoreNota());
        assertEquals("v1", e.getVersioneNota());
    }
}
