package it.alnao.annotazioni.api.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {
    @Test
    void swaggerConfigBeanNotNull() {
        SwaggerConfig config = new SwaggerConfig();
        assertNotNull(config.annotazioniOpenAPI());
    }
}
