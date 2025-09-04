package it.alnao.springbootexample.port.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneCompletaTest {
    @Test
    void testInstance() {
        AnnotazioneCompleta a = new AnnotazioneCompleta();
        assertNotNull(a);
    }
}
