package it.alnao.annotazioni.aws.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnotazioneStoricoDynamoRepositoryImplTest {
    @Test
    void classLoads() {
        AnnotazioneStoricoDynamoRepositoryImpl impl = new AnnotazioneStoricoDynamoRepositoryImpl();
        assertNotNull(impl);
    }
}
