package it.alnao.springbootexample.core.service;

import org.junit.jupiter.api.Test;

class AnnotazioneServiceTest {
    @Test
    void testInterface() {
        // Interface presence test
        Class<?> clazz = AnnotazioneService.class;
        assert clazz != null;
    }
}
