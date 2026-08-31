package it.alnao.springbootexample.api.dto.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtResponseTest {

    @Test
    void noArgConstructor_works() {
        JwtResponse r = new JwtResponse();
        assertEquals("Bearer", r.getTokenType());
    }

    @Test
    void twoArgConstructor_setsFields() {
        JwtResponse r = new JwtResponse("tok", "user", AccountType.LOCAL);
        assertEquals("tok", r.getToken());
        assertEquals("user", r.getUsername());
        assertEquals(AccountType.LOCAL, r.getAccountType());
    }

    @Test
    void fullArgConstructor_setsAllFields() {
        JwtResponse r = new JwtResponse("tok", "user", "user@test.com", AccountType.LOCAL, "USER", 3600L);
        assertEquals("tok", r.getToken());
        assertEquals("user", r.getUsername());
        assertEquals("user@test.com", r.getEmail());
        assertEquals(AccountType.LOCAL, r.getAccountType());
        assertEquals("USER", r.getRole());
        assertEquals(3600L, r.getExpiresIn());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        JwtResponse r = new JwtResponse();
        r.setToken("t");
        r.setUsername("u");
        r.setEmail("e@e.com");
        r.setAccountType(AccountType.GOOGLE);
        r.setRole("ADMIN");
        r.setLinkedProviders(List.of("google", "github"));
        r.setTokenType("Bearer");
        r.setExpiresIn(7200L);

        assertEquals("t", r.getToken());
        assertEquals("u", r.getUsername());
        assertEquals("e@e.com", r.getEmail());
        assertEquals(AccountType.GOOGLE, r.getAccountType());
        assertEquals("ADMIN", r.getRole());
        assertEquals(List.of("google", "github"), r.getLinkedProviders());
        assertEquals("Bearer", r.getTokenType());
        assertEquals(7200L, r.getExpiresIn());
    }
}
