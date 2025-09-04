package it.alnao.springbootexample.port.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneTest {
    @Test
    void testInstance() {
        Annotazione a = new Annotazione();
        assertNotNull(a);
    }
}
