package it.alnao.springbootexample.mongodb.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MongoDBConfigurationTest {

    @Test
    void testConfigurationCreation() {
        MongoDBConfiguration config = new MongoDBConfiguration();
        
        assertNotNull(config);
    }
}
