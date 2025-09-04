package it.alnao.springbootexample.aws.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneRepositoryAwsImplTest {
    @Test
    void classLoads() {
        AnnotazioneRepositoryAwsImpl impl = new AnnotazioneRepositoryAwsImpl();
        assertNotNull(impl);
    }
}
