package it.alnao.springbootexample.postgresql.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostgreSQLConfigurationTest {

    @Test
    void testConfigurationCreation() {
        PostgreSQLConfiguration config = new PostgreSQLConfiguration();
        
        assertNotNull(config);
    }
}
