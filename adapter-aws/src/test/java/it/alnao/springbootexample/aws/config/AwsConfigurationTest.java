package it.alnao.springbootexample.aws.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AwsConfigurationTest {
    @Test
    void configLoads() {
        AwsConfiguration config = new AwsConfiguration();
        assertNotNull(config);
    }
}
