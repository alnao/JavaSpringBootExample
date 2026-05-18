package it.alnao.springbootexample.core.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationLockedExceptionTest {

    @Test
    void costruttore_conIdEOwner_impostaMessaggio() {
        UUID id = UUID.randomUUID();
        AnnotationLockedException ex = new AnnotationLockedException(id, "mario");
        assertEquals(id, ex.getAnnotazioneId());
        assertEquals("mario", ex.getCurrentOwner());
        assertTrue(ex.getMessage().contains(id.toString()));
        assertTrue(ex.getMessage().contains("mario"));
    }

    @Test
    void costruttore_conSoloMessaggio_impostaMessaggio() {
        AnnotationLockedException ex = new AnnotationLockedException("messaggio personalizzato");
        assertEquals("messaggio personalizzato", ex.getMessage());
        assertNull(ex.getAnnotazioneId());
        assertNull(ex.getCurrentOwner());
    }

    @Test
    void isRuntimeException() {
        AnnotationLockedException ex = new AnnotationLockedException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
