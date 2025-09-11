package it.alnao.springbootexample.app;

import it.alnao.springbootexample.app.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

@SpringBootTest
@ActiveProfiles("sqlite")
@Import(TestConfig.class)
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
class GestioneAnnotazioniApplicationTest {
    
    @Test
    void contextLoads() {
        // Verifica che il contesto Spring si avvii correttamente con H2 in-memory
        // Questo test assicura che tutte le configurazioni siano corrette
    }

    // No custom beans needed for context loading test. Remove unnecessary AuthenticationManager bean.
}
