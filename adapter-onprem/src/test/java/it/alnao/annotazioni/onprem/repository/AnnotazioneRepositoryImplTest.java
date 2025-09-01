package it.alnao.annotazioni.onprem.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneRepositoryImplTest {
    @Test
    void classLoads() {
        AnnotazioneRepositoryImpl impl = new AnnotazioneRepositoryImpl();
        assertNotNull(impl);
    }
}
