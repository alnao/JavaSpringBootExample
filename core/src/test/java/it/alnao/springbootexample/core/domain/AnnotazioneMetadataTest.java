package it.alnao.springbootexample.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMetadataTest {
    @Test
    void testInstance() {
        AnnotazioneMetadata a = new AnnotazioneMetadata();
        assertNotNull(a);
    }
}
