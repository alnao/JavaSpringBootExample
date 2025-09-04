package it.alnao.springbootexample.onprem.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneMetadataRepositoryImplTest {
    @Test
    void classLoads() {
        AnnotazioneMetadataRepositoryImpl impl = new AnnotazioneMetadataRepositoryImpl();
        assertNotNull(impl);
    }
}
