package it.alnao.springbootexample.onprem.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OnPremConfigurationTest {
    @Test
    void configLoads() {
        OnPremConfiguration config = new OnPremConfiguration();
        assertNotNull(config);
    }
}
