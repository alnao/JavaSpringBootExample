package it.alnao.springbootexample.core.repository;

import org.junit.jupiter.api.Test;

class AnnotazioneRepositoryTest {
    @Test
    void testInterface() {
        // Interface presence test
        Class<?> clazz = AnnotazioneRepository.class;
        assert clazz != null;
    }
}
