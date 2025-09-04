package it.alnao.springbootexample.port.repository;

import org.junit.jupiter.api.Test;

class AnnotazioneRepositoryTest {
    @Test
    void testInterface() {
        // Interface presence test
        Class<?> clazz = AnnotazioneRepository.class;
        assert clazz != null;
    }
}
