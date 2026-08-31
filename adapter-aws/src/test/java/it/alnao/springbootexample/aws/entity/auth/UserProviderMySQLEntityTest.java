package it.alnao.springbootexample.aws.entity.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserProviderMySQLEntityTest {

    @Test
    void noArgConstructor_setsTimestamps() {
        UserProviderMySQLEntity e = new UserProviderMySQLEntity();
        assertNotNull(e.getCreatedAt());
        assertNotNull(e.getLastUsed());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        UserProviderMySQLEntity e = new UserProviderMySQLEntity();
        LocalDateTime now = LocalDateTime.now();
        e.setId("id-1");
        e.setUserId("user-1");
        e.setProvider("google");
        e.setProviderUserId("google-ext-id");
        e.setProviderEmail("g@gmail.com");
        e.setProviderUsername("guser");
        e.setAccessTokenHash("hash-abc");
        e.setCreatedAt(now);
        e.setLastUsed(now);

        assertEquals("id-1", e.getId());
        assertEquals("user-1", e.getUserId());
        assertEquals("google", e.getProvider());
        assertEquals("google-ext-id", e.getProviderUserId());
        assertEquals("g@gmail.com", e.getProviderEmail());
        assertEquals("guser", e.getProviderUsername());
        assertEquals("hash-abc", e.getAccessTokenHash());
        assertEquals(now, e.getCreatedAt());
        assertEquals(now, e.getLastUsed());
    }

    @Test
    void fromDomain_mapsAllFields() {
        UserProvider up = buildProvider("id-1", "user-1", "github");
        UserProviderMySQLEntity entity = UserProviderMySQLEntity.fromDomain(up);
        assertNotNull(entity);
        assertEquals("user-1", entity.getUserId());
        assertEquals("github", entity.getProvider());
    }

    @Test
    void toDomain_mapsAllFields() {
        UserProviderMySQLEntity e = new UserProviderMySQLEntity();
        e.setId("id-1");
        e.setUserId("user-1");
        e.setProvider("google");
        e.setProviderUserId("ext-id");
        e.setProviderEmail("g@gmail.com");

        UserProvider up = e.toDomain();

        assertNotNull(up);
        assertEquals("user-1", up.getUserId());
        assertEquals("google", up.getProvider());
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
