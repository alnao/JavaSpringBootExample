package it.alnao.annotazioni.port.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMetadataTest {
    @Test
    void testInstance() {
        AnnotazioneMetadata a = new AnnotazioneMetadata();
        assertNotNull(a);
    }
}
