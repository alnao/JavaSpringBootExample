package it.alnao.springbootexample.api.dto.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoTest {

    // ---------- RegisterRequest ----------

    @Test
    void registerRequest_constructorSetsTheMandatoryFields() {
        RegisterRequest r = new RegisterRequest("mario", "mario@test.it", "secret");
        assertEquals("mario", r.getUsername());
        assertEquals("mario@test.it", r.getEmail());
        assertEquals("secret", r.getPassword());
        assertNull(r.getFirstName());
        assertNull(r.getLastName());
    }

    @Test
    void registerRequest_settersAndGettersWork() {
        RegisterRequest r = new RegisterRequest();
        r.setUsername("luigi");
        r.setEmail("luigi@test.it");
        r.setPassword("pwd");
        r.setFirstName("Luigi");
        r.setLastName("Verdi");

        assertEquals("luigi", r.getUsername());
        assertEquals("luigi@test.it", r.getEmail());
        assertEquals("pwd", r.getPassword());
        assertEquals("Luigi", r.getFirstName());
        assertEquals("Verdi", r.getLastName());
    }

    @Test
    void registerRequest_toStringDoesNotLeakThePassword() {
        RegisterRequest r = new RegisterRequest("mario", "mario@test.it", "super-segreta");
        String text = r.toString();
        assertTrue(text.contains("mario"));
        assertTrue(text.contains("mario@test.it"));
        assertFalse(text.contains("super-segreta"));
    }

    // ---------- OAuth2ProviderInfo ----------

    @Test
    void oauth2ProviderInfo_threeArgsConstructorMarksItAvailable() {
        OAuth2ProviderInfo p = new OAuth2ProviderInfo("google", "Google", "/oauth2/google");
        assertEquals("google", p.getId());
        assertEquals("Google", p.getName());
        assertEquals("/oauth2/google", p.getAuthUrl());
        assertTrue(p.isAvailable());
    }

    @Test
    void oauth2ProviderInfo_fourArgsConstructorKeepsTheGivenAvailability() {
        OAuth2ProviderInfo p = new OAuth2ProviderInfo("github", "GitHub", "/oauth2/github", false);
        assertEquals("github", p.getId());
        assertFalse(p.isAvailable());
    }

    @Test
    void oauth2ProviderInfo_settersAndGettersWork() {
        OAuth2ProviderInfo p = new OAuth2ProviderInfo();
        p.setId("microsoft");
        p.setName("Microsoft");
        p.setAuthUrl("/oauth2/microsoft");
        p.setAvailable(true);

        assertEquals("microsoft", p.getId());
        assertEquals("Microsoft", p.getName());
        assertEquals("/oauth2/microsoft", p.getAuthUrl());
        assertTrue(p.isAvailable());
    }

    @Test
    void oauth2ProviderInfo_defaultConstructorLeavesItUnavailable() {
        OAuth2ProviderInfo p = new OAuth2ProviderInfo();
        assertNull(p.getId());
        assertFalse(p.isAvailable());
    }

    @Test
    void oauth2ProviderInfo_toStringIncludesIdAndAvailability() {
        String text = new OAuth2ProviderInfo("google", "Google", "/oauth2/google").toString();
        assertTrue(text.contains("google"));
        assertTrue(text.contains("available=true"));
    }
}
