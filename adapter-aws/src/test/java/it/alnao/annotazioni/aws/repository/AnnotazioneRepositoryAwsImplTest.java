package it.alnao.annotazioni.aws.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneRepositoryAwsImplTest {
    @Test
    void classLoads() {
        AnnotazioneRepositoryAwsImpl impl = new AnnotazioneRepositoryAwsImpl();
        assertNotNull(impl);
    }
}
