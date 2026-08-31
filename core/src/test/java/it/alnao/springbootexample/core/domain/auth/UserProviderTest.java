package it.alnao.springbootexample.core.domain.auth;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserProviderTest {

    @Test
    void noArgConstructor_works() {
        UserProvider up = new UserProvider();
        assertNotNull(up);
    }

    @Test
    void threeArgConstructor_setsFields() {
        UserProvider up = new UserProvider("user-1", "google", "google-ext-id");
        assertEquals("user-1", up.getUserId());
        assertEquals("google", up.getProvider());
        assertEquals("google-ext-id", up.getProviderUserId());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        UserProvider up = new UserProvider();
        up.setId("id-1");
        up.setUserId("user-1");
        up.setProvider("github");
        up.setProviderUserId("gh-ext-id");
        up.setProviderEmail("user@github.com");
        up.setProviderUsername("ghuser");
        up.setAccessTokenHash("hash-abc");
        up.setCreatedAt(now);
        up.setLastUsed(now);

        assertEquals("id-1", up.getId());
        assertEquals("user-1", up.getUserId());
        assertEquals("github", up.getProvider());
        assertEquals("gh-ext-id", up.getProviderUserId());
        assertEquals("user@github.com", up.getProviderEmail());
        assertEquals("ghuser", up.getProviderUsername());
        assertEquals("hash-abc", up.getAccessTokenHash());
        assertEquals(now, up.getCreatedAt());
        assertEquals(now, up.getLastUsed());
    }
}
