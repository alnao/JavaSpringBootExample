package it.alnao.springbootexample.javafx.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SecurityDisabledConfigTest {

    @Test
    void isASpringConfiguration() {
        assertNotNull(SecurityDisabledConfig.class.getAnnotation(Configuration.class));
        assertNotNull(new SecurityDisabledConfig());
    }

    @Test
    void excludesSpringSecurityAutoConfiguration() {
        EnableAutoConfiguration annotation =
                SecurityDisabledConfig.class.getAnnotation(EnableAutoConfiguration.class);
        assertNotNull(annotation);
        assertTrue(Arrays.asList(annotation.exclude()).contains(SecurityAutoConfiguration.class));
    }
}
