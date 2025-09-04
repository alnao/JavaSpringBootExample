package it.alnao.springbootexample.app;

import it.alnao.springbootexample.app.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
class GestionePersonaleApplicationTest {
    
    @Test
    void contextLoads() {
        // Verifica che il contesto Spring si avvii correttamente con H2 in-memory
        // Questo test assicura che tutte le configurazioni siano corrette
    }
}
