package it.alnao.springbootexample.sqlite.entity.auth;

import it.alnao.springbootexample.core.domain.auth.UserProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserProviderSQLiteEntityTest {

    @Test
    void noArgConstructor_works() {
        UserProviderSQLiteEntity e = new UserProviderSQLiteEntity();
        assertNotNull(e);
    }

    @Test
    void domainConstructor_mapsAllFields() {
        UserProvider up = buildProvider("id-1", "user-1", "google");
        UserProviderSQLiteEntity e = new UserProviderSQLiteEntity(up);
        assertEquals("user-1", e.getUserId());
        assertEquals("google", e.getProvider());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        UserProviderSQLiteEntity e = new UserProviderSQLiteEntity();
        LocalDateTime now = LocalDateTime.now();
        e.setId("id-1");
        e.setUserId("user-1");
        e.setProvider("github");
        e.setExternalId("ext-1");
        e.setProviderEmail("g@gh.com");
        e.setProviderUsername("guser");
        e.setAccessTokenHash("hash-123");
        e.setCreatedAt(now);
        e.setLastUsed(now);

        assertEquals("id-1", e.getId());
        assertEquals("user-1", e.getUserId());
        assertEquals("github", e.getProvider());
        assertEquals("ext-1", e.getExternalId());
        assertEquals("g@gh.com", e.getProviderEmail());
        assertEquals("guser", e.getProviderUsername());
        assertEquals("hash-123", e.getAccessTokenHash());
        assertEquals(now, e.getCreatedAt());
        assertEquals(now, e.getLastUsed());
    }

    @Test
    void fromDomain_delegatesToConstructor() {
        UserProvider up = buildProvider("id-1", "user-1", "google");
        UserProviderSQLiteEntity e = UserProviderSQLiteEntity.fromDomain(up);
        assertNotNull(e);
        assertEquals("user-1", e.getUserId());
    }

    @Test
    void toDomain_mapsAllFields() {
        UserProviderSQLiteEntity e = new UserProviderSQLiteEntity();
        e.setId("id-1");
        e.setUserId("user-1");
        e.setProvider("github");
        e.setExternalId("ext-1");
        e.setProviderEmail("g@gh.com");
        e.setProviderUsername("guser");

        UserProvider up = e.toDomain();

        assertNotNull(up);
        assertEquals("user-1", up.getUserId());
        assertEquals("github", up.getProvider());
    }

    private UserProvider buildProvider(String id, String userId, String provider) {
        UserProvider up = new UserProvider();
        up.setId(id);
        up.setUserId(userId);
        up.setProvider(provider);
        up.setProviderUserId("ext-" + id);
        up.setProviderEmail("test@provider.com");
        return up;
    }
}
