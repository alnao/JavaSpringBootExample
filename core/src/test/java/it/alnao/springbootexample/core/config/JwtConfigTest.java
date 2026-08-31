package it.alnao.springbootexample.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    private JwtConfig configWith(String secret, Long expiration, Long refreshExpiration) {
        JwtConfig config = new JwtConfig();
        ReflectionTestUtils.setField(config, "jwtSecret", secret);
        ReflectionTestUtils.setField(config, "jwtExpiration", expiration);
        ReflectionTestUtils.setField(config, "jwtRefreshExpiration", refreshExpiration);
        return config;
    }

    @Test
    void jwtConfigBean_carriesTheConfiguredValues() {
        JwtConfig.JwtConfigBean bean =
                configWith("un-secret-di-test", 3600L, 86400L).jwtConfigBean();
        assertEquals("un-secret-di-test", bean.getSecret());
        assertEquals(3600L, bean.getExpiration());
        assertEquals(86400L, bean.getRefreshExpiration());
    }

    @Test
    void jwtConfigBean_constructorIsUsableDirectly() {
        JwtConfig.JwtConfigBean bean = new JwtConfig.JwtConfigBean("s", 1L, 2L);
        assertEquals("s", bean.getSecret());
        assertEquals(1L, bean.getExpiration());
        assertEquals(2L, bean.getRefreshExpiration());
    }

    @Test
    void passwordEncoder_isBCryptAndRoundTrips() {
        PasswordEncoder encoder = configWith("s", 1L, 2L).passwordEncoder();
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
        String hash = encoder.encode("password-in-chiaro");
        assertNotEquals("password-in-chiaro", hash);
        assertTrue(encoder.matches("password-in-chiaro", hash));
        assertFalse(encoder.matches("password-sbagliata", hash));
    }
}
