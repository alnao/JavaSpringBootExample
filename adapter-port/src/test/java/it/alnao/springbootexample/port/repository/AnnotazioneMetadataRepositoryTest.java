package it.alnao.springbootexample.port.repository;

import org.junit.jupiter.api.Test;

class AnnotazioneMetadataRepositoryTest {
    @Test
    void testInterface() {
        // Interface presence test
        Class<?> clazz = AnnotazioneMetadataRepository.class;
        assert clazz != null;
    }
}
