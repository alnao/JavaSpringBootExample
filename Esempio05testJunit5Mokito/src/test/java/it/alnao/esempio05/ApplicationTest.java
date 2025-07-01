package it.alnao.esempio05;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.profiles.active=test") //indispensabile per usare il application-test.properties
class ApplicationTest {

    @Test
    void contextLoads() {
        // Se il contesto si avvia correttamente, questo test passa.
        assertTrue(true, "Contesto Spring avviato correttamente");
    }
}
 